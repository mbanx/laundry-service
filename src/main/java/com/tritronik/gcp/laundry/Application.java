package com.tritronik.gcp.laundry;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

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

	public static final String GCP_PROJECT_ID = "dev-tritronik";
	public static final String GCS_BUCKET = "dev-tritronik";
	public static final String PUBSUB_INPUT_CHANNEL = "pubsubInputChannel";
	public static final String PUBSUB_OUTPUT_CHANNEL = "pubsubOutputChannel";
	public static final String PUBSUB_TOPIC = "laundry-service";
	public static final String DOWNLOAD_TEMP_DIR = "D:\\TRITRONIK\\DATA - BDA\\GCP";

	@Autowired
	private Logger loggerMain;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	private DataSource dataSource;

	@Bean
	public Logger loggerMain() {
		Logger logger = LoggerFactory.getLogger("Main");
		return logger;
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

	@Bean
	public LockProvider lockProvider() {
		return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
				.withJdbcTemplate(new JdbcTemplate(dataSource)).usingDbTime().build());
	}

	@Bean
	public Storage cloudStorage() throws IOException {
		
		Credentials credentials = null;
		//Credential file access from class path
		{
			ClassLoader classLoader = getClass().getClassLoader();
			String credentialFilePath = "cloud-storage-admin-sa.json";
			InputStream inputStream = classLoader.getResourceAsStream(credentialFilePath);
			credentials = GoogleCredentials.fromStream(inputStream);
		}

		//Credential file access from absolute path
		/*
		{
			String credentialFilePath = "C:\\Users\\62856\\Desktop\\GCP\\cloud-storage-admin-sa.json";
			credentials = GoogleCredentials.fromStream(new FileInputStream(credentialFilePath));
		}
		*/
		
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(GCP_PROJECT_ID).build()
				.getService();
		return storage;
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter(
			@Qualifier(PUBSUB_INPUT_CHANNEL) MessageChannel inputChannel, PubSubTemplate pubSubTemplate) {
		PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate,
				"laundry-service-subscription");
		adapter.setOutputChannel(inputChannel);
		adapter.setAckMode(AckMode.AUTO);

		return adapter;
	}

	@Bean
	public MessageChannel pubsubInputChannel() {
		return new DirectChannel();
	}


	@ServiceActivator(inputChannel = PUBSUB_INPUT_CHANNEL)
	public void messageReceiver(String payload) {
		loggerMain.info("Message arrived! Payload: " + payload);
	}

	@Bean
	@ServiceActivator(inputChannel = PUBSUB_OUTPUT_CHANNEL)
	public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
		return new PubSubMessageHandler(pubsubTemplate, PUBSUB_TOPIC);
	}

	@MessagingGateway(defaultRequestChannel = PUBSUB_OUTPUT_CHANNEL)
	public interface PubsubOutboundGateway {
		void sendToPubsub(String text);
	}
}
