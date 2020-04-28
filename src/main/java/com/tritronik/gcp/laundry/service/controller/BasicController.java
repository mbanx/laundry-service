package com.tritronik.gcp.laundry.service.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tritronik.gcp.laundry.service.props.ServiceProperties;

@RestController
@RequestMapping("/basic")
public class BasicController {

	@Autowired
	private ServiceProperties serviceProperties;
	
	@GetMapping("/configuration/default")
	@ResponseBody
	public Map<String, Object> getDefaultConfiguration() {
		Map<String, Object> response = new HashMap<>();
		response.put("basePrice", serviceProperties.getBasePrice());
		response.put("basePriceUnit", serviceProperties.getBasePriceUnit());
		return response;
	}
	
}
