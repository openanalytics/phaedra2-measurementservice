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
import eu.openanalytics.phaedra.measservice.dto.SubwellDataDTO;
import eu.openanalytics.phaedra.measservice.dto.WellDataDTO;
import eu.openanalytics.phaedra.measservice.record.FilterOptions;
import eu.openanalytics.phaedra.measservice.record.PropertyRecord;
import eu.openanalytics.phaedra.measservice.service.MeasService;
import eu.openanalytics.phaedra.metadataservice.client.MetadataServiceGraphQlClient;
import eu.openanalytics.phaedra.metadataservice.dto.MetadataDTO;
import eu.openanalytics.phaedra.metadataservice.dto.TagDTO;
import eu.openanalytics.phaedra.metadataservice.enumeration.ObjectClass;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MeasurementGraphQLController {

    private final MeasService measService;
    private final MetadataServiceGraphQlClient metadataServiceGraphQlClient;

    public MeasurementGraphQLController(MeasService measService, MetadataServiceGraphQlClient metadataServiceGraphQlClient) {
        this.measService = measService;
        this.metadataServiceGraphQlClient = metadataServiceGraphQlClient;
    }

    @QueryMapping
    public List<MeasurementDTO> measurements(@Argument FilterOptions filter) {
        if (filter != null) {
            if (CollectionUtils.isNotEmpty(filter.ids())) {
                return measService.getMeasurementsByIds(filter.ids());
            }
            if (filter.from() != null || filter.to() != null) {
                return measService.findMeasByCreatedOnRange(filter.from(), filter.to());
            }
        }
        return measService.getAllMeasurements();
    }

    @QueryMapping
    public MeasurementDTO measurementById(@Argument Long measurementId) {
        return measService.findMeasById(measurementId).orElse(null);
    }

    @QueryMapping
    public List<WellDataDTO> measurementDataById(@Argument Long measurementId) {
        List<WellDataDTO> result = new ArrayList<>();
        measService.getWellData(measurementId).forEach((key, value) -> result.add(new WellDataDTO(measurementId, key, value)));
        return result;
    }

    @QueryMapping
    public WellDataDTO measurementDataByIdAndWellColumn(@Argument Long measurementId, @Argument String wellColumn) {
        return new WellDataDTO(measurementId, wellColumn, measService.getWellData(measurementId, wellColumn));
    }

    @QueryMapping
    public List<String> getUniqueWellDataColumns() {
        return measService.getAllUniqueWellDataColumns();
    }

    @QueryMapping
    public List<SubwellDataDTO> measurementSubWellDataByIdAndWellNr(@Argument Long measurementId,
                                                                    @Argument Integer wellNr) {
        List<SubwellDataDTO> result = new ArrayList<>();
        measService.getSubWellData(measurementId, wellNr)
                .forEach((column, value) ->
                        result.add(new SubwellDataDTO(measurementId, wellNr, column, value)));
        return result;
    }

    @QueryMapping
    public List<SubwellDataDTO> measurementSubWellDataByIdAndColumns(@Argument Long measurementId,
                                                                     @Argument List<String> columns) {
        List<SubwellDataDTO> result = new ArrayList<>();
        for (String column : columns) {
            measService.getSubWellData(measurementId, column)
                    .forEach((wellNr, value) ->
                            result.add(new SubwellDataDTO(measurementId, wellNr, column, value)));
        }
        return result;
    }

    @QueryMapping
    public List<SubwellDataDTO> measurementSubWellDataByIdAndWellNrAndColumns(
            @Argument Long measurementId, @Argument Integer wellNr, @Argument List<String> columns) {
        List<SubwellDataDTO> result = new ArrayList<>();
        measService.getSubWellData(measurementId, wellNr, columns)
                .forEach((column, value) ->
                        result.add(new SubwellDataDTO(measurementId, wellNr, column, value)));
        return result;
    }

    @QueryMapping
    public List<String> getUniqueSubWellDataColumns() {
        return measService.getAllUniqueSubWellDataColumns();
    }

    @BatchMapping(typeName = "MeasurementDTO", field = "tags")
    public Map<MeasurementDTO, List<String>> tags(List<MeasurementDTO> measurements) {
        List<Long> measurementIds = measurements.stream()
                .map(MeasurementDTO::getId)
                .toList();

        List<MetadataDTO> metadataResults = metadataServiceGraphQlClient.getMetadata(measurementIds, ObjectClass.MEASUREMENT);

        Map<Long, List<String>> tagsMap = metadataResults.stream()
                .collect(Collectors.toMap(
                        MetadataDTO::getObjectId,
                        metadata -> metadata.getTags().stream()
                                .map(TagDTO::getTag)
                                .toList(),
                        (existing, replacement) -> existing // Handle duplicates
                ));

        return measurements.stream()
                .collect(Collectors.toMap(
                        measurement -> measurement,
                        measurement -> tagsMap.getOrDefault(measurement.getId(), List.of())
                ));
    }

    @BatchMapping(typeName = "MeasurementDTO", field = "properties")
    public Map<MeasurementDTO, List<PropertyRecord>> properties(List<MeasurementDTO> measurements) {
        List<Long> measurementIds = measurements.stream()
                .map(MeasurementDTO::getId)
                .toList();

        List<MetadataDTO> metadataResults = metadataServiceGraphQlClient.getMetadata(measurementIds, ObjectClass.MEASUREMENT);

        Map<Long, List<PropertyRecord>> propertiesMap = metadataResults.stream()
                .collect(Collectors.toMap(
                        MetadataDTO::getObjectId,
                        metadata -> metadata.getProperties().stream()
                                .map(propertyDTO -> new PropertyRecord(propertyDTO.getPropertyName(), propertyDTO.getPropertyValue()))
                                .toList(),
                        (existing, replacement) -> existing // Handle duplicates
                ));

        return measurements.stream()
                .collect(Collectors.toMap(
                        measurement -> measurement,
                        measurement -> propertiesMap.getOrDefault(measurement.getId(), List.of())
                ));
    }
}
