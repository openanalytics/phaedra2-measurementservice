package eu.openanalytics.phaedra.measservice.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import eu.openanalytics.phaedra.measservice.api.dto.NewMeasurementDTO;
import eu.openanalytics.phaedra.measservice.dto.MeasurementDTO;
import eu.openanalytics.phaedra.measservice.model.Measurement;
import eu.openanalytics.phaedra.measservice.support.Containers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@Sql({"/jdbc/test-data.sql"})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(locations = "classpath:application-test.properties")
public class MeasurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", Containers.postgreSQLContainer::getJdbcUrl);
        registry.add("DB_USERNAME", Containers.postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", Containers.postgreSQLContainer::getPassword);
        registry.add("DB_SCHEMA", () -> "measservice");

        registry.add("S3_ENDPOINT", () -> "https://s3.amazonaws.com");
        registry.add("S3_REGION", () -> "eu-west-1");
        registry.add("S3_USERNAME", () -> "test");
        registry.add("S3_PASSWORD", () -> "test");
        registry.add("S3_BUCKET", () -> "phaedra2-poc-measdata");
    }

    @Test
    public void measurementsGetTest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/meas"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<Measurement> measurements = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), List.class);
        assertThat(measurements).isNotNull();
        assertThat(measurements.size()).isEqualTo(4);
    }

    @Test
    public void measurementGetTest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/meas/{measId}", 1000L))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        Measurement measurement = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Measurement.class);
        assertThat(measurement.getId()).isEqualTo(1000L);
        assertThat(measurement.getBarcode()).isEqualTo("SBETST0001");
    }

    @Test
    public void measurementPostTest() throws Exception {
        Measurement measurement = new Measurement();
        measurement.setName("test");
        measurement.setBarcode("barcode");
        measurement.setRows(20);
        measurement.setColumns(30);
        measurement.setCreatedBy("smarien");
        measurement.setCreatedOn(new Date());
        measurement.setCaptureJobId(50L);

        String requestBody = objectMapper.writeValueAsString(measurement);

        MvcResult mvcResult = this.mockMvc.perform(post("/meas").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MeasurementDTO measurementDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), MeasurementDTO.class);
        assertThat(measurementDTO).isNotNull();
        assertThat(measurementDTO.getId()).isEqualTo(1L);
    }

//    @Test
    public void measurementPostTest2() throws Exception {
        String jsonMeasurement = Files.readString(Paths.get(getClass().getClassLoader().getResource("testdata/test_measurement.json").toURI()));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) throws IOException {
                return new Double(-1).floatValue();
            }

            @Override
            public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetType, Number valueToConvert, String failureMsg) throws IOException {
                return -1;
            }
        });

        NewMeasurementDTO measurementDTO = objectMapper.readValue(jsonMeasurement, NewMeasurementDTO.class);
        assertThat(measurementDTO).isNotNull();
    }

    //Errors on deleteSubbwellData in S3

    /*@Test
    public void deleteMeasurementById() throws Exception {
        Long id = 1000L;

        this.mockMvc.perform(delete("/meas/{meas}", id))
                .andDo(print())
                .andExpect(status().isOk());
    }*/

    @Test
    public void deleteMeasurementByCaptureJobId() throws Exception{
        MvcResult mvcResult = this.mockMvc.perform(get("/meas"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<Measurement> measurements = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), List.class);
        assertThat(measurements).isNotNull();
        assertThat(measurements.size()).isEqualTo(4);

        this.mockMvc.perform(delete("/meas/capture-job/{captureJobId}", 1L))
                .andDo(print())
                .andExpect(status().isNoContent());

        MvcResult mvcResult2 = this.mockMvc.perform(get("/meas"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<Measurement> measurements2 = objectMapper.readValue(mvcResult2.getResponse().getContentAsString(), List.class);
        assertThat(measurements2.isEmpty()).isFalse();
    }

    @Test
    public void deleteMeasurementByCaptureJobIdNotFound() throws Exception{
        this.mockMvc.perform(delete("/meas/capture-job/{captureJobId}", 2L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

//    @Test
//    public void getMeasurementsByListOfMeasIdsTest() throws Exception {
//        this.mockMvc.perform(get("/meas")
//                        .param("measIds", "1000L, 2000L"))
//                .andDo(print())
//                .andExpect(status().isOk());
//    }


}
