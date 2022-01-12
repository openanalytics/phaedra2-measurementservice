package eu.openanalytics.phaedra.measurementservice.client.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measurementservice.client.MeasurementServiceClient;
import eu.openanalytics.phaedra.measurementservice.client.exception.MeasUnresolvableException;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CachingHttpMeasurementServiceClient implements MeasurementServiceClient {

    private final HttpMeasurementServiceClient httpMeasurementServiceClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private record WellDataKey(long measId, String columnName) {};
    private final Cache<WellDataKey, float[]> wellDataCache;

    public CachingHttpMeasurementServiceClient(PhaedraRestTemplate restTemplate) {
        httpMeasurementServiceClient = new HttpMeasurementServiceClient(restTemplate);
        wellDataCache = Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterAccess(Duration.ofHours(1))
                .build();
    }

    @Override
    public float[] getWellData(long measId, String columnName) throws MeasUnresolvableException {
        var key = new WellDataKey(measId, columnName);
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
    public MeasurementDTO[] getMeasurements(long... measId) {
        return httpMeasurementServiceClient.getMeasurements(measId);
    }

}
