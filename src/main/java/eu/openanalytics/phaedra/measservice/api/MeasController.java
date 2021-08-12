package eu.openanalytics.phaedra.measservice.api;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.openanalytics.phaedra.measservice.model.Measurement;
import eu.openanalytics.phaedra.measservice.service.MeasService;

@RestController
public class MeasController {

	@Autowired
	private MeasService measService;
	
	@RequestMapping(value="/meas/{measId}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Measurement> getMeasurement(@PathVariable long measId) {
		return ResponseEntity.of(measService.findMeasById(measId));
	}
	
	@RequestMapping(value="/meas", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createMeasurement(@RequestBody Measurement newMeas) {
		newMeas = measService.createNewMeas(newMeas);
		return new ResponseEntity<>(newMeas, HttpStatus.CREATED);
	}
	
	@RequestMapping(value="/meas/{measId}", method=RequestMethod.DELETE)
	public ResponseEntity<Void> deleteMeasurement(@PathVariable long measId) {
		if (!measService.measExists(measId)) return ResponseEntity.notFound().build();
		measService.deleteMeas(measId);
		return ResponseEntity.noContent().build();
	}
	
	@RequestMapping(value="/meas/{measId}/welldata", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, float[]>> getWellData(@PathVariable long measId) {
		return ResponseEntity.of(Optional.ofNullable(measService.getWellData(measId)));
	}
}