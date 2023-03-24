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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.measservice.api.dto.NewMeasurementDTO;
import eu.openanalytics.phaedra.measservice.exception.MeasurementConsumerException;
import eu.openanalytics.phaedra.measservice.model.Measurement;

@Service
public class MeasKafkaConsumer {

    private final MeasService measService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String TOPIC = "datacapture";
    private static final String EVENT_SAVE_MEAS = "saveMeasurement";
    private static final String GROUP_ID = "measurement-service";
    
    public MeasKafkaConsumer(MeasService measService) {
        this.measService = measService;
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void onNewMeasurement(NewMeasurementDTO newMeasurementDTO, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String msgKey) throws MeasurementConsumerException {
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
}
