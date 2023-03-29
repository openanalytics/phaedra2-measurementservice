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
package eu.openanalytics.phaedra.measservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import eu.openanalytics.phaedra.measservice.api.dto.NewMeasurementDTO;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.dto.SubwellDataDTO;
import eu.openanalytics.phaedra.measservice.dto.WellDataDTO;
import eu.openanalytics.phaedra.measservice.exception.MeasurementConsumerException;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

import static eu.openanalytics.phaedra.measservice.config.KafkaConfig.*;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

@Service
public class MeasKafkaConsumer {

    private final MeasService measService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String TOPIC = "datacapture";
    private static final String EVENT_SAVE_MEAS = "saveMeasurement";

    public MeasKafkaConsumer(MeasService measService) {
        this.measService = measService;
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void onNewMeasurement(NewMeasurementDTO newMeasurementDTO, @Header(KafkaHeaders.RECEIVED_KEY) String msgKey) throws MeasurementConsumerException {
        if (!EVENT_SAVE_MEAS.equals(msgKey)) return;

        logger.info("Create new measurement with " + newMeasurementDTO.getName() + " and barcode " + newMeasurementDTO.getBarcode());
        try {
            // Step 1: persist a new Measurement entity
            Measurement newMeas = measService.createNewMeas(newMeasurementDTO.asMeasurement());

            // Step 2: persist the well data for the new Measurement
            if (newMeasurementDTO.getWelldata() != null && !newMeasurementDTO.getWelldata().isEmpty()) {
                measService.setMeasWellData(newMeas.getId(), newMeasurementDTO.getWelldata());
                newMeasurementDTO.setWelldata(null);
            }
        } catch (Exception e) {
            throw new MeasurementConsumerException(e.getMessage());
        }
    }

    @KafkaListener(topics = TOPIC_MEASUREMENTS, groupId = GROUP_ID)
    public void consumeMeasurements(String msgValue, @Header(KafkaHeaders.RECEIVED_KEY) String msgKey) throws JsonProcessingException {
        switch (msgKey) {
            case EVENT_SAVE_WELL_DATA -> onSaveWellData(msgValue);
            case EVENT_SAVE_SUBWELL_DATA -> onSaveSubwellData(msgValue);
            default -> { return; }
        }
    }


    public void onSaveWellData(String wellData) throws JsonProcessingException {
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

        WellDataDTO wellDataDTO = objectMapper.readValue(wellData, WellDataDTO.class);
        Optional<MeasurementDTO> measurementDTO = measService.findMeasById(wellDataDTO.getMeasurementId());
        if (measurementDTO.isEmpty()) return;

        if (ArrayUtils.isNotEmpty(wellDataDTO.getData())) {
            measService.setMeasWellData(wellDataDTO.getMeasurementId(), wellDataDTO.getColumn(), wellDataDTO.getData());
        }
    }

    public void onSaveSubwellData(String subwellData) throws JsonProcessingException {
        SubwellDataDTO subwellDataDTO = new ObjectMapper().readValue(subwellData, SubwellDataDTO.class);
        Optional<MeasurementDTO> measurement = measService.findMeasById(subwellDataDTO.getMeasurementId());

        if (measurement.isPresent()) {
            if (isNotEmpty(subwellDataDTO.getData())) {
                subwellDataDTO.getData().keySet().parallelStream().forEach(column -> {
                    measService.setMeasSubWellData(subwellDataDTO.getMeasurementId(), subwellDataDTO.getWellId(), column, subwellDataDTO.getData().get(column));
                });
            }
        }
    }
}
