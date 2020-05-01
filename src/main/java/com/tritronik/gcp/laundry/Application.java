package com.tritronik.gcp.laundry;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "30s")
@ComponentScan(basePackages = { "com.tritronik.gcp.laundry" })
@EnableJpaRepositories(basePackages = { "com.tritronik.gcp.laundry" })
@EntityScan(basePackages = { "com.tritronik.gcp.laundry" })
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	private DataSource dataSource;

	@Bean
	public Logger loggerController() {
		Logger logger = LoggerFactory.getLogger("Controller");
		return logger;
	}

	@Bean
	public Logger loggerUtil() {
		Logger logger = LoggerFactory.getLogger("Util");
		return logger;
	}

	@Bean
	public LockProvider lockProvider() {
		return new JdbcTemplateLockProvider(
				JdbcTemplateLockProvider.Configuration.builder()
				.withJdbcTemplate(new JdbcTemplate(dataSource))
				.usingDbTime()
				.build()
				);
	}
}
