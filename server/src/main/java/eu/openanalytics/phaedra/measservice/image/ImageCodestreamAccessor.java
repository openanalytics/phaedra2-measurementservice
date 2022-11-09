package eu.openanalytics.phaedra.measservice.image;

import java.io.IOException;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.openanalytics.phaedra.measservice.service.MeasService;

public class ImageCodestreamAccessor {

	private long measId;
	private int wellNr;
	private String channelId;
	private MeasService measService;
	
	private int chunkSize;
	private byte[][] data;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public ImageCodestreamAccessor(long measId, int wellNr, String channelId, MeasService measService) {
		this.measId = measId;
		this.wellNr = wellNr;
		this.channelId = channelId;
		this.measService = measService;
		
		this.chunkSize = 100000;
		this.data = new byte[100][];
	}
	
	public byte[] getBytes(long offset, int len) throws IOException {
//		logger.info(String.format("Requested bytes %d + %d for meas %d, well %d, channel %s", offset, len, measId, wellNr, channelId));
		
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
		logger.info(String.format("Fetching chunk %d for meas %d, well %d, channel %s", i, measId, wellNr, channelId));
		data[i] = measService.getImageDataPart(measId, wellNr, channelId, i * chunkSize, chunkSize);
	}
}
