/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.measservice.repository.dao;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.*;
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

	@Value("${meas-service.db.schema:measservice}")
	private String schemaName;

	@Value("${meas-service.db.welldata-table:welldata}")
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
		} catch (SQLException | IOException e) {
			throw new RuntimeException("Failed to save measurement data", e);
		}
	}

	public void saveData(long measId, String column, float[] data) {
		if (data == null || ArrayUtils.isEmpty(data)) throw new RuntimeException("No measurement data provided");

		String sql = String.format("select count(*) from %s.%s where meas_id = %d", schemaName, tableName, measId);
		int rowCount = select(sql, rs -> (rs.next()) ? rs.getInt(1) : 0, 0);
		if (rowCount > 0) throw new RuntimeException("Cannot save measurement data: data already exists for meas " + measId);

		try (Connection conn = getConnection()) {
			sql = String.format("insert into %s.%s(meas_id, column_name, \"values\") values (?, ?, ?)", schemaName, tableName);

			PreparedStatement stmt= conn.prepareStatement(sql);
			stmt.setLong(1, measId);
			stmt.setString(2, column);
			stmt.setObject(3, data);
			stmt.executeUpdate();

			conn.commit();
		} catch (SQLException e) {
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
