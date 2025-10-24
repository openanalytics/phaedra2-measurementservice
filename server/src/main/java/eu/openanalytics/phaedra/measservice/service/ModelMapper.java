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
package eu.openanalytics.phaedra.measservice.service;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.Conditions;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ModelMapper {
    private final org.modelmapper.ModelMapper modelMapper = new org.modelmapper.ModelMapper();

    public ModelMapper() {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.createTypeMap(Measurement.class, MeasurementDTO.class)
                .setPropertyCondition(Conditions.isNotNull());
    }

    public MeasurementDTO map(Measurement measurement) {
        return modelMapper.map(measurement, MeasurementDTO.class);
    }

    public Measurement map(MeasurementDTO measurementDTO) {
        return modelMapper.map(measurementDTO, Measurement.class);
    }

    public Measurement map(Measurement measurement, MeasurementDTO measurementDTO) {
        if (StringUtils.isNotBlank(measurementDTO.getName())
                && !StringUtils.equals(measurement.getName(), measurementDTO.getName()))
            measurement.setName(measurementDTO.getName());

        if (StringUtils.isNotBlank(measurementDTO.getBarcode())
                && !StringUtils.equals(measurement.getBarcode(), measurementDTO.getBarcode()))
            measurement.setBarcode(measurementDTO.getBarcode());

        if (StringUtils.isNotBlank(measurementDTO.getDescription())
                && !StringUtils.equals(measurement.getDescription(), measurementDTO.getDescription()))
            measurement.setDescription(measurementDTO.getDescription());

        if (ArrayUtils.isNotEmpty(measurementDTO.getWellColumns())
                && !Arrays.equals(measurement.getWellColumns(), measurementDTO.getWellColumns()))
            measurement.setWellColumns(measurementDTO.getWellColumns());

        if (ArrayUtils.isNotEmpty(measurementDTO.getSubWellColumns())
                && !Arrays.equals(measurement.getSubWellColumns(), measurementDTO.getSubWellColumns()))
            measurement.setSubWellColumns(measurementDTO.getSubWellColumns());

        if (ArrayUtils.isNotEmpty(measurementDTO.getImageChannels())
                && !Arrays.equals(measurement.getImageChannels(), measurementDTO.getImageChannels()))
            measurement.setImageChannels(measurementDTO.getImageChannels());

        if (measurementDTO.getColumns() != null
                && !measurementDTO.getColumns().equals(measurement.getColumns()))
            measurement.setColumns(measurementDTO.getColumns());

        if (measurementDTO.getRows() != null
                && !measurementDTO.getRows().equals(measurement.getRows()))
            measurement.setRows(measurementDTO.getRows());

        return measurement;
    }
}
