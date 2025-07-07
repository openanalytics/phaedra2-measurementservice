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
package eu.openanalytics.phaedra.measurementservice.client.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class UrlFactory {

    private String baseURL;
    
    public UrlFactory(String baseURL) {
    	this.baseURL = baseURL;
	}
    
    public String measurementWellData(long measId, String columnName) {
        return String.format("%s/measurements/%s/welldata/%s", baseURL, measId, columnName);
    }

    public String measurementSubWellData(long measId, String columnName) {
        return String.format("%s/measurements/%s/subwelldata/%s", baseURL, measId, columnName);
    }

    public String getMeasurementsByMeasIds(long... measIds) {
        String url = String.format("%s/measurements", baseURL);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("ids", StringUtils.join(measIds,','));
        return builder.build().toString();
    }

    public String getMeasurementsByMeasId(long measId) {
        return String.format("%s/measurements/%s", baseURL, measId);
    }

    public String getAllMeasurements() {
        return String.format("%s/measurements", baseURL);
    }
}
