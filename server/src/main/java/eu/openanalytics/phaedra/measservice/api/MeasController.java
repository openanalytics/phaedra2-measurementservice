package eu.openanalytics.phaedra.measservice.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import eu.openanalytics.phaedra.measservice.api.dto.NewMeasurementDTO;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import eu.openanalytics.phaedra.measservice.service.MeasService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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
    public ResponseEntity<?> createMeasurement(@RequestBody String jsonMeasurement, HttpServletRequest request) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) throws IOException {
                return NumberUtils.createFloat("-1.0");
            }

            @Override
            public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetType, Number valueToConvert, String failureMsg) throws IOException {
                return -1;
            }
        });

        NewMeasurementDTO newMeasurementDTO = objectMapper.readValue(jsonMeasurement, NewMeasurementDTO.class);

        //TODO Identify user from auth info in HTTP request
        if (request.getUserPrincipal() == null || request.getUserPrincipal().getName() == null) {
            newMeasurementDTO.setCreatedBy("Anonymous");
        } else {
            newMeasurementDTO.setCreatedBy(request.getUserPrincipal().getName());
        }

        try {
            // Step 1: persist a new Measurement entity
            Measurement newMeas = measService.createNewMeas(newMeasurementDTO.asMeasurement());

            // Step 2: persist the well data for the new Measurement
            if (newMeasurementDTO.getWelldata() != null && !newMeasurementDTO.getWelldata().isEmpty()) {
                measService.setMeasWellData(newMeas.getId(), newMeasurementDTO.getWelldata());
                newMeasurementDTO.setWelldata(null);
            }

            return new ResponseEntity<>(newMeas, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/meas", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MeasurementDTO>> getMeasurements(@RequestParam(name = "filter", required = false) Map<String, String> filters,
                                                                @RequestParam(name = "measIds", required = false) List<Long> measIds) {
        if (CollectionUtils.isNotEmpty(measIds)) {
            return ResponseEntity.ok(measService.getMeasurementsByIds(measIds));
        }
        return ResponseEntity.ok(measService.getAllMeasurements());
    }

    @RequestMapping(value = "/meas/{measId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MeasurementDTO> getMeasurement(@PathVariable long measId) {
        return ResponseEntity.of(measService.findMeasById(measId));
    }

    @RequestMapping(value = "/meas/between/{date1}/{date2}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MeasurementDTO>> getMeasurementsBetween(
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

    @RequestMapping(value = "/meas/capture-job/{captureJobId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMeasurementByCaptureJobId(@PathVariable long captureJobId) {
        if (!measService.measWithCaptureJobIdExists(captureJobId)) return ResponseEntity.notFound().build();
        measService.deleteMeasWithCaptureJobId(captureJobId);
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
