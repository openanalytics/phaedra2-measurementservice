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
package eu.openanalytics.phaedra.measurementservice.client;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measurementservice.client.exception.MeasUnresolvableException;

import java.util.List;
import java.util.Map;

public interface MeasurementServiceClient {

    float[] getWellData(long measId, String columnName) throws MeasUnresolvableException;
    Map<Integer, float[]> getSubWellData(long measId, String columnName) throws MeasUnresolvableException;
    
    List<MeasurementDTO> getMeasurementsByMeasIds(long ...measId);

}
