package eu.openanalytics.phaedra.measservice.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import eu.openanalytics.phaedra.measservice.service.MeasService;

@Component
public class ImageCodestreamAccessorCache {

	@Autowired
	private MeasService measService;
	
	@Cacheable("codestream_accessors")
	public ImageCodestreamAccessor getCodestreamAccessor(long measId, int wellNr, String channel) {
		return new ImageCodestreamAccessor(measId, wellNr, channel, measService);
	}
	
}
