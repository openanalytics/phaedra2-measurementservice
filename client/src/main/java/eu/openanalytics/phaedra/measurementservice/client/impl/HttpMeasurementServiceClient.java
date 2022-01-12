package eu.openanalytics.phaedra.measurementservice.client.impl;

import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measurementservice.client.MeasurementServiceClient;
import eu.openanalytics.phaedra.measurementservice.client.exception.MeasUnresolvableException;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

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
    public MeasurementDTO[] getMeasurements(long ...measIds) {
        if (measIds != null) {
            return restTemplate.getForObject(UrlFactory.getMeasurementsByMeasIds(measIds), MeasurementDTO[].class);
        }
        return restTemplate.getForObject(UrlFactory.getAllMeasurements(), MeasurementDTO[].class);
    }
}
