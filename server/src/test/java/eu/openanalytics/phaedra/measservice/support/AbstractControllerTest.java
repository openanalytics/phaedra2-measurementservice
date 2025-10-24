/**
 * Phaedra II
 *
 * Copyright (C) 2016-2025 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.measservice.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.metadataservice.client.MetadataServiceClient;
import eu.openanalytics.phaedra.util.auth.ClientCredentialsTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql({"/jdbc/test-data.sql"})
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
})
@TestPropertySource(locations = "classpath:application-test.properties")
abstract public class AbstractControllerTest {

    @LocalServerPort
    private int port;

    protected WebTestClient restWebTestClient;
    protected HttpGraphQlTester httpGraphQlTester;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private ClientCredentialsTokenGenerator ccTokenGenerator;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", Containers.postgreSQLContainer::getJdbcUrl);
        registry.add("DB_USER", Containers.postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", Containers.postgreSQLContainer::getPassword);

        registry.add("S3_ENDPOINT", () -> "https://s3.amazonaws.com");
        registry.add("S3_REGION", () -> "eu-west-1");
        registry.add("S3_USERNAME", () -> "test");
        registry.add("S3_PASSWORD", () -> "test");
        registry.add("S3_BUCKET", () -> "phaedra2-poc-measdata");
    }

    @BeforeEach
    public void setUp() {
        String baseUrl = "http://localhost:" + port + "/phaedra/measurement-service";
        WebTestClient.Builder baseClientBuilder = WebTestClient
                .bindToServer()
                .baseUrl(baseUrl);

        this.restWebTestClient = baseClientBuilder.build();

        WebTestClient graphqlWebTestClient = baseClientBuilder
                .baseUrl(baseUrl + "/graphql")
                .build();
        this.httpGraphQlTester = HttpGraphQlTester.create(graphqlWebTestClient);
    }
}
