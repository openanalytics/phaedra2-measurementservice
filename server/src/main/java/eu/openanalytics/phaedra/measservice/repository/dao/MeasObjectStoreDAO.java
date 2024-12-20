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
package eu.openanalytics.phaedra.measservice.repository.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import jakarta.annotation.PostConstruct;

@Component
public class MeasObjectStoreDAO {

	@Autowired
	private AmazonS3 s3Client;
	
	private TransferManager transferMgr;
	
	@Value("${meas-service.s3.upload-max-tries:5}")
	private int uploadMaxTries;
	
	@Value("${meas-service.s3.upload-retry-delay:1000}")
	private int uploadRetryDelayMs;
	
	@Value("${meas-service.s3.upload-temp-file-threshold:10000}")
	private int uploadTempFileThreshold;
	
	@Value("${meas-service.s3.threads:100}")
	private int s3Threads;
	
	@Value("${meas-service.s3.bucket-name}")
	private String bucketName;
	
	@Value("${meas-service.s3.enable-sse:false}")
	private boolean enableSSE;
	
	@PostConstruct
	public void init() {
		if (!s3Client.doesBucketExistV2(bucketName)) {
			s3Client.createBucket(bucketName);
		}
		
		transferMgr = TransferManagerBuilder
				.standard()
				.withExecutorFactory(() -> Executors.newFixedThreadPool(s3Threads))
				.withS3Client(s3Client)
				.build();
	}
	
	public String[] listMeasObjects(long measId, String prefix) throws IOException {
		String s3Prefix = makeS3Key(measId, prefix);
		List<S3ObjectSummary> objects = new ArrayList<>();

		String continuationToken = null;
		do {
			ListObjectsV2Request req = new ListObjectsV2Request();
			req.setBucketName(bucketName);
			req.setPrefix(s3Prefix);
			if (continuationToken != null) req.setContinuationToken(continuationToken);
			
			ListObjectsV2Result results = s3Client.listObjectsV2(req);
			objects.addAll(results.getObjectSummaries());
			continuationToken = (results.isTruncated()) ? results.getContinuationToken() : null;
		} while (continuationToken != null);
		
		return objects.stream().map(o -> unmakeS3Key(o.getKey())).toArray(i -> new String[i]);
	}
	
	public boolean measObjectExists(long measId, String key) throws IOException {
		String s3key = makeS3Key(measId, key);
		return s3Client.doesObjectExist(bucketName, s3key);
	}
	
	public long getMeasObjectSize(long measId, String key) throws IOException {
		String s3key = makeS3Key(measId, key);
		return s3Client.getObjectMetadata(bucketName, s3key).getContentLength();
	}
	
	public Object getMeasObject(long measId, String key) throws IOException {
		byte[] bytes = getMeasObjectRaw(measId, key);
		return deserializeObjectFromStream(new ByteArrayInputStream(bytes));
	}

	public byte[] getMeasObjectRaw(long measId, String key) throws IOException {
		return getMeasObjectRaw(measId, key, -1, -1);
	}
	
	public byte[] getMeasObjectRaw(long measId, String key, long offset, int len) throws IOException {
		String s3key = makeS3Key(measId, key);
		GetObjectRequest request = new GetObjectRequest(bucketName, s3key);
		if (len > 1) {
			request.setRange(offset, offset + len - 1);
		}
		try (
			S3Object object = s3Client.getObject(request);
			S3ObjectInputStream input = object.getObjectContent();
		) {
			return StreamUtils.copyToByteArray(input);
		} catch (AmazonS3Exception e) {
			throw new IOException(e);
		}
	}
	
	public void putMeasObject(long measId, String key, Object value) throws IOException {
		putMeasObjectRaw(measId, key, serializeObject(value));
	}
	
	public void putMeasObjectRaw(long measId, String key, byte[] value) throws IOException {
		String s3key = makeS3Key(measId, key);
		
		boolean useTempFile = value.length > uploadTempFileThreshold;
		File tempFile = null;
		if (useTempFile) {
			tempFile = File.createTempFile("meas-data", null);
			try (OutputStream out = new FileOutputStream(tempFile)) {
				StreamUtils.copy(value, out);
			}
		}
		
		// By using a file instead of a stream, TransferManager can optimize the upload: multipart parallel uploads with auto-retrying.
		// Still, additional retrying is applied here to deal with 400 (Request Timeout) errors which are not retried by the TransferManager.
		Exception caughtException = null;
		try {
			int currentTry = 1;
			while (currentTry <= uploadMaxTries) {
				try {
					Upload upload = null;
					if (useTempFile) {
						upload = transferMgr.upload(bucketName, s3key, tempFile);
					} else {
						ObjectMetadata mtdt = new ObjectMetadata();
						mtdt.setContentLength(value.length);
						upload = transferMgr.upload(bucketName, s3key, new ByteArrayInputStream(value), mtdt);
					}
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
			if (useTempFile) tempFile.delete();
		}
		throw new IOException(String.format("Failed to upload data to S3 for meas %d and key %s", measId, key), caughtException);		
	}

	public void deleteMeasObject(long measId, String key) throws IOException {
		String s3key = makeS3Key(measId, key);
		s3Client.deleteObject(bucketName, s3key);
	}

	public void deleteMeasObjects(long measId, String[] keys) throws IOException {
		String[] s3Keys = Arrays.stream(keys).map(k -> makeS3Key(measId, k)).toArray(i -> new String[i]);
		// Split the keys into groups of 1000 (the max of DeleteObjectsRequest).
		List<String[]> keySets = splitArray(s3Keys, 1000);
		keySets.stream().parallel().forEach(ks -> {
			DeleteObjectsRequest req = new DeleteObjectsRequest(bucketName).withKeys(ks);
			s3Client.deleteObjects(req);
		});
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
	
	private String unmakeS3Key(String s3Key) {
		return s3Key.substring(s3Key.indexOf('/') + 1);
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

	private <T> List<T[]> splitArray(T[] items, int maxSubArraySize) {
		List<T[]> result = new ArrayList<T[]>();
		if (items == null || items.length == 0) {
			return result;
		}

		int from = 0;
		int to = 0;
		int slicedItems = 0;
		while (slicedItems < items.length) {
			to = from + Math.min(maxSubArraySize, items.length - to);
			T[] slice = Arrays.copyOfRange(items, from, to);
			result.add(slice);
			slicedItems += slice.length;
			from = to;
		}
		return result;
	}
}
