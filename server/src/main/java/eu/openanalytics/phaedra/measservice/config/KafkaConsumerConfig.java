package eu.openanalytics.phaedra.measservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    public static final String SAVE_SUBWELL_DATA_EVENT = "requestCreateSubwellData";
    public static final String MEASUREMENTS_TOPIC = "measurements";

    @Bean
    public RecordFilterStrategy<String, Object> saveSubwellDataEventFilter() {
        RecordFilterStrategy<String, Object> recordFilterStrategy = consumerRecord -> !(consumerRecord.key().equalsIgnoreCase(SAVE_SUBWELL_DATA_EVENT));
        return recordFilterStrategy;
    }
}
