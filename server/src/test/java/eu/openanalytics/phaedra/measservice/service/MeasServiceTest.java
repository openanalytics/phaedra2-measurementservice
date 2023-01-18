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

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.repository.MeasDataRepository;
import eu.openanalytics.phaedra.measservice.repository.MeasRepository;
import eu.openanalytics.phaedra.measservice.support.Containers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Sql({"/jdbc/test-data.sql"})
@TestPropertySource(locations = "classpath:application-test.properties")
public class MeasServiceTest {
    @Autowired
    private MeasServiceImpl measService;

    @Autowired
    private MeasRepository measRepository;

    @Autowired
    private MeasDataRepository measDataRepository;

    @Autowired ModelMapper modelMapper;

    @Container
    private static JdbcDatabaseContainer postgreSQLContainer = new PostgreSQLContainer("postgres:13-alpine")
            .withDatabaseName("phaedra2")
            .withUrlParam("currentSchema","measservice")
            .withPassword("inmemory")
            .withUsername("inmemory");

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

    @BeforeEach
    void before() {
        this.measService = new MeasServiceImpl(measRepository, measDataRepository, modelMapper);
    }

    @Test
    public void getAllMeasurements() {
        List<MeasurementDTO> measurementDTOs = measService.getAllMeasurements();
        assertThat(measurementDTOs.isEmpty()).isFalse();
        assertThat(measurementDTOs.size()).isEqualTo(4);
    }

    @Test
    public void findMeasById() {
        Optional<MeasurementDTO> measurementDTO = measService.findMeasById(3000L);
        MeasurementDTO get = measurementDTO.get();
        assertThat(get.getBarcode()).isEqualTo("SBETST0001");
        assertThat(get.getCaptureJobId()).isEqualTo(1L);
    }

    @Test
    public void existsByCaptureJobId() {
        boolean comp = measService.measWithCaptureJobIdExists(1L);
        assertThat(comp).isTrue();
        boolean comp2 = measService.measWithCaptureJobIdExists(2L);
        assertThat(comp2).isFalse();
    }

    @Test
    public void deleteMeasurementByCaptureJobId() {
        //Does a measurement with this id exist?
        boolean comp = measService.measWithCaptureJobIdExists(1L);
        assertThat(comp).isTrue();
        //Delete
        measService.deleteMeasWithCaptureJobId(1L);
        //Does a measurement with this id exist?
        boolean comp2 = measService.measWithCaptureJobIdExists(1L);
        assertThat(comp2).isFalse();
        //Are all measurements removed now?
        List<MeasurementDTO> measurementDTOs = measService.getAllMeasurements();
        assertThat(measurementDTOs.size()).isEqualTo(1);
        assertThat(measurementDTOs.isEmpty()).isFalse();
    }
}
