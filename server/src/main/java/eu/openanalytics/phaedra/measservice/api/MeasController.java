package eu.openanalytics.phaedra.measservice.api;

import eu.openanalytics.phaedra.measservice.api.dto.NewMeasurementDTO;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import eu.openanalytics.phaedra.measservice.service.MeasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class MeasController {

    @Autowired
    private MeasService measService;

    /**
     * Measurements
     * ************
     */

    @RequestMapping(value = "/meas", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createMeasurement(@RequestBody NewMeasurementDTO newMeasDTO, HttpServletRequest request) {

        //TODO Identify user from auth info in HTTP request
        if (request.getUserPrincipal() == null || request.getUserPrincipal().getName() == null) {
            newMeasDTO.setCreatedBy("Anonymous");
        } else {
            newMeasDTO.setCreatedBy(request.getUserPrincipal().getName());
        }

        try {
            // Step 1: persist a new Measurement entity
            Measurement newMeas = measService.createNewMeas(newMeasDTO.asMeasurement());

            // Step 2: persist the well data for the new Measurement
            if (newMeasDTO.getWelldata() != null && !newMeasDTO.getWelldata().isEmpty()) {
                measService.setMeasWellData(newMeas.getId(), newMeasDTO.getWelldata());
                newMeasDTO.setWelldata(null);
            }

            return new ResponseEntity<>(newMeas, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/meas", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Measurement>> getMeasurements(@RequestParam(name = "filter", required = false) Map<String, String> filters) {
        return ResponseEntity.ok(measService.getAllMeasurements());
    }

    @RequestMapping(value = "/meas/{measId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Measurement> getMeasurement(@PathVariable long measId) {
        return ResponseEntity.of(measService.findMeasById(measId));
    }

    @RequestMapping(value = "/meas/between/{date1}/{date2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Measurement>> getMeasurementsBetween(
            @PathVariable @DateTimeFormat(pattern = "dd-MM-yyyy") Date date1,
            @PathVariable @DateTimeFormat(pattern = "dd-MM-yyyy") Date date2) {
        return ResponseEntity.ok(measService.findMeasByCreatedOnRange(date1, date2));
    }

    @RequestMapping(value = "/meas/{measId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMeasurement(@PathVariable long measId) {
        if (!measService.measExists(measId)) return ResponseEntity.notFound().build();
        measService.deleteMeas(measId);
        return ResponseEntity.noContent().build();
    }

    /**
     * WellData
     * ********
     */

    @RequestMapping(value = "/meas/{measId}/welldata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, float[]>> getWellData(@PathVariable long measId) {
        return ResponseEntity.of(Optional.ofNullable(measService.getWellData(measId)));
    }

    @RequestMapping(value = "/meas/{measId}/welldata/{column}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<float[]> getWellData(@PathVariable long measId, @PathVariable String column) {
        return ResponseEntity.of(Optional.ofNullable(measService.getWellData(measId, column)));
    }

    /**
     * SubWellData
     * ***********
     */

    @RequestMapping(value = "/meas/{measId}/subwelldata/{column}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> setSubWellData(@PathVariable long measId, @PathVariable String column, @RequestBody Map<Integer, float[]> dataMap) {
        measService.setMeasSubWellData(measId, column, dataMap);
        return ResponseEntity.created(null).build();
    }

    @RequestMapping(value = "/meas/{measId}/subwelldata/{column}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<Integer, float[]>> getSubWellData(@PathVariable long measId, @PathVariable String column) {
        return ResponseEntity.of(Optional.ofNullable(measService.getSubWellData(measId, column)));
    }

    @RequestMapping(value = "/meas/{measId}/subwelldata/{column}/{wellNr}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<float[]> getSubWellData(@PathVariable long measId, @PathVariable String column, @PathVariable int wellNr) {
        return ResponseEntity.of(Optional.ofNullable(measService.getSubWellData(measId, wellNr, column)));
    }

    /**
     * ImageData
     * *********
     */

    @RequestMapping(value = "/meas/{measId}/imagedata/{wellNr}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> setImageData(@PathVariable long measId, @PathVariable int wellNr, @RequestBody Map<String, byte[]> dataMap) {
        measService.setMeasImageData(measId, wellNr, dataMap);
        return ResponseEntity.created(null).build();
    }

    @RequestMapping(value = "/meas/{measId}/imagedata/{wellNr}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, byte[]>> getImageData(@PathVariable long measId, @PathVariable int wellNr) {
        return ResponseEntity.of(Optional.ofNullable(measService.getImageData(measId, wellNr)));
    }

    @RequestMapping(value = "/meas/{measId}/imagedata/{wellNr}/{channel}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> getImageData(@PathVariable long measId, @PathVariable int wellNr, @PathVariable String channel) {
        return ResponseEntity.of(Optional.ofNullable(measService.getImageData(measId, wellNr, channel)));
    }
}
