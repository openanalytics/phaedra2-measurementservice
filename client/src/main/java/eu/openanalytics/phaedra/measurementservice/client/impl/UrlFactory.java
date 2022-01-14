package eu.openanalytics.phaedra.measurementservice.client.impl;

import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

public class UrlFactory {

    private static final String MEAS_SERVICE = "http://phaedra-measurement-service/phaedra/measurement-service";

    public static String measurementWell(long measId, String columnName) {
        return String.format("%s/meas/%s/welldata/%s", MEAS_SERVICE, measId, columnName);
    }

    public static String getMeasurementsByMeasIds(long ...measIds) {
        String url = new StringBuilder(MEAS_SERVICE).append("/meas").toString();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("measIds", measIds);
        return builder.build().toString();
    }

    public static String getAllMeasurements() {
        return new StringBuilder(MEAS_SERVICE).append("/meas").toString();
    }
}
