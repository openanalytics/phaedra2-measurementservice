package eu.openanalytics.phaedra.measurementservice.client.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlFactoryTest {

    @Test
    public void getMeasurementsByMeasIds() {
        String url = UrlFactory.getMeasurementsByMeasIds(1, 2, 3, 4);
        assertThat(url).isEqualTo("http://phaedra-measurement-service/phaedra/measurement-service/meas?measIds=1,2,3,4");
    }

    @Test
    public void getAllMeasurements() {
        String url = UrlFactory.getAllMeasurements();
        assertThat(url).isEqualTo("http://phaedra-measurement-service/phaedra/measurement-service/meas");
    }

}
