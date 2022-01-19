package eu.openanalytics.phaedra.measservice.repository;

import eu.openanalytics.phaedra.measservice.model.Measurement;
import eu.openanalytics.phaedra.measservice.support.Containers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Sql({"/jdbc/test-data.sql"})
@TestPropertySource(locations = "classpath:application-test.properties")
public class MeasRepositoryTest {

    @Autowired
    private MeasRepository measRepository;

    @Container
    private static JdbcDatabaseContainer postgreSQLContainer = new PostgreSQLContainer("postgres:13-alpine")
            .withDatabaseName("phaedra2")
            .withUrlParam("currentSchema","measservice")
            .withPassword("inmemory")
            .withUsername("inmemory");

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
    public void contextLoads() {
        assertThat(measRepository).isNotNull();
    }

    @Test
    public void findAllByIdsTest() {
        List<Measurement> measurements = measRepository.findAllByIds(new long[]{1000L,2000L,3000L});
        assertThat(measurements.isEmpty()).isFalse();
        assertThat(measurements.size()).isEqualTo(3);
    }

    @Test
    public void getByCaptureJobId() {
        List<Measurement> measurements = measRepository.getMeasurementByCaptureJobId(1L);
        assertThat(measurements.size()).isEqualTo(3);
    }
}
