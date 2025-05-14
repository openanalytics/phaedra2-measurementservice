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
import eu.openanalytics.phaedra.measservice.dto.WellDataDTO;
import eu.openanalytics.phaedra.measservice.record.FilterOptions;
import eu.openanalytics.phaedra.measservice.service.MeasService;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class MeasurementGraphQLController {

    private final MeasService measService;

    public MeasurementGraphQLController(MeasService measService) {
        this.measService = measService;
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
    public float[] measurementDataByIdAndWellColumn(@Argument Long measurementId, @Argument String wellColumn) {
        return measService.getWellData(measurementId, wellColumn);
    }

    @QueryMapping
    public List<String> getUniqueWellDataColumns() {
        return measService.getAllUniqueWellDataColumns();
    }

    @QueryMapping
    public List<String> getUniqueSubWellDataColumns() {
        return measService.getAllUniqueSubWellDataColumns();
    }
}
