package eu.openanalytics.phaedra.measservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConfig {
    public static final String GROUP_ID = "meas-service";

    // Topics
    public static final String TOPIC_MEASUREMENTS = "measurements";

    // Events
    public static final String EVENT_SAVE_SUBWELL_DATA = "saveSubwellData";
    public static final String EVENT_SAVE_WELL_DATA = "saveWellData";
}
