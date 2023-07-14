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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measurementservice.client.MeasurementServiceClient;
import eu.openanalytics.phaedra.measurementservice.client.exception.MeasUnresolvableException;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;

@Component
public class HttpMeasurementServiceClient implements MeasurementServiceClient {

    private final PhaedraRestTemplate restTemplate;
    private final IAuthorizationService authService;

    public HttpMeasurementServiceClient(PhaedraRestTemplate restTemplate, IAuthorizationService authService) {
        this.restTemplate = restTemplate;
        this.authService = authService;
    }

    @Override
    public float[] getWellData(long measId, String columnName) throws MeasUnresolvableException {
        try {
            var res = restTemplate.exchange(UrlFactory.measurementWell(measId, columnName), HttpMethod.GET,
            		new HttpEntity<>(makeHttpHeaders()), float[].class);
            if (res == null) throw new MeasUnresolvableException("WellData could not be converted");
            return res.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new MeasUnresolvableException("WellData not found");
        } catch (HttpClientErrorException ex) {
            throw new MeasUnresolvableException("Error while fetching WellData");
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    public Map<Integer, float[]> getSubWellData(long measId, String columnName) throws MeasUnresolvableException {
    	try {
            var res = restTemplate.exchange(UrlFactory.measurementWell(measId, columnName), HttpMethod.GET,
            		new HttpEntity<>(makeHttpHeaders()), Map.class);
            if (res == null) throw new MeasUnresolvableException("WellData could not be converted");
            return res.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new MeasUnresolvableException("WellData not found");
        } catch (HttpClientErrorException ex) {
            throw new MeasUnresolvableException("Error while fetching WellData");
        }
    }

    @Override
    public MeasurementDTO getMeasurementByMeasId(long measId) {
        var response = restTemplate.exchange(UrlFactory.getMeasurementsByMeasId(measId), HttpMethod.GET,
                new HttpEntity<>(makeHttpHeaders()), MeasurementDTO.class);
        return response.getBody();
    }

    @Override
    public List<MeasurementDTO> getMeasurementsByMeasIds(long ...measIds) {
        if (measIds != null) {
            var res = restTemplate.exchange(UrlFactory.getMeasurementsByMeasIds(measIds), HttpMethod.GET,
            		new HttpEntity<>(makeHttpHeaders()), MeasurementDTO[].class);
            MeasurementDTO[] measurements = res.getBody();
            return Arrays.asList(measurements);
        }
        return new ArrayList<>();
    }

    private HttpHeaders makeHttpHeaders() {
    	HttpHeaders httpHeaders = new HttpHeaders();
        String bearerToken = authService.getCurrentBearerToken();
    	if (bearerToken != null) httpHeaders.set(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", bearerToken));
    	return httpHeaders;
    }
}
