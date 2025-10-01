/**
 * Phaedra II
 *
 * Copyright (C) 2016-2025 Open Analytics
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
package eu.openanalytics.phaedra.measservice.repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import eu.openanalytics.phaedra.measservice.repository.dao.MeasObjectStoreDAO;
import eu.openanalytics.phaedra.measservice.repository.dao.MeasWelldataDAO;

@Repository
public class MeasDataRepository {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String PREFIX_SW_DATA = "subwelldata";
	private static final String PREFIX_IMAGE_DATA = "imagedata";

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
	public void setWellData(long measId, String column, float[] data) {
		welldataDAO.saveData(measId, column, data);
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

	public void putSubWellData(long measId, String column, Map<Integer, float[]> data) {
		data.keySet().parallelStream().forEach(i -> {
			putSubWellData(measId, i + 1, column, data.get(i));
		});
	}

	public void putSubWellData(long measId, int wellNr, Map<String, float[]> data) {
		data.keySet().parallelStream().forEach(column -> {
			putSubWellData(measId, wellNr, column, data.get(column));
		});
	}
	
	public void putSubWellData(long measId, int wellNr, String column, float[] data) {
		String key = String.format("%s.%s.%d", PREFIX_SW_DATA, column, wellNr);
		try {
			objectStoreDAO.putMeasObject(measId, key, data);
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to store subwell data for measurement %d, well %d, column %s", measId, wellNr, column), e);
		}
	}

	public float[] getSubWellData(long measId, int wellNr, String column) {
		String key = String.format("%s.%s.%d", PREFIX_SW_DATA, column, wellNr);
		try {
			if (!objectStoreDAO.measObjectExists(measId, key)) return null;
			return (float[]) objectStoreDAO.getMeasObject(measId, key);
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to retrieve subwell data for measurement %d, well %d, column %s", measId, wellNr, column), e);
		}
	}

	public Map<Integer, float[]> getSubWellData(long measId, String column) {
		String prefix = String.format("%s.%s", PREFIX_SW_DATA, column);
		try {
			// Find all available subkeys (expected: 1 key per well)
			String[] keys = objectStoreDAO.listMeasObjects(measId, prefix);
			if (keys.length == 0) return null;

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
		try {
			// Find all available subkeys (expected: 1 key per well per column)
			String[] keys = objectStoreDAO.listMeasObjects(measId, PREFIX_SW_DATA);
			objectStoreDAO.deleteMeasObjects(measId, keys);
		} catch (IOException e) {
			throw new RecoverableDataAccessException(String.format("Failed to delete subwell data for measurement %d", measId), e);
		}
	}

	/*
	 * Image data storage approach
	 * ***************************
	 *
	 * One object per well per channel.
	 *
	 * Key: "imagedata.<wellNr>.<channel>"
	 * Value: byte[]
	 *
	 * Summary:
	 * - A few MB per object
	 * - A few objects per well
	 * - Thousands of objects per meas
	 * - Thus, several GB per meas
	 */

	public void putImageData(long measId, int wellNr, Map<String, byte[]> data) {
		data.keySet().parallelStream().forEach(c -> {
			putImageData(measId, wellNr, c, data.get(c));
		});
	}

	public void putImageData(long measId, int wellNr, String channel, byte[] data) {
		String key = String.format("%s.%d.%s", PREFIX_IMAGE_DATA, wellNr, channel);
		try {
			objectStoreDAO.putMeasObjectRaw(measId, key, data);
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to store image data for measurement %d, well %d, channel %s", measId, wellNr, channel), e);
		}
	}

	public long getImageDataSize(long measId, int wellNr, String channel) {
		String key = String.format("%s.%d.%s", PREFIX_IMAGE_DATA, wellNr, channel);
		try {
			if (!objectStoreDAO.measObjectExists(measId, key)) return -1;
			return objectStoreDAO.getMeasObjectSize(measId, key);
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to retrieve image data for measurement %d, well %d, channel %s", measId, wellNr, channel), e);
		}
	}

	public byte[] getImageData(long measId, int wellNr, String channel) {
		String key = String.format("%s.%d.%s", PREFIX_IMAGE_DATA, wellNr, channel);
		try {
			if (!objectStoreDAO.measObjectExists(measId, key)) return null;
			return objectStoreDAO.getMeasObjectRaw(measId, key);
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to retrieve image data for measurement %d, well %d, channel %s", measId, wellNr, channel), e);
		}
	}

	public byte[] getImageData(long measId, int wellNr, String channel, long offset, int len) {
		String key = String.format("%s.%d.%s", PREFIX_IMAGE_DATA, wellNr, channel);
		try {
			// Note that this method does NOT perform an existence check, for performance reasons.
			return objectStoreDAO.getMeasObjectRaw(measId, key, offset, len);
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to retrieve image data for measurement %d, well %d, channel %s", measId, wellNr, channel), e);
		}
	}

	public Map<String, byte[]> getImageData(long measId, int wellNr) {
		String prefix = String.format("%s.%d.", PREFIX_IMAGE_DATA, wellNr);
		try {
			// Find all available subkeys (expected: 1 key per channel)
			String[] keys = objectStoreDAO.listMeasObjects(measId, prefix);
			if (keys.length == 0) return null;

			return Arrays.stream(keys).parallel().map(k -> {
				try {
					long start = System.currentTimeMillis();

					String channel = k.substring(k.lastIndexOf('.') + 1);
					byte[] values = objectStoreDAO.getMeasObjectRaw(measId, k);
					long duration = System.currentTimeMillis() - start;
					logger.info("Retrieved image data in {} ms", duration);
					return Pair.of(channel, values);
				} catch (IOException e) {
					throw new RecoverableDataAccessException(
							String.format("Failed to retrieve image data for measurement %d, well %d", measId, wellNr), e);
				}

			}).collect(Collectors.toMap(p -> p.getFirst(), p -> p.getSecond()));
		} catch (IOException e) {
			throw new RecoverableDataAccessException(
					String.format("Failed to retrieve image data for measurement %d, well %s", measId, wellNr), e);
		}
	}

	public void deleteImageData(long measId) {
		try {
			// Find all available subkeys (expected: 1 key per well per column)
			String[] keys = objectStoreDAO.listMeasObjects(measId, PREFIX_IMAGE_DATA);
			objectStoreDAO.deleteMeasObjects(measId, keys);
		} catch (IOException e) {
			throw new RecoverableDataAccessException(String.format("Failed to delete image data for measurement %d", measId), e);
		}
	}

}
