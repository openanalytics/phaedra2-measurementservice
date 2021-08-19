package eu.openanalytics.phaedra.measservice.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.openanalytics.phaedra.measservice.model.Measurement;
import eu.openanalytics.phaedra.measservice.repository.MeasDataRepository;
import eu.openanalytics.phaedra.measservice.repository.MeasRepository;

@Component
public class MeasServiceImpl implements MeasService {

	@Autowired
	private MeasRepository measRepo;
	
	@Autowired
	private MeasDataRepository measDataRepo;
	
	@Override
	public Measurement createNewMeas(Measurement measInfo) {
		measInfo.setCreatedOn(new Date());
		validateMeas(measInfo, true);
		return measRepo.save(measInfo);
	}

	@Override
	public Optional<Measurement> findMeasById(long measId) {
		return measRepo.findById(measId);
	}

	@Override
	public List<Measurement> findMeasByCreatedOnRange(Date date1, Date date2) {
		return measRepo.findByCreatedOnRange(date1, date2);
	}
	
	@Override
	public boolean measExists(long measId) {
		return measRepo.existsById(measId);
	}

	@Override
	public void deleteMeas(long measId) {
		//TODO Deletion should be queued in an async fashion.
		measRepo.deleteById(measId);
		measDataRepo.deleteWellData(measId);
		measDataRepo.deleteSubWellData(measId);
		measDataRepo.deleteImageData(measId);
	}

	@Override
	public void setMeasWellData(long measId, Map<String, float[]> wellData) {
		Measurement meas = findMeasById(measId).orElse(null);
		
		if (meas == null) 
			throw new IllegalArgumentException(String.format("Cannot save welldata: measurement with ID %d does not exist", measId));
		if (!ArrayUtils.isEmpty(meas.getWellColumns())) 
			throw new IllegalArgumentException(String.format("Cannot save welldata: measurement with ID %d already contains well data", measId));
		
		int wellCount = meas.getRows() * meas.getColumns();
		for (String column: wellData.keySet()) {
			float[] values = wellData.get(column);
			int valueCount = values.length;
			if (valueCount != wellCount)
				throw new IllegalArgumentException(String.format(
						"Cannot save welldata for measurement %d: column %s has an unexpected count (expected: %d, actual: %d)",
						measId, column, wellCount, valueCount));
		}
		
		measDataRepo.setWellData(measId, wellData);
		
		String[] wellColumns = wellData.keySet().stream().sorted().toArray(i -> new String[i]);
		meas.setWellColumns(wellColumns);
		measRepo.save(meas);
	}
	
	@Override
	public float[] getWellData(long measId, String column) {
		if (!measExists(measId)) return null;
		return measDataRepo.getWellData(measId, column);
	}

	@Override
	public Map<String, float[]> getWellData(long measId) {
		return measDataRepo.getWellData(measId);
	}

	@Override
	public void setMeasSubWellData(long measId, String column, Map<Integer, float[]> subWellData) {
		Measurement meas = findMeasById(measId).orElse(null);
		
		if (meas == null) {
			throw new IllegalArgumentException(String.format("Cannot save subwelldata: measurement with ID %d does not exist", measId));
		}
		if (ArrayUtils.contains(meas.getSubWellColumns(), column)) {
			throw new IllegalArgumentException(
					String.format("Cannot save subwelldata: measurement with ID %d already contains subwelldata for column %s", measId, column));
		}
		if (subWellData == null || subWellData.isEmpty()) {
			throw new IllegalArgumentException(String.format("Cannot save subwelldata: no data provided"));
		}
		
		int wellCount = meas.getRows() * meas.getColumns();
		if (subWellData.size() != wellCount) {
			throw new IllegalArgumentException(
					String.format("Cannot save subwelldata: data array has unexpected size (expected: %d, actual: %d)", wellCount, subWellData.size()));
		}
		
		measDataRepo.putSubWellData(measId, column, subWellData);

		// Register the new column in the measurement.
		String[] subWellColumns = ArrayUtils.add(meas.getSubWellColumns(), column);
		subWellColumns = Arrays.stream(subWellColumns).sorted().toArray(i -> new String[i]);
		meas.setSubWellColumns(subWellColumns);
		measRepo.save(meas);
	}

	@Override
	public float[] getSubWellData(long measId, int wellNr, String column) {
		if (!measExists(measId)) return null;
		return measDataRepo.getSubWellData(measId, wellNr, column);
	}

	@Override
	public Map<Integer, float[]> getSubWellData(long measId, String column) {
		if (!measExists(measId)) return null;
		return measDataRepo.getSubWellData(measId, column);
	}

	@Override
	public void setMeasImageData(long measId, int wellNr, Map<String, byte[]> imageData) {
		Measurement meas = findMeasById(measId).orElse(null);
		
		if (meas == null) {
			throw new IllegalArgumentException(String.format("Cannot save image data: measurement with ID %d does not exist", measId));
		}
		if (imageData == null || imageData.isEmpty()) {
			throw new IllegalArgumentException(String.format("Cannot save image data: no data provided"));
		}
		
		String[] channelNames = imageData.keySet().stream().sorted().toArray(i -> new String[i]);
		if (meas.getImageChannels() != null && !Arrays.equals(channelNames, meas.getImageChannels())) {
			throw new IllegalArgumentException("Cannot save image data: provided channel names do not match the measurement channel names");
		}
		
		measDataRepo.putImageData(measId, wellNr, imageData);
		
		if (meas.getImageChannels() == null) {
			meas.setImageChannels(channelNames);
			measRepo.save(meas);
		}
	}
	
	@Override
	public byte[] getImageData(long measId, int wellNr, String channel) {
		if (!measExists(measId)) return null;
		return measDataRepo.getImageData(measId, wellNr, channel);
	}

	@Override
	public Map<String, byte[]> getImageData(long measId, int wellNr) {
		if (!measExists(measId)) return null;
		return measDataRepo.getImageData(measId, wellNr);
	}

	/**
	 * Non-public
	 * **********
	 */

	private void validateMeas(Measurement meas, boolean isNewMeas) {
		if (isNewMeas) Assert.isTrue(meas.getId() == 0, "New measurement must have ID equal to 0");
		Assert.hasText(meas.getName(), "Measurement name cannot be empty");
		Assert.hasText(meas.getBarcode(), "Measurement barcode cannot be empty");
		Assert.isTrue(meas.getRows() > 0, "Measurement must have at least 1 row");
		Assert.isTrue(meas.getColumns() > 0, "Measurement must have at least 1 column");
		Assert.hasText(meas.getCreatedBy(), "Measurement creator cannot be empty");
		Assert.notNull(meas.getCreatedOn(), "Measurement creation date cannot be null");
	}
}
