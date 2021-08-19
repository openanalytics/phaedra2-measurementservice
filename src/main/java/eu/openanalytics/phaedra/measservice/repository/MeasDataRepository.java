package eu.openanalytics.phaedra.measservice.repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import eu.openanalytics.phaedra.measservice.repository.dao.MeasObjectStoreDAO;
import eu.openanalytics.phaedra.measservice.repository.dao.MeasWelldataDAO;

@Repository
public class MeasDataRepository {

	@Autowired
	private MeasWelldataDAO welldataDAO;
	
	@Autowired
	private MeasObjectStoreDAO objectStoreDAO;
		
	public float[] getWellData(long measId, String column) {
		return welldataDAO.getData(measId, column);
	}
	
	public Map<String, float[]> getWellData(long measId) {
		return welldataDAO.getData(measId);
	}
	
	public void deleteWellData(long measId) {
		welldataDAO.deleteData(measId);
	}
	
	public void setWellData(long measId, Map<String, float[]> wellData) {
		welldataDAO.saveData(measId, wellData);
	}
	
	/*
	 * Subwell data storage approach
	 * *****************************
	 * 
	 * One object per well per column.
	 * 
	 * Key: "subwelldata.<colName>.<wellNr>"
	 * Value: float[]
	 * 
	 * Summary:
	 * - Thousands of objects per well
	 * - A million objects per meas
	 * - Several KB per value
	 * - Thus, several MB per well
	 * - Thus, several GB per meas
	 */
	
	public void putSubWellData(long measId, String column, float[][] data) {
		IntStream.range(0, data.length).parallel().forEach(i -> {
			putSubWellData(measId, i + 1, column, data[i]);
		});
	}
	
	public void putSubWellData(long measId, int wellNr, String column, float[] data) {
		String key = String.format("subwelldata.%s.%d", column, wellNr); 
		try {
			objectStoreDAO.putMeasObject(measId, key, data);
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to store subwell data for measurement %d, well %d, column %s", measId, wellNr, column), e);
		}
	}
	
	public float[] getSubWellData(long measId, int wellNr, String column) {
		String key = String.format("subwelldata.%s.%d", column, wellNr);
		try {
			float[] swData = (float[]) objectStoreDAO.getMeasObject(measId, key);
			return swData;
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to retrieve subwell data for measurement %d, well %d, column %s", measId, wellNr, column), e);
		}
	}
	
	public Map<Integer, float[]> getSubWellData(long measId, String column) {
		String prefix = String.format("subwelldata.%s", column);
		try {
			// Find all available subkeys (expected: 1 key per well)
			String[] keys = objectStoreDAO.listMeasObjects(measId, prefix);
			
			return Arrays.stream(keys).parallel().map(k -> {
				try {
					Integer wellNr = Integer.valueOf(k.substring(k.lastIndexOf('.') + 1));
					float[] values = (float[]) objectStoreDAO.getMeasObject(measId, k);
					return Pair.of(wellNr, values);
				} catch (IOException e) {
					throw new RecoverableDataAccessException(
							String.format("Failed to retrieve subwell data for measurement %d, column %s", measId, column), e);
				}
			}).collect(Collectors.toMap(p -> p.getFirst(), p -> p.getSecond()));
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to retrieve subwell data for measurement %d, column %s", measId, column), e);
		}
	}
	
	public void deleteSubWellData(long measId) {
		String prefix = "subwelldata";
		try {
			// Find all available subkeys (expected: 1 key per well per column)
			String[] keys = objectStoreDAO.listMeasObjects(measId, prefix);
			objectStoreDAO.deleteMeasObjects(measId, keys);
		} catch (IOException e) {
			throw new RecoverableDataAccessException(String.format("Failed to delete subwell data for measurement %d", measId), e);
		}
	}

	/*
	 * Image data storage approach
	 * ***************************
	 * 
	 * Simple object storage:
	 * 		- Key: measId + wellNr + channelNr
	 * 		- One byte array per well image channel
	 * 
	 * Summary:
	 * 		- A few MB per array
	 * 		- A few arrays per well
	 * 		- Thousands of arrays per meas
	 * 		- Thus, several GB per meas
	 */
	
	public byte[] getImageData(long measId, int wellNr, int channelNr) {
		return null;
	}
	
	public Map<Integer, byte[]> getImageData(long measId, int wellNr) {
		return null;
	}
	
	public void deleteImageData(long measId) {
		
	}

}
