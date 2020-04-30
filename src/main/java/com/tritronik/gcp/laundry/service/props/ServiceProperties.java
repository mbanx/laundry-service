package com.tritronik.gcp.laundry.service.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service.prop")
public class ServiceProperties {

	private long basePrice;
	private String basePriceUnit;
	private String baseType;
	private int baseFixRateDelay;

	public long getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(long basePrice) {
		this.basePrice = basePrice;
	}

	public String getBasePriceUnit() {
		return basePriceUnit;
	}

	public void setBasePriceUnit(String basePriceUnit) {
		this.basePriceUnit = basePriceUnit;
	}

	public String getBaseType() {
		return baseType;
	}

	public void setBaseType(String baseType) {
		this.baseType = baseType;
	}

	public int getBaseFixRateDelay() {
		return baseFixRateDelay;
	}

	public void setBaseFixRateDelay(int baseFixRateDelay) {
		this.baseFixRateDelay = baseFixRateDelay;
	}

	@Override
	public String toString() {
		return "ServiceProperties [basePrice=" + basePrice + ", basePriceUnit=" + basePriceUnit + "]";
	}

}
