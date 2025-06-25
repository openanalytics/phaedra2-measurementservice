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
package eu.openanalytics.phaedra.measservice;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import eu.openanalytics.phaedra.imaging.render.ImageRenderService;
import eu.openanalytics.phaedra.metadataservice.client.config.MetadataServiceClientAutoConfiguration;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import eu.openanalytics.phaedra.util.auth.AuthenticationConfigHelper;
import eu.openanalytics.phaedra.util.auth.AuthorizationServiceFactory;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;
import eu.openanalytics.phaedra.util.jdbc.JDBCUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import javax.sql.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableScheduling
@EnableCaching
@EnableWebSecurity
@SpringBootApplication
@Import({MetadataServiceClientAutoConfiguration.class})
public class MeasServiceApplication {

	private final Environment environment;

	public MeasServiceApplication(Environment environment) {
		this.environment = environment;
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(MeasServiceApplication.class);
		app.run(args);
	}

	@Bean
	public DataSource dataSource() {
		return JDBCUtils.createDataSource(environment);
	}

	@Bean
	public AmazonS3 amazonS3() {
		String endpoint = environment.getProperty("S3_ENDPOINT", "https://s3.amazonaws.com");
		String region = environment.getProperty("S3_REGION", "eu-west-1");
		String username = environment.getProperty("S3_USERNAME");
		String password = environment.getProperty("S3_PASSWORD");

		AWSCredentialsProvider credentialsProvider;
		if (username != null && password != null) {
			credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(username, password));
		} else {
			credentialsProvider = new DefaultAWSCredentialsProviderChain();
		}

		return AmazonS3ClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
				.withCredentials(credentialsProvider)
				.enablePathStyleAccess()
				.build();
	}

	@Bean
	public OpenAPI customOpenAPI() {
		Server server = new Server().url(environment.getProperty("API_URL")).description("Default Server URL");
		return new OpenAPI().addServersItem(server);
	}

	@Bean
	public ImageRenderService renderService() {
		return new ImageRenderService();
	}

	@Bean
	public IAuthorizationService authService() {
		return AuthorizationServiceFactory.create();
	}

	@Bean
	public SecurityFilterChain httpSecurity(HttpSecurity http) throws Exception {
		return AuthenticationConfigHelper.configure(http);
	}

	@Bean
	public PhaedraRestTemplate restTemplate() {
		return new PhaedraRestTemplate();
	}
}
