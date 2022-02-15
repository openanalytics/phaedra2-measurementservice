/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import eu.openanalytics.phaedra.util.jdbc.JDBCUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

@EnableDiscoveryClient
@EnableScheduling
// Temporary disable security autoconfiguration for measurement-service
// TODO: When authorisation is tested and ready to be integrated into other services remove the exclude property
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class MeasServiceApplication {

	private static final String PROP_DB_URL = "meas-service.db.url";
	private static final String PROP_DB_USERNAME = "meas-service.db.username";
	private static final String PROP_DB_PASSWORD = "meas-service.db.password";
	private static final String PROP_DB_SCHEMA = "meas-service.db.schema";

	private static final String PROP_S3_ENDPOINT = "meas-service.s3.endpoint";
	private static final String PROP_S3_REGION = "meas-service.s3.region";
	private static final String PROP_S3_USERNAME = "meas-service.s3.username";
	private static final String PROP_S3_PASSWORD = "meas-service.s3.password";

	private final ServletContext servletContext;
	private final Environment environment;

	public MeasServiceApplication(ServletContext servletContext, Environment environment) {
		this.servletContext = servletContext;
		this.environment = environment;
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(MeasServiceApplication.class);
		app.run(args);
	}

	@Bean
	public DataSource dataSource() {
		String url = environment.getProperty(PROP_DB_URL);
		if (url == null || url.trim().isEmpty()) {
			throw new RuntimeException("No database URL configured: " + PROP_DB_URL);
		}
		String driverClassName = JDBCUtils.getDriverClassName(url);
		if (driverClassName == null) {
			throw new RuntimeException("Unsupported database type: " + url);
		}

		HikariConfig config = new HikariConfig();
		config.setAutoCommit(false);
		config.setMaximumPoolSize(20);
		config.setConnectionTimeout(60000);
		config.setJdbcUrl(url);
		config.setDriverClassName(driverClassName);
		config.setUsername(environment.getProperty(PROP_DB_USERNAME));
		config.setPassword(environment.getProperty(PROP_DB_PASSWORD));

		String schema = environment.getProperty(PROP_DB_SCHEMA);
		if (schema != null && !schema.trim().isEmpty()) {
			config.setConnectionInitSql("set search_path to " + schema);
		}

		return new HikariDataSource(config);
	}

	@Bean
	public AmazonS3 amazonS3() {
		String endpoint = environment.getProperty(PROP_S3_ENDPOINT, "https://s3.amazonaws.com");
		String region = environment.getProperty(PROP_S3_REGION, "eu-west-1");
		String username = environment.getProperty(PROP_S3_USERNAME);
		String password = environment.getProperty(PROP_S3_PASSWORD);

		return AmazonS3ClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(username, password)))
				.enablePathStyleAccess()
				.build();
	}

	@Bean
	public OpenAPI customOpenAPI() {
		Server server = new Server().url(servletContext.getContextPath()).description("Default Server URL");
		return new OpenAPI().addServersItem(server);
	}
}
