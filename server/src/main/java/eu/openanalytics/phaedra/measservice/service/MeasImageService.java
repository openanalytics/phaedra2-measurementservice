/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.imaging.jp2k.ICodestreamSource;
import eu.openanalytics.phaedra.imaging.jp2k.openjpeg.OpenJPEGLibLoader;
import eu.openanalytics.phaedra.imaging.jp2k.openjpeg.source.ByteArraySource;
import eu.openanalytics.phaedra.imaging.render.ImageRenderConfig;
import eu.openanalytics.phaedra.imaging.render.ImageRenderService;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;

@Service
public class MeasImageService {

	static {
		OpenJPEGLibLoader.load();
	}
	
	@Autowired
	private MeasService measService;
	
	@Autowired
	private ImageRenderConfigService renderConfigService;
	
	public byte[] renderImage(long measId, int wellNr, String channel, Long renderConfigId) throws IOException {

		MeasurementDTO meas = measService.findMeasById(measId).orElse(null);
		if (meas == null) return null;
		
		byte[] codestreamData = measService.getImageData(measId, wellNr, channel);
		if (codestreamData == null) return null;
		
		ICodestreamSource[] sources = new ICodestreamSource[] {
			new ByteArraySource(codestreamData)
		};
		
		ImageRenderConfig cfg = obtainImageRenderConfig(Collections.singletonList(channel), renderConfigId);
		return renderService().renderImage(sources, cfg);
	}

	public byte[] renderImage(long measId, int wellNr, Long renderConfigId) throws IOException {

		MeasurementDTO meas = measService.findMeasById(measId).orElse(null);
		if (meas == null) return null;
		
		Map<String, byte[]> codestreamDatas = measService.getImageData(measId, wellNr);
		if (codestreamDatas == null || codestreamDatas.isEmpty()) return null;
		
		List<ICodestreamSource> sources = new ArrayList<>();
		List<String> availableChannels = new ArrayList<>();
		
		String[] channels = meas.getImageChannels();
		for (int i = 0; i < channels.length; i++) {
			String channel = channels[i];
			byte[] codestreamData = codestreamDatas.get(channel);
			if (codestreamData == null) continue;
			availableChannels.add(channel);
			sources.add(new ByteArraySource(codestreamData));
		}
		
		ImageRenderConfig cfg = obtainImageRenderConfig(availableChannels, renderConfigId);
		return renderService().renderImage(sources.toArray(i -> new ICodestreamSource[i]), cfg);
	}
	
	@Bean
	private ImageRenderService renderService() {
		return new ImageRenderService();
	}
	
	private ImageRenderConfig obtainImageRenderConfig(List<String> channels, Long configId) {
		if (configId == null) {
			return new ImageRenderConfig(channels.stream().toArray(i -> new String[i]));
		} else {
			return renderConfigService.getConfigById(configId).map(c -> c.getConfig())
					.orElseThrow(() -> new IllegalArgumentException("No image render config found with ID " + configId));
		}
	}
}
