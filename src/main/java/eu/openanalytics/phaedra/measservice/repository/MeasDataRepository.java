package eu.openanalytics.phaedra.measservice.repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Repository;

import eu.openanalytics.phaedra.measservice.repository.dao.MeasObjectStoreDAO;
import eu.openanalytics.phaedra.measservice.repository.dao.MeasWelldataDAO;

@Repository
public class MeasDataRepository {

	@Autowired
	private MeasWelldataDAO welldataDAO;
	
	@Autowired
	private MeasObjectStoreDAO objectStoreDAO;
	
	/*
	 * Well data storage approach
	 * **************************
	 * 
	 * Wide column storage:
	 * 		- Row key: measId
	 * 		- Column key: featureName
	 * 		- Column value: float[]
	 * 
	 * Summary:
	 * - One row per meas
	 * - Thousands of rows
	 * - Thousands of columns
	 * - Several KB per value
	 * - Thus, several MB per row
	 * - Thus, several MB per plate
	 */
	
	public float[] getWellData(long measId, String column) {
		return welldataDAO.getData(measId, column);
	}
	
	public Map<String, float[]> getWellData(long measId) {
		return welldataDAO.getData(measId);
	}
	
	public void deleteWellData(long measId) {
		welldataDAO.deleteData(measId);
	}
	
	/*
	 * Subwell data storage approach
	 * *****************************
	 * 
	 * Wide column storage:
	 * 		- Row key: measId + wellNr
	 * 		- Column key: featureName
	 * 		- Column value: float[]
	 * 
	 * Summary:
	 * - Thousands of rows per meas
	 * - Millions of rows
	 * - Thousands of columns
	 * - Several KB per value
	 * - Thus, several MB per row
	 * - Thus, several GB per meas
	 */
	
	public float[] getSubWellData(long measId, int wellNr, String column) {
		Map<Integer, float[]> swData = getSubWellData(measId, column);
		if (swData == null) throw new IllegalArgumentException(
				String.format("Measurement with ID %d does not have subwell data for well nr %d and column %s", measId, wellNr, column));
		return swData.get(wellNr);
	}
	
	public Map<Integer, float[]> getSubWellData(long measId, String column) {
		String key = String.format("subwelldata.%s", column);
		try {
			float[][] swData = (float[][]) objectStoreDAO.getMeasObject(measId, key);
			Map<Integer, float[]> swDataMap = new HashMap<>();
			for (int i = 0; i < swData.length; i++) {
				swDataMap.put(i + 1, swData[i]);
			}
			return swDataMap;
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to retrieve subwell data for measurement %d, column %s", measId, column), e);
		}
	}
	
	public void deleteSubWellData(long measId) {
		//TODO
//		objectStoreDAO.deleteMeasObject(measId, null);
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
