package eu.openanalytics.phaedra.measservice.repository.dao;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

@Component
public class MeasObjectStoreDAO {

	@Autowired
	private AmazonS3Client s3Client;
	
	private TransferManager transferMgr;
	
	@Value("${meas-service.s3.upload-max-tries:5}")
	private int uploadMaxTries;
	
	@Value("${meas-service.s3.upload-retry-delay:1000}")
	private int uploadRetryDelayMs;
	
	@Value("${meas-service.s3.bucket-name}")
	private String bucketName;
	
	@Value("${meas-service.s3.enable-sse:false}")
	private boolean enableSSE;
	
	@PostConstruct
	public void init() {
		transferMgr = TransferManagerBuilder
				.standard()
				.withS3Client(s3Client)
				.build();
	}
	
	public Object getMeasObject(long measId, String key) throws IOException {
		String s3key = makeS3Key(measId, key);
		GetObjectRequest request = new GetObjectRequest(bucketName, s3key);
		try (
			S3Object object = s3Client.getObject(request);
			S3ObjectInputStream input = object.getObjectContent();
		) {
			return deserializeObjectFromStream(input);
		}
	}

	public void putMeasObject(long measId, String key, Object value) throws IOException {
		String s3key = makeS3Key(measId, key);
		
		// Serialize the object and write it into a temporary file.
		byte[] objectBytes = serializeObject(value);
		File tempFile = File.createTempFile("meas-data", null);
		try (OutputStream out = new FileOutputStream(tempFile)) {
			StreamUtils.copy(objectBytes, out);
		}
		
		// By using a file instead of a stream, TransferManager can optimize the upload: multipart parallel uploads with auto-retrying.
		// Still, additional retrying is applied here to deal with 400 (Request Timeout) errors which are not retried by the TransferManager.
		Exception caughtException = null;
		try {
			int currentTry = 1;
			while (currentTry <= uploadMaxTries) {
				try {
					Upload upload = transferMgr.upload(bucketName, s3key, tempFile);
					upload.waitForCompletion();
					return;
				} catch (Exception e) {
					caughtException = e;
					if (uploadRetryDelayMs > 0) {
						try { Thread.sleep(uploadRetryDelayMs); } catch (InterruptedException ie) {}
					}
					currentTry++;
				}
			}
		} finally {
			tempFile.delete();
		}
		throw new IOException(String.format("Failed to upload data to S3 for meas %d and key %s", measId, key), caughtException);		
	}

	public void deleteMeasObject(long measId, String key) throws IOException {
		String s3key = makeS3Key(measId, key);
		s3Client.deleteObject(bucketName, s3key);
	}

	/**
	 * Non-public
	 * **********
	 */
	
	private String makeS3Key(long measId, String objectKey) {
		if (objectKey == null) throw new IllegalArgumentException("Null object key specified");
		StringBuilder sb = new StringBuilder();
		sb.append(reverse(measId));
		sb.append("/");
		sb.append(objectKey);
		return sb.toString();
	}
	
	private String reverse(long measId) {
		StringBuilder sb = new StringBuilder();
		sb.append(measId);
		sb.reverse();
		return sb.toString();
	}
	
	private byte[] serializeObject(Object o) throws IOException {
		byte[] bytes = null;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(o);
			oos.flush();
			bytes = os.toByteArray();
		}
		return bytes;
	}
	
	private Object deserializeObjectFromStream(InputStream input) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(input);
		Object object = null;
		try {
			object = ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return object;
	}
}
