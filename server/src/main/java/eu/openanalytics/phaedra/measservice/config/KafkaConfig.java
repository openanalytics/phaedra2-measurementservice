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
package eu.openanalytics.phaedra.measservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.kafka.support.converter.BytesJsonMessageConverter;

@Configuration
@EnableKafka
public class KafkaConfig {
	
    public static final String GROUP_ID = "measurements-service";

    // Topics
    public static final String TOPIC_MEASUREMENTS = "measurements";
    public static final String TOPIC_DATACAPTURE = "datacapture";

    // Events
    public static final String EVENT_REQ_MEAS_SAVE_WELL_DATA = "requestMeasurementSaveWellData";
    public static final String EVENT_REQ_MEAS_SAVE_SUBWELL_DATA = "requestMeasurementSaveSubwellData";
    
    public static final String EVENT_NOTIFY_DC_JOB_UPDATED = "notifyCaptureJobUpdated";
    public static final String EVENT_NOTIFY_NEW_MEASUREMENT = "notifyNewMeasurement";
    
    @Bean
    public RecordFilterStrategy<String, String> requestMeasurementSaveWellDataFilter() {
        return rec -> !(rec.key().equalsIgnoreCase(EVENT_REQ_MEAS_SAVE_WELL_DATA));
    }

    @Bean
    public RecordFilterStrategy<String, String> requestMeasurementSaveSubwellDataFilter() {
        return rec -> !(rec.key().equalsIgnoreCase(EVENT_REQ_MEAS_SAVE_SUBWELL_DATA));
    }

    @Bean
    public RecordFilterStrategy<String, String> notifyCaptureJobUpdatedFilter() {
        return rec -> !(rec.key().equalsIgnoreCase(EVENT_NOTIFY_DC_JOB_UPDATED));
    }

    @Bean
    public BytesJsonMessageConverter messageConverter() {
    	return new BytesJsonMessageConverter();
    }
}
