package eu.openanalytics.phaedra.measservice;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import eu.openanalytics.phaedra.util.jdbc.JDBCUtils;

@SpringBootApplication
public class MeasServiceApplication {

	private static final String PROP_DB_URL = "db.url";
	private static final String PROP_DB_USERNAME = "db.username";
	private static final String PROP_DB_PASSWORD = "db.password";
		  	
	@Autowired
	private Environment environment;
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(MeasServiceApplication.class);
		app.run(args);
	}
	
//	@Bean
	public DataSource plateDataSource() {
		String url = environment.getProperty(PROP_DB_URL);
		if (StringUtils.isEmpty(url)) {
			throw new RuntimeException("No database URL configured: " + PROP_DB_URL);
		}
		String driverClassName = JDBCUtils.getDriverClassName(url);
		if (driverClassName == null) {
			throw new RuntimeException("Unsupported database type: " + url);
		}
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(environment.getProperty(PROP_DB_USERNAME));
		dataSource.setPassword(environment.getProperty(PROP_DB_PASSWORD));
		return dataSource;
	}

}
