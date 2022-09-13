package eu.openanalytics.phaedra.measservice.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.phaedra.measservice.model.NamedImageRenderConfig;
import eu.openanalytics.phaedra.measservice.service.ImageRenderConfigService;

@RestController
public class ImageRenderConfigController {

	@Autowired
	private ImageRenderConfigService service;
	
    @RequestMapping(value = "/render-config", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createConfig(@RequestBody String renderConfigString, ObjectMapper objectMapper) {
        try {
        	NamedImageRenderConfig config = objectMapper.readValue(renderConfigString, NamedImageRenderConfig.class);
        	config = service.createConfig(config);
        	return new ResponseEntity<>(config, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
    
    @RequestMapping(value = "/render-config", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateConfig(@RequestBody String renderConfigString, ObjectMapper objectMapper) {
        try {
        	NamedImageRenderConfig config = objectMapper.readValue(renderConfigString, NamedImageRenderConfig.class);
        	config = service.updateConfig(config);
        	return new ResponseEntity<>(config, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
    
    @RequestMapping(value = "/render-configs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<NamedImageRenderConfig>> getAllConfigs() {
        return ResponseEntity.ok(service.getAllConfigs());
    }
    
    @RequestMapping(value = "/render-config/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NamedImageRenderConfig> getConfig(@PathVariable long id) {
        return ResponseEntity.of(service.getConfigById(id));
    }
    
    @RequestMapping(value = "/render-config/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteConfig(@PathVariable long id) {
        if (!service.configExists(id)) return ResponseEntity.notFound().build();
        service.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }
}
