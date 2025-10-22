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
package eu.openanalytics.phaedra.measservice.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.imaging.jp2k.ICodestreamSource;
import eu.openanalytics.phaedra.imaging.jp2k.ICodestreamSourceDescriptor;
import eu.openanalytics.phaedra.imaging.render.ImageRenderConfig;
import eu.openanalytics.phaedra.imaging.render.ImageRenderConfig.ChannelRenderConfig;
import eu.openanalytics.phaedra.imaging.render.ImageRenderService;
import eu.openanalytics.phaedra.imaging.util.ImageRenderConfigUtils;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.image.ImageCodestreamAccessor;
import eu.openanalytics.phaedra.measservice.image.ImageCodestreamAccessorCache;

@Service
public class MeasImageService {

	@Autowired
	private MeasService measService;

	@Autowired
	private ImageRenderConfigService renderConfigService;

	@Autowired
	private ImageCodestreamAccessorCache codestreamAccessorCache;

	@Autowired
	private ImageRenderService renderService;

	static Logger logger = org.slf4j.LoggerFactory.getLogger(MeasImageService.class);

	@CacheEvict(value = "meas_image", allEntries = true)
	public void clearCache() {
		logger.info("clearing cache");
	}

//	@Cacheable(value = "meas_image", key = "{#measId, #wellNr, #channel, #renderConfigId, #renderConfig.hashCode()}" )
  @Cacheable(value = "meas_image",
    key = "T(java.lang.String).format('%d-%d-%s-%s', #measId, #wellNr, #channel, #renderConfigId ?: 'none')",
    condition = "#result != null && #result.length < 10485760")
	public byte[] renderImage(long measId, int wellNr, String channel, Long renderConfigId, ImageRenderConfig renderConfig) throws IOException {

		MeasurementDTO meas = measService.findMeasById(measId).orElse(null);
		if (meas == null) return null;

		ICodestreamSourceDescriptor source = createCodestreamSourceDescriptor(measId, wellNr, channel);
		if (source == null) return null;

		ImageRenderConfig cfg = generateFullRenderConfig(Collections.singletonList(channel), renderConfigId, renderConfig);
		return renderService.renderImage(new ICodestreamSourceDescriptor[] { source }, cfg);
	}

//	@Cacheable(value = "meas_image", key = "{#measId, #wellNr, #channels.hashCode(), #renderConfigId, #renderConfig.hashCode()}")
  @Cacheable(value = "meas_image",
    key = "T(java.lang.String).format('%d-%d-%s-%s', #measId, #wellNr, #channels.hashCode(), #renderConfigId ?: 'none')",
    condition = "#result != null && #result.length < 10485760")
	public byte[] renderImage(long measId, int wellNr, List<String> channels, Long renderConfigId, ImageRenderConfig renderConfig) throws IOException {

		MeasurementDTO meas = measService.findMeasById(measId).orElse(null);
		if (meas == null) return null;

		List<ICodestreamSourceDescriptor> sources = new ArrayList<>();
		List<String> availableChannels = new ArrayList<>();

		for (String channel: channels) {
			ICodestreamSourceDescriptor source = createCodestreamSourceDescriptor(measId, wellNr, channel);
			if (source == null) continue;
			availableChannels.add(channel);
			sources.add(source);
		}
		if (availableChannels.isEmpty()) return null;

		ImageRenderConfig cfg = generateFullRenderConfig(availableChannels, renderConfigId, renderConfig);
		return renderService.renderImage(sources.stream().toArray(i -> new ICodestreamSourceDescriptor[i]), cfg);
	}

//	@Cacheable(value = "meas_image", key = "{#measId, #wellNr, #renderConfigId, #renderConfig.hashCode()}")
  @Cacheable(value = "meas_image",
    key = "T(java.lang.String).format('%d-%d-%s', #measId, #wellNr, #renderConfigId ?: 'none')",
    condition = "#result != null && #result.length < 10485760")
	public byte[] renderImage(long measId, int wellNr, Long renderConfigId, ImageRenderConfig renderConfig) throws IOException {

		MeasurementDTO meas = measService.findMeasById(measId).orElse(null);
		if (meas == null) return null;

		List<ICodestreamSourceDescriptor> sources = new ArrayList<>();
		List<String> availableChannels = new ArrayList<>();

		String[] channels = meas.getImageChannels();
		if (channels == null) return null;

		for (int i = 0; i < channels.length; i++) {
			String channel = channels[i];
			ICodestreamSourceDescriptor source = createCodestreamSourceDescriptor(measId, wellNr, channel);
			if (source == null) continue;
			availableChannels.add(channel);
			sources.add(source);
		}
		if (availableChannels.isEmpty()) return null;

		ImageRenderConfig cfg = generateFullRenderConfig(availableChannels, renderConfigId, renderConfig);
		return renderService.renderImage(sources.stream().toArray(i -> new ICodestreamSourceDescriptor[i]), cfg);
	}

	private ImageRenderConfig generateFullRenderConfig(List<String> channelsToRender, Long baseConfigId, ImageRenderConfig additionalConfig) {
		// Start from a default config
		ImageRenderConfig cfg = new ImageRenderConfig(channelsToRender.stream().toArray(i -> new String[i])).withDefaults();

		// If there's a base config, load it and merge it into the default config.
		if (baseConfigId != null) {
			ImageRenderConfig baseConfig = renderConfigService.getConfigById(baseConfigId)
					.map(c -> c.getConfig())
					.orElseThrow(() -> new IllegalArgumentException("No image render config found with ID " + baseConfigId));
			ImageRenderConfigUtils.merge(baseConfig, cfg);
		}

		// Then, apply additionalConfig, if any.
		if (additionalConfig != null) {
			ImageRenderConfigUtils.merge(additionalConfig, cfg);
		}

		// Finally, filter and sort channel configs so they match the requested channels.
		List<ChannelRenderConfig> filteredChannelConfigs = new ArrayList<>();
		for (String channel: channelsToRender) {
			ChannelRenderConfig channelConfig = Arrays.stream(cfg.channelConfigs).filter(c -> channel.equalsIgnoreCase(c.name)).findAny().orElse(new ChannelRenderConfig(channel).withDefaults());
			filteredChannelConfigs.add(channelConfig);
		}
		cfg.channelConfigs = filteredChannelConfigs.stream().toArray(i -> new ChannelRenderConfig[i]);

		System.out.println("Resulting config: " + cfg.toString());

		return cfg;
	}

	private ICodestreamSourceDescriptor createCodestreamSourceDescriptor(long measId, int wellNr, String channel) {
		long codestreamSize = measService.getImageDataSize(measId, wellNr, channel);
		if (codestreamSize <= 0) return null;

		ImageCodestreamAccessor codestreamAccessor = codestreamAccessorCache.getCodestreamAccessor(measId, wellNr, channel);
		return new CodestreamSourceDescriptor(codestreamSize, codestreamAccessor);
	}

	private static class CodestreamSourceDescriptor implements ICodestreamSourceDescriptor {

		private long codestreamSize;
		private ImageCodestreamAccessor codestreamAccessor;

		public CodestreamSourceDescriptor(long codestreamSize, ImageCodestreamAccessor codestreamAccessor) {
			this.codestreamSize = codestreamSize;
			this.codestreamAccessor = codestreamAccessor;
		}

		@Override
		public ICodestreamSource create() throws IOException {
			return new ICodestreamSource() {
				@Override
				public long getSize() throws IOException {
					return codestreamSize;
				}
				@Override
				public byte[] getBytes(long pos, int len) throws IOException {
					return codestreamAccessor.getBytes(pos, len);
				}
			};
		}

	}
}
