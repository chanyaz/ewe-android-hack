package com.expedia.bookings.data.flights;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FlightCreateTripParams {

	private String productKey;

	public FlightCreateTripParams(String productKey) {
		this.productKey = productKey;
	}

	@NotNull
	public Map<String, Object> toQueryMap() {
		Map<String, Object> params = new HashMap<>();
		params.put("productKey", productKey);
		return params;
	}
}
