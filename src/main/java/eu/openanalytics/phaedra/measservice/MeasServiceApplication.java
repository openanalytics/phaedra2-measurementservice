package eu.openanalytics.phaedra.measservice;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import eu.openanalytics.phaedra.util.jdbc.JDBCUtils;

@SpringBootApplication
public class MeasServiceApplication {

	private static final String PROP_DB_URL = "db.url";
	private static final String PROP_DB_USERNAME = "db.username";
	private static final String PROP_DB_PASSWORD = "db.password";
	private static final String PROP_DB_SCHEMA = "meas-service.db.schema";
	
	@Autowired
	private Environment environment;
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(MeasServiceApplication.class);
		app.run(args);
	}
	
	@Bean
	public DataSource dataSource() {
		String url = environment.getProperty(PROP_DB_URL);
		if (StringUtils.isEmpty(url)) {
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
		if (!StringUtils.isEmpty(schema)) {
			config.setConnectionInitSql("set search_path to " + schema);
		}
		
		return new HikariDataSource(config);
	}

}
