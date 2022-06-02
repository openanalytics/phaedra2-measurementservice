package eu.openanalytics.phaedra.measservice.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.imaging.jp2k.ICodestreamSource;
import eu.openanalytics.phaedra.imaging.jp2k.openjpeg.source.ByteArraySource;
import eu.openanalytics.phaedra.imaging.render.ImageRenderConfig;
import eu.openanalytics.phaedra.imaging.render.ImageRenderService;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;

@Service
public class MeasImageService {

	@Autowired
	private MeasService measService;
	
	public byte[] renderImage(long measId, int wellNr, String channel) throws IOException {

		MeasurementDTO meas = measService.findMeasById(measId).orElse(null);
		if (meas == null) return null;
		
		byte[] codestreamData = measService.getImageData(measId, wellNr, channel);
		if (codestreamData == null) return null;
		
		ICodestreamSource[] sources = new ICodestreamSource[] {
			new ByteArraySource(codestreamData)
		};
		
		ImageRenderConfig cfg = new ImageRenderConfig(channel);
		
		return renderService().renderImage(sources, cfg);
	}

	public byte[] renderImage(long measId, int wellNr) throws IOException {

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
		
		ImageRenderConfig cfg = new ImageRenderConfig(availableChannels.stream().toArray(i -> new String[i]));
		return renderService().renderImage(sources.toArray(i -> new ICodestreamSource[i]), cfg);
	}
	
	@Bean
	private ImageRenderService renderService() {
		return new ImageRenderService();
	}
}
