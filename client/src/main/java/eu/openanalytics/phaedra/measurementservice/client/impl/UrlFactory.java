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
package eu.openanalytics.phaedra.measurementservice.client.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class UrlFactory {

    private static final String MEAS_SERVICE = "http://phaedra-measurement-service:8080/phaedra/measurement-service";

    public static String measurementWellData(long measId, String columnName) {
        return String.format("%s/measurements/%s/welldata/%s", MEAS_SERVICE, measId, columnName);
    }
    
    public static String measurementSubWellData(long measId, String columnName) {
        return String.format("%s/measurements/%s/subwelldata/%s", MEAS_SERVICE, measId, columnName);
    }

    public static String getMeasurementsByMeasIds(long... measIds) {
        String url = new StringBuilder(MEAS_SERVICE).append("/measurements").toString();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("ids", StringUtils.join(measIds,','));
        return builder.build().toString();
    }

    public static String getAllMeasurements() {
        return new StringBuilder(MEAS_SERVICE).append("/measurements").toString();
    }
}
