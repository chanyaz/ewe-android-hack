package com.expedia.bookings.data.flights;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class FlightCreateTripParams {
	private String productKey;
	private Boolean withInsurance;

	public FlightCreateTripParams(String productKey, Boolean withInsurance) {
		this.productKey = productKey;
		this.withInsurance = withInsurance;
	}

	@NotNull
	public Map<String, Object> toQueryMap() {
		Map<String, Object> params = new HashMap<>();
		params.put("productKey", productKey);
		params.put("withInsurance", withInsurance);
		return params;
	}
}
