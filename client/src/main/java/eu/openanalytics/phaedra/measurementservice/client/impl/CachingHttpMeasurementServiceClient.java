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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measurementservice.client.MeasurementServiceClient;
import eu.openanalytics.phaedra.measurementservice.client.exception.MeasUnresolvableException;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class CachingHttpMeasurementServiceClient implements MeasurementServiceClient {

    private final HttpMeasurementServiceClient httpMeasurementServiceClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private record CacheDataKey(long measId, String columnName) {};
    private final Cache<CacheDataKey, float[]> wellDataCache;
    private final Cache<CacheDataKey, Map<Integer, float[]>> subWellDataCache;

    public CachingHttpMeasurementServiceClient(PhaedraRestTemplate restTemplate, IAuthorizationService authService) {
        httpMeasurementServiceClient = new HttpMeasurementServiceClient(restTemplate, authService);
        wellDataCache = Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterAccess(Duration.ofHours(1))
                .build();
        subWellDataCache = Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterAccess(Duration.ofHours(1))
                .build();
    }

    @Override
    public float[] getWellData(long measId, String columnName) throws MeasUnresolvableException {
        var key = new CacheDataKey(measId, columnName);
        var res = wellDataCache.getIfPresent(key);
        if (res == null) {
            res = httpMeasurementServiceClient.getWellData(measId, columnName);
            wellDataCache.put(key, res);
        } else {
            logger.info(String.format("Retrieved object from cache: WellData measId=%s, columName=%s",  measId, columnName));
        }
        return res;
    }

    @Override
    public Map<Integer, float[]> getSubWellData(long measId, String columnName) throws MeasUnresolvableException {
    	 var key = new CacheDataKey(measId, columnName);
         var res = subWellDataCache.getIfPresent(key);
         if (res == null) {
             res = httpMeasurementServiceClient.getSubWellData(measId, columnName);
             subWellDataCache.put(key, res);
         } else {
             logger.info(String.format("Retrieved object from cache: SubWellData measId=%s, columName=%s",  measId, columnName));
         }
         return res;
    }
    
    @Override
    public List<MeasurementDTO> getMeasurementsByMeasIds(long... measId) {
        return httpMeasurementServiceClient.getMeasurementsByMeasIds(measId);
    }

}
