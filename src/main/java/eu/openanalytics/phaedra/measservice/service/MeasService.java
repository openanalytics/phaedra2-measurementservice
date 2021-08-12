package eu.openanalytics.phaedra.measservice.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.measservice.model.Measurement;

@Service
public interface MeasService {

	/**
	 * Create a new persistent measurement object.
	 * Note that measurement cannot be updated, the initial information must be completed.
	 * 
	 * @param measInfo The information of the new measurement. The id must be zero.
	 * @return The input measurement object, with a generated id added to it.
	 */
	public Measurement createNewMeas(Measurement measInfo);
	
	public void setMeasWellData(long measId, Map<String, float[]> wellData);
	public void setMeasSubWellData(long measId, String column, float[][] subWellData);
	public void setMeasImageData(long measId, String channel, byte[][] imageData);
	
	/**
	 * Finish the creation of a measurement.
	 * After this method has been called, data can no longer be added to a measurement.
	 * 
	 * @param measId The id of the measurement to finalize.
	 */
	public void finalizeCreation(long measId);
	
	public Optional<Measurement> findMeasById(long measId);
	
	public boolean measExists(long measId);
	
	/**
	 * Request the deletion of a measurement.
	 * 
	 * @param measId The id of the measurement to delete.
	 */
	public void deleteMeas(long measId);
	
	/* Well-level data */
	
	public float[] getWellData(long measId, String column);
	public Map<String, float[]> getWellData(long measId);
	
	/* SubWell-level data */
	
	public float[] getSubWellData(long measId, int wellNr, String column);
	public Map<Integer, float[]> getSubWellData(long measId, String column);
	
	/* Image data */
	
	public byte[] getImageData(long measId, int wellNr, int channelNr);
	public Map<Integer, byte[]> getImageData(long measId, int wellNr);
	
}
