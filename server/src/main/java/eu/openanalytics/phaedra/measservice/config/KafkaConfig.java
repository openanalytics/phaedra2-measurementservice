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

@Configuration
@EnableKafka
public class KafkaConfig {
    public static final String GROUP_ID = "measurements-service";

    // Topics
    public static final String TOPIC_MEASUREMENTS = "measurements";

    // Events
    public static final String EVENT_SAVE_WELL_DATA = "saveWellData";
    public static final String EVENT_SAVE_SUBWELL_DATA = "saveSubwellData";

    @Bean
    public RecordFilterStrategy<String, String> saveWellDataFilter() {
        return rec -> !(rec.key().equalsIgnoreCase(EVENT_SAVE_WELL_DATA));
    }

    @Bean
    public RecordFilterStrategy<String, String> saveSubwellDataFilter() {
        return rec -> !(rec.key().equalsIgnoreCase(EVENT_SAVE_SUBWELL_DATA));
    }
}
