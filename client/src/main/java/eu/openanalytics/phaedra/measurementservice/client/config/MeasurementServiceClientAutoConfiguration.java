package eu.openanalytics.phaedra.measurementservice.client.config;


import eu.openanalytics.phaedra.measurementservice.client.MeasurementServiceClient;
import eu.openanalytics.phaedra.measurementservice.client.impl.HttpMeasurementServiceClient;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeasurementServiceClientAutoConfiguration {

    @Bean
    public MeasurementServiceClient measurementServiceClient(PhaedraRestTemplate phaedraRestTemplate) {
        return new HttpMeasurementServiceClient(phaedraRestTemplate);
    }

}
