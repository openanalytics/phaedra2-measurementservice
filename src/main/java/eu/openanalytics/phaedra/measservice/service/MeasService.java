package eu.openanalytics.phaedra.measservice.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.measservice.model.Measurement;

@Service
public interface MeasService {

	/**
	 * Create a new persistent measurement object.
	 * Note that measurement cannot be updated, the initial information must be complete.
	 * 
	 * @param measInfo The information of the new measurement. The ID must be zero.
	 * @return The input measurement object, with a generated ID added to it.
	 */
	public Measurement createNewMeas(Measurement measInfo);
	
	/**
	 * Add well data to a measurement. Note that this can be done only once:
	 * if a measurement already contains well data, an exception will be thrown.
	 * 
	 * @param measId The ID of the measurement to add well data to.
	 * @param wellData The well data to add to the measurement.
	 */
	public void setMeasWellData(long measId, Map<String, float[]> wellData);
	
	public void setMeasSubWellData(long measId, String column, float[][] subWellData);
	public void setMeasImageData(long measId, String channel, byte[][] imageData);
	
	public Optional<Measurement> findMeasById(long measId);
	
	public boolean measExists(long measId);
	
	/**
	 * Request the deletion of a measurement.
	 * 
	 * @param measId The ID of the measurement to delete.
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
