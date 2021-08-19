package eu.openanalytics.phaedra.measservice.service;

import java.util.Date;
import java.util.List;
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
	 * Find a measurement using its ID.
	 * 
	 * @param measId The ID of the measurement to look for.
	 * @return An Optional measurement, empty if no matching measurement was found.
	 */
	public Optional<Measurement> findMeasById(long measId);
	
	/**
	 * Find a collection of measurements using a Date range.
	 * 
	 * @param date1 The start date to search for.
	 * @param date2 The end date to search for.
	 * @return A collection of all matching measurements, possibly empty.
	 */
	public List<Measurement> findMeasByCreatedOnRange(Date date1, Date date2);
	
	/**
	 * Check whether a measurement with the given ID exists.
	 * 
	 * @param measId The ID of the measurement to look for.
	 * @return True if a measurement exists for the given ID.
	 */
	public boolean measExists(long measId);
	
	/**
	 * Request the permanent deletion of a measurement, including all of its data.
	 * 
	 * @param measId The ID of the measurement to delete.
	 */
	public void deleteMeas(long measId);
	
	/**
	 * Add well data to a measurement. Note that this can be done only once:
	 * if a measurement already contains well data, an exception will be thrown.
	 * 
	 * @param measId The ID of the measurement to add well data to.
	 * @param wellData The well data to add to the measurement.
	 */
	public void setMeasWellData(long measId, Map<String, float[]> wellData);
	
	/**
	 * Retrieve the welldata for a measurement for a given column name.
	 * 
	 * @param measId The ID of the measurement to get welldata for.
	 * @param column The name of the column to get welldata for.
	 * @return The welldata, may be null.
	 */
	public float[] getWellData(long measId, String column);
	
	/**
	 * Retrieve all the welldata for a measurement.
	 * 
	 * @param measId The ID of the measurement to get welldata for.
	 * @return The map of welldata, may be empty.
	 */
	public Map<String, float[]> getWellData(long measId);
	
	/* SubWell-level data */
	
	public void setMeasSubWellData(long measId, String column, float[][] subWellData);
	
	public float[] getSubWellData(long measId, int wellNr, String column);
	public Map<Integer, float[]> getSubWellData(long measId, String column);
	
	/* Image data */
	
	public void setMeasImageData(long measId, String channel, byte[][] imageData);
	
	public byte[] getImageData(long measId, int wellNr, int channelNr);
	public Map<Integer, byte[]> getImageData(long measId, int wellNr);
	
}
