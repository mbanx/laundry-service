package com.tritronik.gcp.laundry.service.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tritronik.gcp.laundry.service.props.ServiceProperties;

@RestController
@RequestMapping("/basic")
public class BasicController {

	private Logger logger = LoggerFactory.getLogger("");
	
	@Autowired
	private ServiceProperties serviceProperties;
	
	@Autowired
	private Logger loggerController;
	
	@Autowired
	private Logger loggerUtil;
	
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
	
}
