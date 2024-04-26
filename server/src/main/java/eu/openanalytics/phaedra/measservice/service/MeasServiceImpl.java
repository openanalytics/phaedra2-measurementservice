/**
 * Phaedra II
 *
 * Copyright (C) 2016-2024 Open Analytics
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
package eu.openanalytics.phaedra.measservice.service;

import com.google.common.primitives.Longs;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.exception.MeasurementNotFoundException;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import eu.openanalytics.phaedra.measservice.repository.MeasDataRepository;
import eu.openanalytics.phaedra.measservice.repository.MeasRepository;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Component
public class MeasServiceImpl implements MeasService {

	private final MeasRepository measRepo;
	private final MeasDataRepository measDataRepo;
	private final ModelMapper modelMapper;
	private final IAuthorizationService authService;

	public MeasServiceImpl(MeasRepository measRepo, MeasDataRepository measDataRepo, ModelMapper modelMapper, IAuthorizationService authService) {
		this.measRepo = measRepo;
		this.measDataRepo = measDataRepo;
		this.modelMapper = modelMapper;
		this.authService = authService;
	}

	@Override
	public Measurement createNewMeas(Measurement measurement) {
		measurement.setCreatedBy(authService.getCurrentPrincipalName());
		measurement.setCreatedOn(new Date());
		validateMeas(measurement, true);
		return measRepo.save(measurement);
	}

	@Override
	public Measurement updateMeasurement(MeasurementDTO measurementDTO) throws MeasurementNotFoundException {
		Optional<Measurement> result = measRepo.findById(measurementDTO.getId());
		if (result.isPresent()) {
			Measurement updated = modelMapper.map(result.get(), measurementDTO);
			updated.setUpdatedBy(authService.getCurrentPrincipalName());
			updated.setUpdatedOn(new Date());
			return measRepo.save(updated);
		}
		throw new MeasurementNotFoundException(String.format("Measurement %d not found", measurementDTO.getId()));
	}

	@Override
	public List<MeasurementDTO> getAllMeasurements() {
		List<Measurement> result = (List<Measurement>) measRepo.findAll();
		return result.stream().map(modelMapper::map).toList();
	}

	@Override
	public Optional<MeasurementDTO> findMeasById(long measId) {
		Optional<Measurement> measurement = measRepo.findById(measId);
		return measurement.map(modelMapper::map);
	}

	@Override
	public List<MeasurementDTO> getMeasurementsByIds(List<Long> measIds) {
		List<Measurement> result = measRepo.findAllByIds(Longs.toArray(measIds));
		return result.stream().map(modelMapper::map).toList();
	}

	@Override
	public List<MeasurementDTO> findMeasByCreatedOnRange(Date from, Date to) {
		List<Measurement> result = measRepo.findByCreatedOnRange(from, to);
		return result.stream().map(modelMapper::map).toList();
	}

	@Override
	public boolean measExists(long measId) {
		return measRepo.existsById(measId);
	}

	@Override
	public void deleteMeas(long measId) {
		measRepo.deleteById(measId);

		// The bulk delete operation is performed async.
		//TODO Implement deletion in a separate service with queueing and support for restarts
		ForkJoinPool.commonPool().submit(() -> {
			measDataRepo.deleteWellData(measId);
			measDataRepo.deleteSubWellData(measId);
			measDataRepo.deleteImageData(measId);
		});
	}

	@Override
	public void setMeasWellData(long measId, Map<String, float[]> wellData) {
		Measurement meas = measRepo.findById(measId).orElse(null);

		if (meas == null)
			throw new IllegalArgumentException(String.format("Cannot save welldata: measurement with ID %d does not exist", measId));

		int wellCount = meas.getRows() * meas.getColumns();
		for (Map.Entry<String, float[]> entry : wellData.entrySet()) {
			int valueCount = entry.getValue().length;
			if (valueCount != wellCount)
				throw new IllegalArgumentException(String.format(
						"Cannot save welldata for measurement %d: column %s has an unexpected count (expected: %d, actual: %d)",
						measId, entry.getKey(), wellCount, valueCount));
		}

		measDataRepo.setWellData(measId, wellData);

		String[] wellColumns = wellData.keySet().stream().sorted().toArray(String[]::new);
		meas.setWellColumns(wellColumns);
		measRepo.save(meas);
	}


	@Override
	public void setMeasWellData(long measId, String column, float[] data) {
		Measurement meas = measRepo.findById(measId).orElse(null);

		if (meas == null)
			throw new IllegalArgumentException(String.format("Cannot save welldata: measurement with ID %d does not exist", measId));

		int wellCount = meas.getRows() * meas.getColumns();
		int valueCount = data.length;
		if (valueCount != wellCount)
			throw new IllegalArgumentException(String.format(
					"Cannot save welldata for measurement %d: column %s has an unexpected count (expected: %d, actual: %d)",
					measId, column, wellCount, valueCount));

		measDataRepo.setWellData(measId, column, data);
	}

	@Override
	public float[] getWellData(long measId, String column) {
		if (!measExists(measId)) return new float[0];
		return measDataRepo.getWellData(measId, column);
	}

	@Override
	public Map<String, float[]> getWellData(long measId) {
		if (!measExists(measId)) return Collections.emptyMap();
		return measDataRepo.getWellData(measId);
	}

	@Override
	public List<String> getAllUniqueWellDataColumns() {
		return measRepo.findDistinctWellColumns();
	}

	@Override
	public void setMeasSubWellData(long measId, String column, Map<Integer, float[]> subWellData) {
		Measurement meas = measRepo.findById(measId).orElse(null);

		if (meas == null) {
			throw new IllegalArgumentException(String.format("Cannot save subwelldata: measurement with ID %d does not exist", measId));
		}
		if (ArrayUtils.contains(meas.getSubWellColumns(), column)) {
			throw new IllegalArgumentException(
					String.format("Cannot save subwelldata: measurement with ID %d already contains subwelldata for column %s", measId, column));
		}
		if (subWellData == null || subWellData.isEmpty()) {
			throw new IllegalArgumentException("Cannot save subwelldata: no data provided");
		}

		int wellCount = meas.getRows() * meas.getColumns();
		if (subWellData.size() != wellCount) {
			throw new IllegalArgumentException(
					String.format("Cannot save subwelldata: data array has unexpected size (expected: %d, actual: %d)", wellCount, subWellData.size()));
		}

		measDataRepo.putSubWellData(measId, column, subWellData);

		// Register the new column in the measurement.
		String[] subWellColumns = ArrayUtils.add(meas.getSubWellColumns(), column);
		subWellColumns = Arrays.stream(subWellColumns).sorted().toArray(String[]::new);
		meas.setSubWellColumns(subWellColumns);
		measRepo.save(meas);
	}

	@Override
	public void setMeasSubWellData(long measId, int wellNr, String column, float[] subWellData) {
		Measurement meas = measRepo.findById(measId).orElse(null);

		if (meas == null) {
			throw new IllegalArgumentException(String.format("Cannot save subwelldata: measurement with ID %d does not exist", measId));
		}
		if (subWellData == null || ArrayUtils.isEmpty(subWellData)) {
			throw new IllegalArgumentException("Cannot save subwelldata: no data provided");
		}

		measDataRepo.putSubWellData(measId, wellNr, column, subWellData);
	}

	@Override
	public Map<String, float[]> getSubWellData(long measId, int wellNr) {
		Optional<Measurement> measurement = measRepo.findById(measId);
		if (measurement.isEmpty()) return Collections.emptyMap();

		return Arrays.stream(measurement.get().getSubWellColumns())
				.collect(HashMap::new, (map, column) -> {
					float[] values = measDataRepo.getSubWellData(measId, wellNr, column);
					map.put(column, values);
				}, HashMap::putAll);
	}

	@Override
	public float[] getSubWellData(long measId, int wellNr, String column) {
		if (!measExists(measId)) return new float[0];
		return measDataRepo.getSubWellData(measId, wellNr, column);
	}

	public Map<String, float[]> getSubWellData(long measId, int wellNr, List<String> columns) {
		if (!measExists(measId)) return Collections.emptyMap();
		Map<String, float[]> subWellData = new HashMap<>();
		columns.forEach(column -> subWellData.put(column, measDataRepo.getSubWellData(measId, wellNr, column)));
		return subWellData;
	}

	@Override
	public Map<Integer, float[]> getSubWellData(long measId, String column) {
		if (!measExists(measId)) return Collections.emptyMap();
		return measDataRepo.getSubWellData(measId, column);
	}

	@Override
	public List<String> getAllUniqueSubWellDataColumns() {
		return measRepo.findDistinctSubWellColumns();
	}

	@Override
	public void setMeasImageData(long measId, int wellNr, Map<String, byte[]> imageData) {
		Measurement meas = measRepo.findById(measId).orElse(null);

		if (meas == null) {
			throw new IllegalArgumentException(String.format("Cannot save image data: measurement with ID %d does not exist", measId));
		}
		if (imageData == null || imageData.isEmpty()) {
			throw new IllegalArgumentException("Cannot save image data: no data provided");
		}

		String[] channelNames = imageData.keySet().stream().sorted().toArray(String[]::new);
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
	public void setMeasImageData(long measId, int wellNr, String channelId, byte[] imageData) {
		Measurement meas = measRepo.findById(measId).orElse(null);

		if (meas == null) {
			throw new IllegalArgumentException(String.format("Cannot save image data: measurement with ID %d does not exist", measId));
		}
		if (imageData == null || imageData.length == 0) {
			throw new IllegalArgumentException("Cannot save image data: no data provided");
		}
		if (channelId == null || channelId.trim().isEmpty()) {
			throw new IllegalArgumentException("Cannot save image data: no channel id provided");
		}

		measDataRepo.putImageData(measId, wellNr, channelId, imageData);

		String[] channelNames = meas.getImageChannels();
		if (channelNames == null) {
			channelNames = new String[] { channelId };
		} else if (!ArrayUtils.contains(channelNames, channelId)) {
			int nrChannels = channelNames.length;
			channelNames = new String[nrChannels + 1];
			System.arraycopy(meas.getImageChannels(), 0, channelNames, 0, nrChannels);
			channelNames[nrChannels] = channelId;
		} else {
			return;
		}

		meas.setImageChannels(channelNames);
		measRepo.save(meas);
	}

	@Override
	public long getImageDataSize(long measId, int wellNr, String channel) {
		return measDataRepo.getImageDataSize(measId, wellNr, channel);
	}

	@Override
	public byte[] getImageData(long measId, int wellNr, String channel) {
		if (!measExists(measId)) return new byte[0];
		return measDataRepo.getImageData(measId, wellNr, channel);
	}

	@Override
	public byte[] getImageDataPart(long measId, int wellNr, String channel, long offset, int len) {
		// Note that this method does NOT perform an existence check, for performance reasons.
		return measDataRepo.getImageData(measId, wellNr, channel, offset, len);
	}

	@Override
	public Map<String, byte[]> getImageData(long measId, int wellNr) {
		if (!measExists(measId)) return Collections.emptyMap();
		return measDataRepo.getImageData(measId, wellNr);
	}

	/**
	 * Non-public
	 * **********
	 */

	private void validateMeas(Measurement meas, boolean isNewMeas) {
		if (isNewMeas) Assert.isTrue(meas.getId() == null, "New measurement must have ID equal to 0");
		Assert.hasText(meas.getName(), "Measurement name cannot be empty");
		Assert.hasText(meas.getBarcode(), "Measurement barcode cannot be empty");
		Assert.hasText(meas.getCreatedBy(), "Measurement creator cannot be empty");
		Assert.notNull(meas.getCreatedOn(), "Measurement creation date cannot be null");
	}
}
