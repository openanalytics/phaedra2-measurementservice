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

import static eu.openanalytics.phaedra.measservice.config.KafkaConfig.GROUP_ID;
import static eu.openanalytics.phaedra.measservice.config.KafkaConfig.TOPIC_DATACAPTURE;
import static eu.openanalytics.phaedra.measservice.config.KafkaConfig.TOPIC_MEASUREMENTS;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.replace;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.dto.SubwellDataDTO;
import eu.openanalytics.phaedra.measservice.dto.WellDataDTO;
import eu.openanalytics.phaedra.measservice.exception.MeasurementConsumerException;

@Service
public class KafkaConsumerService {

    private final MeasService measService;
    private final KafkaProducerService kafkaProducerService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public KafkaConsumerService(MeasService measService, KafkaProducerService kafkaProducerService) {
        this.measService = measService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @KafkaListener(topics = TOPIC_DATACAPTURE, groupId = GROUP_ID, filter = "notifyMeasCapturedFilter")
    public void onMeasCaptured(MeasurementDTO capturedMeas) throws MeasurementConsumerException {
    	if (measService.measExists(capturedMeas.getId())) {
    		MeasurementDTO meas = measService.findMeasById(capturedMeas.getId()).get();
    		kafkaProducerService.notifyNewMeasurementAvailable(meas);
    	}
    }

    @KafkaListener(topics = TOPIC_MEASUREMENTS, groupId = GROUP_ID, filter = "saveWellDataFilter")
    public void onSaveWellData(String wellData) throws JsonProcessingException {
        String cleanWellDataString = replace(wellData,"\\r", "");
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) {
                logger.info(String.format("Value to convert: %s", valueToConvert));
                return NumberUtils.isParsable(valueToConvert) ? NumberUtils.createFloat("-1.0") : Float.NaN;
            }

            @Override
            public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetType, Number valueToConvert, String failureMsg) {
                logger.info(String.format("Value to convert: %s", valueToConvert));
                return -1;
            }
        });

        WellDataDTO wellDataDTO = objectMapper.readValue(cleanWellDataString, WellDataDTO.class);
        if (isNotBlank(wellDataDTO.getColumn())) {
            if (isNotEmpty(wellDataDTO.getData())) {
                measService.setMeasWellData(wellDataDTO.getMeasurementId(), wellDataDTO.getColumn(), wellDataDTO.getData());
            }
        }
    }
    @KafkaListener(topics = TOPIC_MEASUREMENTS, groupId = GROUP_ID, filter = "saveSubwellDataFilter")
    public void onSaveSubwellData(String subwellData) throws JsonProcessingException {
        String cleanSubWellDataString = replace(subwellData, "\\r", "");
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) {
                logger.info(String.format("Value to convert: %s", valueToConvert));
                return NumberUtils.isParsable(valueToConvert) ? NumberUtils.createFloat("-1.0") : Float.NaN;
            }

            @Override
            public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetType, Number valueToConvert, String failureMsg) {
                logger.info(String.format("Value to convert: %s", valueToConvert));
                return -1;
            }
        });

        SubwellDataDTO subwellDataDTO = objectMapper.readValue(cleanSubWellDataString, SubwellDataDTO.class);
        if (subwellDataDTO != null && isNotBlank(subwellDataDTO.getColumn())) {
            if (isNotEmpty(subwellDataDTO.getData())) {
                measService.setMeasSubWellData(subwellDataDTO.getMeasurementId(), subwellDataDTO.getWellId(), subwellDataDTO.getColumn(), subwellDataDTO.getData());
            }
        }
    }
}
