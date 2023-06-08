package eu.openanalytics.phaedra.measservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wildfly.common.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.measservice.api.dto.NewMeasurementDTO;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.dto.WellDataDTO;
import eu.openanalytics.phaedra.measservice.exception.MeasurementConsumerException;
import eu.openanalytics.phaedra.measservice.service.MeasKafkaConsumer;
import eu.openanalytics.phaedra.measservice.service.MeasService;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(MockitoExtension.class)
//@Testcontainers
//@SpringBootTest
//@Sql({"/jdbc/test-data.sql"})
//@TestPropertySource(locations = "classpath:application-test.properties")
class MeasKafkaConsumerTest {

    @Mock private MeasService measService;
    @InjectMocks private MeasKafkaConsumer measKafkaConsumer;

    private ObjectMapper objectMapper;
    private String wellDataJson;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        wellDataJson =
                "{\"measurementId\": 1, \"column\": \"TestColumn\", \"data\": [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]}";
    }

    @Test
    void onNewMeasurement_handlesEventWithSaveMeasurementKey() throws MeasurementConsumerException {
        // given
        NewMeasurementDTO newMeasurementDTO = new NewMeasurementDTO();
        String msgKey = "saveMeasurement";

        // when
        measKafkaConsumer.onNewMeasurement(newMeasurementDTO, msgKey);

        // then
        verify(measService).createNewMeas(any());
    }

//    @Test
    void onSaveWellData_handlesEventWithSaveWellDataKey() throws JsonProcessingException {
        // given
        MeasurementDTO measurementDTO = new MeasurementDTO();
        measurementDTO.setId(1L);
        when(measService.findMeasById(measurementDTO.getId())).thenReturn(Optional.of(measurementDTO));

        // when
        measKafkaConsumer.onSaveWellData(wellDataJson);

        // then
        verify(measService).setMeasWellData(measurementDTO.getId(),"TestColumn", new float[] {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f});
    }
}
