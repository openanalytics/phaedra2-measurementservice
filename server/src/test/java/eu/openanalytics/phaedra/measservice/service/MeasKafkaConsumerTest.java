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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.dto.WellDataDTO;

@ExtendWith(MockitoExtension.class)
//@Testcontainers
//@SpringBootTest
//@Sql({"/jdbc/test-data.sql"})
//@TestPropertySource(locations = "classpath:application-test.properties")
class MeasKafkaConsumerTest {

    @Mock private MeasService measService;
    @InjectMocks private KafkaConsumerService measKafkaConsumer;

    private WellDataDTO wellData;

    @BeforeEach
    void setUp() {
        wellData = new WellDataDTO(1, "TestColumn", new float[] {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f});
    }

//    @Test
    void onSaveWellData_handlesEventWithSaveWellDataKey() throws JsonProcessingException {
        // given
        MeasurementDTO measurementDTO = new MeasurementDTO();
        measurementDTO.setId(1L);
        when(measService.findMeasById(measurementDTO.getId())).thenReturn(Optional.of(measurementDTO));

        // when
        measKafkaConsumer.onSaveWellData(wellData);

        // then
        verify(measService).setMeasWellData(measurementDTO.getId(), wellData.getColumn(), wellData.getData());
    }
}
