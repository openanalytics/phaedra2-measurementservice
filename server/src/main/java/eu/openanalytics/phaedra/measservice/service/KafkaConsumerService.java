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
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.dto.SubwellDataDTO;
import eu.openanalytics.phaedra.measservice.dto.WellDataDTO;
import eu.openanalytics.phaedra.measservice.exception.MeasurementConsumerException;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class KafkaConsumerService {

    private final MeasService measService;
    private final KafkaProducerService kafkaProducerService;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    public KafkaConsumerService(MeasService measService, KafkaProducerService kafkaProducerService) {
        this.measService = measService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @KafkaListener(topics = TOPIC_DATACAPTURE, groupId = GROUP_ID, filter = "notifyCaptureJobUpdatedFilter")
    public void onCaptureJobUpdated(CaptureJobProgressDTO captureJob) throws MeasurementConsumerException {
    	Long measId = captureJob.getMeasurementId();
    	if (measId != null && measService.measExists(measId)) {
    		MeasurementDTO meas = measService.findMeasById(measId).get();
    		kafkaProducerService.notifyNewMeasurementAvailable(meas);
    	}
    }

    @KafkaListener(topics = TOPIC_MEASUREMENTS, groupId = GROUP_ID + "_requestMeasurementSaveWellData", filter = "requestMeasurementSaveWellDataFilter")
    public void onSaveWellData(WellDataDTO wellData) throws JsonProcessingException {
    	logger.info(String.format("Received well data: %s", wellData));
        if (isBlank(wellData.getColumn()) || isEmpty(wellData.getData())) {
        	logger.warn(String.format("Ignoring invalid saveWellData request: %s", wellData));
        } else {
        	measService.setMeasWellData(wellData.getMeasurementId(), wellData.getColumn(), wellData.getData());
        }
    }
    @KafkaListener(topics = TOPIC_MEASUREMENTS, groupId = GROUP_ID + "_requestMeasurementSaveSubwellData", filter = "requestMeasurementSaveSubwellDataFilter")
    public void onSaveSubwellData(SubwellDataDTO subwellData) throws JsonProcessingException {
        if (isBlank(subwellData.getColumn()) || isEmpty(subwellData.getData())) {
        	logger.warn(String.format("Ignoring invalid saveSubwellData request: %s", subwellData));
        } else {
        	measService.setMeasSubWellData(subwellData.getMeasurementId(), subwellData.getWellId(), subwellData.getColumn(), subwellData.getData());
        }
    }
    
    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CaptureJobProgressDTO {
    	private Long id;
    	private String sourcePath;
    	private String statusCode;
    	private String statusMessage;
    	private Long measurementId;
    	private String barcode;
    }
}
