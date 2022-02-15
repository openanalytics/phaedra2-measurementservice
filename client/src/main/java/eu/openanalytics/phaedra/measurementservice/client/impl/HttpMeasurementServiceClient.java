/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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
package eu.openanalytics.phaedra.measurementservice.client.impl;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measurementservice.client.MeasurementServiceClient;
import eu.openanalytics.phaedra.measurementservice.client.exception.MeasUnresolvableException;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class HttpMeasurementServiceClient implements MeasurementServiceClient {

    private final PhaedraRestTemplate restTemplate;

    public HttpMeasurementServiceClient(PhaedraRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public float[] getWellData(long measId, String columnName) throws MeasUnresolvableException {
        try {
            var wellData = restTemplate.getForObject(UrlFactory.measurementWell(measId, columnName), float[].class);
            if (wellData == null) {
                throw new MeasUnresolvableException("WellData could not be converted");
            }
            return wellData;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new MeasUnresolvableException("WellData not found");
        } catch (HttpClientErrorException ex) {
            throw new MeasUnresolvableException("Error while fetching WellData");
        }
    }

    @Override
    public List<MeasurementDTO> getMeasurementsByMeasIds(long ...measIds) {
        if (measIds != null) {
            return Arrays.asList(restTemplate.getForObject(UrlFactory.getMeasurementsByMeasIds(measIds), MeasurementDTO[].class));
        }
        return new ArrayList<>();
    }
}
