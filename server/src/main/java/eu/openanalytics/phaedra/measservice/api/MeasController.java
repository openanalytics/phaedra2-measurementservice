/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
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
package eu.openanalytics.phaedra.measservice.api;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.openanalytics.phaedra.measservice.api.dto.NewMeasurementDTO;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.exception.MeasurementNotFoundException;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import eu.openanalytics.phaedra.measservice.service.MeasService;

@RestController
@RequestMapping("/measurements")
public class MeasController {

    @Autowired
    private MeasService measService;

    /**
     * Measurements
     * ************
     */

    @PostMapping
    public ResponseEntity<?> createMeasurement(@RequestBody NewMeasurementDTO measurementDTO) throws JsonProcessingException {
        Measurement meas = measService.createNewMeas(measurementDTO.asMeasurement());
        if (measurementDTO.getWelldata() != null && !measurementDTO.getWelldata().isEmpty()) {
            measService.setMeasWellData(meas.getId(), measurementDTO.getWelldata());
        }
        return new ResponseEntity<>(meas, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{measurementId}")
    public ResponseEntity<?> updateMeasurement(@PathVariable long measurementId, @RequestBody MeasurementDTO measurementDTO) throws JsonProcessingException {
        if (measurementId != measurementDTO.getId())
            return ResponseEntity.badRequest().build();

        try {
            Measurement measurement = measService.updateMeasurement(measurementDTO);
            return new ResponseEntity<>(measurement, HttpStatus.OK);
        } catch (MeasurementNotFoundException e) {
            return ResponseEntity.noContent().build();
        }

    }

    @GetMapping(value = "/{measurementId}")
    public ResponseEntity<MeasurementDTO> getMeasurement(@PathVariable long measurementId) {
        return ResponseEntity.of(measService.findMeasById(measurementId));
    }

    @GetMapping
    public ResponseEntity<List<MeasurementDTO>> getMeasurements(
    		@RequestParam(name = "ids", required = false) List<Long> ids,
    		@RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date fromDate,
    		@RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date toDate) {

        if (CollectionUtils.isNotEmpty(ids)) {
            return ResponseEntity.ok(measService.getMeasurementsByIds(ids));
        } else if (fromDate != null || toDate != null) {
        	return ResponseEntity.ok(measService.findMeasByCreatedOnRange(fromDate, toDate));
        } else {
        	return ResponseEntity.ok(measService.getAllMeasurements());
        }
    }

    @DeleteMapping(value = "/{measurementId}")
    public ResponseEntity<Void> deleteMeasurement(@PathVariable long measurementId) {
        if (!measService.measExists(measurementId)) return ResponseEntity.notFound().build();
        measService.deleteMeas(measurementId);
        return ResponseEntity.noContent().build();
    }

    /**
     * WellData
     * ********
     */

    @GetMapping(value = "/{measurementId}/welldata")
    public ResponseEntity<Map<String, float[]>> getWellData(@PathVariable long measurementId) {
        return ResponseEntity.of(Optional.ofNullable(measService.getWellData(measurementId)));
    }

    @GetMapping(value = "/{measurementId}/welldata/{column}")
    public ResponseEntity<float[]> getWellData(@PathVariable long measurementId, @PathVariable String column) {
        return ResponseEntity.of(Optional.ofNullable(measService.getWellData(measurementId, column)));
    }

    /**
     * SubWellData
     * ***********
     */

    @PostMapping(value = "/{measurementId}/subwelldata/{column}")
    public ResponseEntity<Void> setSubWellData(@PathVariable long measurementId, @PathVariable String column, @RequestBody Map<Integer, float[]> dataMap) {
        measService.setMeasSubWellData(measurementId, column, dataMap);
        return ResponseEntity.created(null).build();
    }

    @GetMapping(value = "/{measurementId}/subwelldata")
    public ResponseEntity<Map<String, float[]>> getSubWellData(@PathVariable long measurementId, @RequestParam List<String> columns, @RequestParam int wellNr) {
        return ResponseEntity.of(Optional.ofNullable(measService.getSubWellData(measurementId, wellNr, columns)));
    }

    @GetMapping(value = "/{measurementId}/subwelldata/{column}")
    public ResponseEntity<Map<Integer, float[]>> getSubWellData(@PathVariable long measurementId, @PathVariable String column) {
        return ResponseEntity.of(Optional.ofNullable(measService.getSubWellData(measurementId, column)));
    }

    @GetMapping(value = "/{measurementId}/subwelldata/{column}/{wellNr}")
    public ResponseEntity<float[]> getSubWellData(@PathVariable long measurementId, @PathVariable String column, @PathVariable int wellNr) {
        return ResponseEntity.of(Optional.ofNullable(measService.getSubWellData(measurementId, wellNr, column)));
    }

    /**
     * ImageData
     * *********
     */

    @PostMapping(value = "/{measurementId}/imagedata/{wellNr}")
    public ResponseEntity<Void> setImageData(@PathVariable long measurementId, @PathVariable int wellNr, @RequestBody Map<String, byte[]> dataMap) {
        measService.setMeasImageData(measurementId, wellNr, dataMap);
        return ResponseEntity.created(null).build();
    }

    @PostMapping(value = "/{measurementId}/imagedata/{wellNr}/{channel}")
    public ResponseEntity<Void> setImageData(@PathVariable long measurementId, @PathVariable int wellNr, @PathVariable String channel, @RequestBody byte[] imageData) {
        measService.setMeasImageData(measurementId, wellNr, channel, imageData);
        return ResponseEntity.created(null).build();
    }

    @GetMapping(value = "/{measurementId}/imagedata/{wellNr}")
    public ResponseEntity<Map<String, byte[]>> getImageData(@PathVariable long measurementId, @PathVariable int wellNr) {
        return ResponseEntity.of(Optional.ofNullable(measService.getImageData(measurementId, wellNr)));
    }

    @GetMapping(value = "/{measurementId}/imagedata/{wellNr}/{channel}")
    public ResponseEntity<byte[]> getImageData(@PathVariable long measurementId, @PathVariable int wellNr, @PathVariable String channel) {
        return ResponseEntity.of(Optional.ofNullable(measService.getImageData(measurementId, wellNr, channel)));
    }
}
