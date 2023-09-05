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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import eu.openanalytics.phaedra.measservice.support.Containers;

@Testcontainers
@SpringBootTest
@Sql({"/jdbc/test-data.sql"})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.properties")
public class MeasurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", Containers.postgreSQLContainer::getJdbcUrl);
        registry.add("DB_USERNAME", Containers.postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", Containers.postgreSQLContainer::getPassword);
        registry.add("DB_SCHEMA", () -> "measservice");

        registry.add("S3_ENDPOINT", () -> "https://s3.amazonaws.com");
        registry.add("S3_REGION", () -> "eu-west-1");
        registry.add("S3_USERNAME", () -> "test");
        registry.add("S3_PASSWORD", () -> "test");
        registry.add("S3_BUCKET", () -> "phaedra2-poc-measdata");
    }

    @Test
    public void measurementsGetTest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/measurements"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<Measurement> measurements = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), List.class);
        assertThat(measurements).isNotNull();
        assertThat(measurements.size()).isEqualTo(4);
    }

    @Test
    public void measurementGetTest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/measurements/{measId}", 1000L))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        Measurement measurement = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Measurement.class);
        assertThat(measurement.getId()).isEqualTo(1000L);
        assertThat(measurement.getBarcode()).isEqualTo("SBETST0001");
    }

    @Test
    public void measurementPostTest() throws Exception {
        Measurement measurement = new Measurement();
        measurement.setName("test");
        measurement.setBarcode("barcode");
        measurement.setRows(20);
        measurement.setColumns(30);
        measurement.setCreatedBy("smarien");
        measurement.setCreatedOn(new Date());

        String requestBody = objectMapper.writeValueAsString(measurement);

        MvcResult mvcResult = this.mockMvc.perform(post("/measurements").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MeasurementDTO measurementDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), MeasurementDTO.class);
        assertThat(measurementDTO).isNotNull();
        assertThat(measurementDTO.getId()).isEqualTo(1L);
    }
}
