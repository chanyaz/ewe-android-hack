package com.expedia.bookings.data.flights;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class FlightCreateTripParams {

	private String productKey;
	public boolean flexEnabled;
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
