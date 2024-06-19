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
package eu.openanalytics.phaedra.measservice.image;

import java.io.IOException;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.openanalytics.phaedra.measservice.service.MeasService;

/**
 * This class provides access to the bytes of a codestream, which is assumed to be remote i.e. not on the local machine, 
 * and a certain amount of lag (latency) is expected when fetching the bytes.
 * Therefore, this class maintains a cache of byte "chunks", trying to load only enough bytes to satisfy the codec's need
 * for rendering an image at a certain scale or region.
 */
public class ImageCodestreamAccessor {

	private long measId;
	private int wellNr;
	private String channelId;
	private MeasService measService;

	private int chunkSize;
	private byte[][] data;
	private long codestreamSize;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public ImageCodestreamAccessor(long measId, int wellNr, String channelId, MeasService measService) {
		this.measId = measId;
		this.wellNr = wellNr;
		this.channelId = channelId;
		this.measService = measService;

		this.codestreamSize = measService.getImageDataSize(measId, wellNr, channelId);
		
		// Determine an appropriate chunk size that caches enough, but not too many, bytes.
		this.chunkSize = 100000;
		if ((int) codestreamSize / chunkSize > 20) chunkSize *= 2;
		int chunkCount = (int) codestreamSize / chunkSize;
		if (codestreamSize % chunkSize > 0) chunkCount++;
		
		this.data = new byte[chunkCount][];
	}

	public byte[] getBytes(long offset, int len) throws IOException {
//		logger.debug(String.format("Requested bytes %d + %d for meas %d, well %d, channel %s", offset, len, measId, wellNr, channelId));

		byte[] buffer = new byte[len];

		int startChunkIndex = (int) (offset / chunkSize);
		int endChunkIndex = (int) ((offset + len) / chunkSize);
		if (endChunkIndex >= data.length) growDataArray();

		int bufferOffset = 0;
		int remainingLen = len;

		// Fetch any missing chunks, in parallel.
		IntStream.rangeClosed(startChunkIndex, endChunkIndex)
				.filter(i -> data[i] == null)
				.parallel()
				.forEach(i -> fetchChunk(i));

		// Copy the relevant parts of the chunks into the response buffer.
		for (int i = startChunkIndex; i <= endChunkIndex; i++) {
			int posInChunk = (i == startChunkIndex) ? (int) (offset % chunkSize) : 0;
			int lenInChunk = Math.min(remainingLen, data[i].length - posInChunk);

			try {
				System.arraycopy(data[i], posInChunk, buffer, bufferOffset, lenInChunk);
			} catch (Exception e) {
				logger.error(String.format("Image byte copy failed: chunk %d, %d bytes out of %d", i, lenInChunk, data[i].length), e);
			}

			bufferOffset += lenInChunk;
			remainingLen -= lenInChunk;
		}

		return buffer;
	}

	private void growDataArray() {
		byte[][] newData = new byte[data.length * 2][];
		for (int i = 0; i < data.length; i++) {
			newData[i] = data[i];
		}
		data = newData;
	}

	private void fetchChunk(int i) {
		long offset = i * chunkSize;
		int maxBytesToFetch = (int) (codestreamSize - offset);
		if (maxBytesToFetch <= 0) data[i] = new byte[0];
		int len = Math.min(chunkSize, maxBytesToFetch);
		
		long startTime = System.currentTimeMillis();
		data[i] = measService.getImageDataPart(measId, wellNr, channelId, offset, len);
		long durationMs = System.currentTimeMillis() - startTime;
		
		logger.debug(String.format("Remote fetched chunk %d [chunksize=%d, codestreamsize=%d] for meas %d, well %d, channel %s in %d ms", i, chunkSize, codestreamSize, measId, wellNr, channelId, durationMs));
	}
}
