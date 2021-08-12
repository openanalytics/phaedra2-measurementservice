package eu.openanalytics.phaedra.measservice.repository.dao;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.postgresql.copy.CopyManager;
import org.postgresql.jdbc.PgConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MeasWelldataDAO {

	@Value("${meas-service.welldata.db.schema:phaedra}")
	private String schemaName;
	
	@Value("${meas-service.welldata.db.table:welldata}")
	private String tableName;
	
	@Autowired
	private DataSource dataSource;
	
	public float[] getData(long measId, String column) {
		String sql = String.format("select values from %s.%s where meas_id = %d and column_name = '%s'", schemaName, tableName, measId, column);
		return select(sql, rs -> (rs.next()) ? getNumValues(rs, "values") : null, 0);
	}
	
	public float[][] getData(long measId, String[] columns) {
		String colNameString = Arrays.stream(columns).map(c -> "'" + c + "'").collect(Collectors.joining(","));
		String sql = String.format("select column_name, values from %s.%s where meas_id = %d and column_name in (%s)", schemaName, tableName, measId, colNameString);
		return select(sql, rs -> {
			float[][] retVal = new float[columns.length][];
			while (rs.next()) {
				String colName = rs.getString("column_name");
				float[] values = getNumValues(rs, "values");
				int index = ArrayUtils.indexOf(columns, colName);
				if (index >= 0) retVal[index] = values;
			}
			return retVal;
		}, 0);
	}

	public Map<String, float[]> getData(long measId) {
		String sql = String.format("select column_name, values from %s.%s where meas_id = %d", schemaName, tableName, measId);
		return select(sql, rs -> {
			Map<String, float[]> results = new HashMap<>();
			while (rs.next()) {
				String colName = rs.getString("column_name");
				float[] values = getNumValues(rs, "values");
				results.put(colName, values);
			}
			return results;
		}, 0);
	}
	
	public void saveData(long measId, Map<String, float[]> data) {
		if (data == null || data.isEmpty()) throw new RuntimeException("No measurement data provided");
		
		String sql = String.format("select count(*) from %s.%s where meas_id = %d", schemaName, tableName, measId);
		int rowCount = select(sql, rs -> (rs.next()) ? rs.getInt(1) : 0, 0);
		if (rowCount > 0) throw new RuntimeException("Cannot save measurement data: data already exists for meas " + measId);

		try (Connection conn = getConnection()) {
			byte[] dataCSV = toCSV(measId, data);
			sql = String.format("copy %s.%s (meas_id, column_name, values) from stdin (format csv)", schemaName, tableName);
			CopyManager cm = conn.unwrap(PgConnection.class).getCopyAPI();
			cm.copyIn(sql, new ByteArrayInputStream(dataCSV));
			conn.commit();
		} catch (IOException | SQLException e) {
			throw new RuntimeException("Failed to save measurement data", e);
		}
	}
	
	public void deleteData(long measId) {
		String sql = String.format("delete from %s.%s where meas_id = %d", schemaName, tableName, measId);
		execute(sql);
	}
	
	private float[] getNumValues(ResultSet rs, String column) throws SQLException {
		Array numValArray = rs.getArray(column);
		if (numValArray == null) return null;
		Double[] doubleValues = (Double[]) numValArray.getArray();
		if (doubleValues == null) return null;
		
		float[] floatValues = new float[doubleValues.length];
		for (int i=0; i<doubleValues.length; i++) floatValues[i] = doubleValues[i].floatValue();
		return floatValues;
	}
	
	private <T> T select(String sql, ResultProcessor<T> resultProcessor, int fetchSize) {
		try (Connection conn = getConnection()) {
			try (Statement stmt = conn.createStatement()) {
				if (fetchSize > 0) stmt.setFetchSize(fetchSize);
				try (ResultSet rs = stmt.executeQuery(sql)) {
					return resultProcessor.process(rs);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to execute query: " + sql, e);
		}
	}
	
	private void execute(String sql) {
		try (Connection conn = getConnection()) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sql);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to execute query: " + sql, e);
		}
	}
	
	private interface ResultProcessor<T> {
		public T process(ResultSet rs) throws SQLException;
	}
	
	private byte[] toCSV(long measId, Map<String, float[]> data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (BufferedWriter csvWriter = new BufferedWriter(new OutputStreamWriter(bos))) {
			for (String colName: data.keySet()) {
				float[] values = data.get(colName);
				String valueArray = IntStream.range(0, values.length).mapToObj(i -> String.valueOf(values[i])).collect(Collectors.joining(","));
				csvWriter.write(String.format("%d,\"%s\",\"{%s}\"\n", measId, colName, valueArray));
			}
			csvWriter.flush();
		}
		return bos.toByteArray();
	}
	
	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
}
