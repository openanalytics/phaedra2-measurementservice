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
import eu.openanalytics.phaedra.measservice.record.PropertyRecord;
import eu.openanalytics.phaedra.measservice.support.AbstractControllerTest;
import eu.openanalytics.phaedra.metadataservice.client.MetadataServiceGraphQlClient;
import eu.openanalytics.phaedra.metadataservice.dto.MetadataDTO;
import eu.openanalytics.phaedra.metadataservice.dto.PropertyDTO;
import eu.openanalytics.phaedra.metadataservice.dto.TagDTO;
import eu.openanalytics.phaedra.metadataservice.enumeration.ObjectClass;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MeasurementGraphQLControllerTest extends AbstractControllerTest {

    @MockBean
    private MetadataServiceGraphQlClient metadataServiceGraphQlClient;

//    @Test
    public void getMeasurementsTest() {
        List<Long> testMeasurementIds = createTestMeasurementIds();
        when(metadataServiceGraphQlClient.getMetadata(testMeasurementIds, ObjectClass.MEASUREMENT))
                .thenReturn(createTestMetadata());
        String document = """
                    {
                        measurements {
                            id
                            name
                            barcode
                            tags
                            properties {
                                propertyName
                                propertyValue
                            }
                        }
                    }
                """;
        GraphQlTester.Response response = httpGraphQlTester.document(document).execute();
        response.errors().verify();
        List<MeasurementDTO> result = response.path("measurements").entityList(MeasurementDTO.class).get();
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(4);
        assertThat(result.stream().map(MeasurementDTO::getId).toList()).containsExactlyInAnyOrder(1000L, 2000L, 3000L, 4000L);
        assertThat(result.stream().map(MeasurementDTO::getTags).toList()).isNotEmpty();
        assertThat(result.stream().map(MeasurementDTO::getProperties).toList()).isNotEmpty();
    }

    private List<Long> createTestMeasurementIds() {
        return List.of(1000L, 2000L, 3000L, 4000L);
    }

    private List<MetadataDTO> createTestMetadata() {
        return List.of(
                createMetadataForMeasurement(1000L, 1L, 2L),
                createMetadataForMeasurement(2000L, 3L, 4L),
                createMetadataForMeasurement(3000L, 5L, 6L),
                createMetadataForMeasurement(4000L, 7L, 8L)
        );
    }

    private MetadataDTO createMetadataForMeasurement(Long measurementId, Long firstTagId, Long secondTagId) {
        return new MetadataDTO(measurementId,
                List.of(
                        new TagDTO(firstTagId, "MeasTest_%d".formatted(firstTagId), null, null),
                        new TagDTO(secondTagId, "MeasTest_%d".formatted(secondTagId), null, null)
                ),
                List.of(
                        new PropertyDTO("TestProperty1", "PropertyValue1", measurementId, ObjectClass.MEASUREMENT),
                        new PropertyDTO("TestProperty2", "PropertyValue2", measurementId, ObjectClass.MEASUREMENT)
                )
        );
    }
}
