package eu.openanalytics.phaedra.measservice.api;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.openanalytics.phaedra.measservice.service.MeasImageService;

@RestController
public class MeasImageController {

	@Autowired
	private MeasImageService measImageService;
	
	@RequestMapping(value = "/image/{measId}/{wellNr}/{channel}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> renderImage(@PathVariable long measId, @PathVariable int wellNr, @PathVariable String channel) {
    	try {
    		byte[] rendered = measImageService.renderImage(measId, wellNr, channel);
   			return ResponseEntity.of(Optional.ofNullable(rendered));
    	} catch (IOException e) {
    		throw new RuntimeException("Render failed", e);
    	}
    }
    
	@RequestMapping(value = "/image/{measId}/{wellNr}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> renderImage(@PathVariable long measId, @PathVariable int wellNr) {
    	try {
    		byte[] rendered = measImageService.renderImage(measId, wellNr);
   			return ResponseEntity.of(Optional.ofNullable(rendered));
    	} catch (IOException e) {
    		throw new RuntimeException("Render failed", e);
    	}
    }
}
