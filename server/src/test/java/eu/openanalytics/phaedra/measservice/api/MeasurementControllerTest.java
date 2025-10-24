/**
 * Phaedra II
 *
 * Copyright (C) 2016-2025 Open Analytics
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

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import eu.openanalytics.phaedra.measservice.support.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementControllerTest extends AbstractControllerTest {

//    @Test
    public void measurementsGetTest() throws Exception {
        restWebTestClient
                .get().uri("/measurements")
                .exchange().expectStatus().isOk()
                .expectBodyList(MeasurementDTO.class)
                .value(measurements -> {
                    assertThat(measurements).isNotNull();
                    assertThat(measurements.size()).isEqualTo(4);
                });
    }

//    @Test
    public void measurementGetTest() throws Exception {
        restWebTestClient
                .get().uri("/measurements/{measId}", 1000L)
                .exchange().expectStatus().isOk()
                .expectBody(MeasurementDTO.class)
                .value(measurement -> {
                    assertThat(measurement).isNotNull();
                    assertThat(measurement.getId()).isEqualTo(1000L);
                    assertThat(measurement.getBarcode()).isEqualTo("SBETST0001");
                });
    }

//    @Test
    public void measurementPostTest() throws Exception {
        Measurement measurement = new Measurement();
        measurement.setName("test");
        measurement.setBarcode("barcode");
        measurement.setRows(20);
        measurement.setColumns(30);

        String requestBody = objectMapper.writeValueAsString(measurement);

        restWebTestClient
                .post().uri("/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange().expectStatus().isCreated()
                .expectBody(MeasurementDTO.class)
                .value(newMeasurement -> {
                    assertThat(newMeasurement).isNotNull();
                    assertThat(newMeasurement.getId()).isNotNull();
                    assertThat(newMeasurement.getName()).isEqualTo("test");
                    assertThat(newMeasurement.getBarcode()).isEqualTo("barcode");
                    assertThat(newMeasurement.getRows()).isEqualTo(20);
                    assertThat(newMeasurement.getColumns()).isEqualTo(30);
                    assertThat(newMeasurement.getCreatedBy()).isEqualTo("testuser");
                    assertThat(newMeasurement.getCreatedOn()).isNotNull();
                });
    }

//    @Test
    public void measurementPutTest() throws Exception {
        Long measurementId = 1000L;
        MeasurementDTO measurementDTO = restWebTestClient
                .get().uri("/measurements/{measId}", measurementId)
                .exchange().expectStatus().isOk()
                .expectBody(MeasurementDTO.class)
                .returnResult().getResponseBody();

        measurementDTO.setName("changed");

        String requestBody = objectMapper.writeValueAsString(measurementDTO);
        restWebTestClient
                .put().uri("/measurements/{measId}", measurementId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange().expectStatus().isOk()
                .expectBody(MeasurementDTO.class)
                .value(updatedMeasurement -> {
                    assertThat(updatedMeasurement).isNotNull();
                    assertThat(updatedMeasurement.getId()).isEqualTo(measurementId);
                    assertThat(updatedMeasurement.getName()).isEqualTo("changed");
                    assertThat(updatedMeasurement.getUpdatedBy()).isEqualTo("testuser");
                    assertThat(updatedMeasurement.getUpdatedOn()).isNotNull();
                });
    }

//    @Test
    public void measurementDeleteTest() throws Exception {
        Long measurementId = 1000L;
        restWebTestClient
                .delete().uri("/measurements/{measId}", measurementId)
                .exchange().expectStatus().isNoContent();

        restWebTestClient
                .get().uri("/measurements/{measId}", measurementId)
                .exchange().expectStatus().isNotFound();
    }
}
