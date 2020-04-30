package com.tritronik.gcp.laundry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

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
}
