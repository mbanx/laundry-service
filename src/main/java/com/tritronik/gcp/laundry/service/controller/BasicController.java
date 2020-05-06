package com.tritronik.gcp.laundry.service.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.tritronik.gcp.laundry.Application;
import com.tritronik.gcp.laundry.Application.PubsubOutboundGateway;
import com.tritronik.gcp.laundry.service.props.ServiceProperties;

import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;


@RefreshScope
@RestController
@RequestMapping("/basic")
public class BasicController {

	@Autowired
	private ServiceProperties serviceProperties;

	@Autowired
	private Logger loggerController;

	@Autowired
	private Logger loggerUtil;
	
	@Autowired
	private Storage cloudStorage;
	
	@Autowired
	private PubsubOutboundGateway messagingGateway;

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/instance/info")
	@ResponseBody
	public String getInstanceInformation() {
		String methodName = new Throwable().getStackTrace()[0].getMethodName();

		loggerUtil.info("{} - finish... ", methodName);
		return "test";
	}

	@GetMapping("/configuration/default")
	@ResponseBody
	public Map<String, Object> getDefaultConfiguration() {
		String methodName = new Throwable().getStackTrace()[0].getMethodName();

		Map<String, Object> response = new HashMap<>();
		response.put("basePrice", serviceProperties.getBasePrice());
		response.put("basePriceUnit", serviceProperties.getBasePriceUnit());
		response.put("baseType", serviceProperties.getBaseType());
		loggerController.info("{} - finish... ", methodName);

		return response;
	}

	@Scheduled(fixedDelayString = "#{@serviceProperties.baseFixRateDelay}")
	@SchedulerLock(name = "scheduleFixedDelayTask", lockAtMostFor = "#{@serviceProperties.baseLockAtMostFor}", lockAtLeastFor = "#{@serviceProperties.baseLockAtLeastFor}")
	public void scheduleFixedDelayTask() {
		// To assert that the lock is held (prevents misconfiguration errors)
		LockAssert.assertLocked();

		String methodName = new Throwable().getStackTrace()[0].getMethodName();
		loggerController.info("{} trigger using spring scheduler", methodName);
	}

	@PostMapping("/postMessage")
	public String publishMessage(@RequestParam("message") String message) {
		messagingGateway.sendToPubsub(message);
		return message;
	}

	@GetMapping("/pullViaAPI")
	@ResponseBody
	public String pullViaAPI() {
		String methodName = new Throwable().getStackTrace()[0].getMethodName();
		loggerController.info("{} start... ", methodName);

		String response = "";

		String url = "http://cra.tritronik.com/user/search?sortBy=username&sortOrder=ASC&page=0&limit=10";
		HttpMethod method = HttpMethod.GET;

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
		String urlWithParam = uriBuilder.toUriString();

		HttpHeaders headers = new HttpHeaders();

		JSONObject body = new JSONObject();
		HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

		ResponseEntity<String> responseFromApi = restTemplate.exchange(urlWithParam, method, entity, String.class);
		if(responseFromApi != null) {
			response = responseFromApi.getBody();
		}

		loggerController.info("{} finish... response={}", methodName, response);
		return response;
	}

	@PostMapping("/uploadToCloudStorage")
	@ResponseBody
	public void uploadToCloudStorage(@RequestParam("file") MultipartFile uploadFile) throws IOException {
		String srcFileName = uploadFile.getOriginalFilename();
		byte[] srcFileBytes = uploadFile.getBytes();

		String projectId = Application.GCP_PROJECT_ID;
		String bucketName = Application.GCS_BUCKET;
		String objectName = "laundry-service/"+srcFileName;

		Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
		BlobId blobId = BlobId.of(bucketName, objectName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		Blob blob = storage.create(blobInfo, srcFileBytes);

		loggerController.info(String.valueOf(blob));
	}

	@GetMapping("/downloadFromCloudStorage/{blobName}")
	public ResponseEntity<Resource> downloadFromCloudStorage(
			@RequestParam("blobName") String blobName) throws FileNotFoundException, IOException {
		

		String bucketName = Application.GCS_BUCKET;
		BlobId blobId = BlobId.of(bucketName, blobName);
		loggerController.info(blobId.getBucket());

		Blob blob = cloudStorage.get(blobId);
		String filename = FilenameUtils.getName(blobName);

		Path path = Paths.get(Application.DOWNLOAD_TEMP_DIR, filename);
		blob.downloadTo(path);
		
		File file = path.toFile();
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(resource);
		
	}
}
