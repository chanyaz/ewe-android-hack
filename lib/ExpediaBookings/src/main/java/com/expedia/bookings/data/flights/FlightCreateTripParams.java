package com.expedia.bookings.data.flights;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.utils.Strings;

public class FlightCreateTripParams {
	private String productKey;

	public FlightCreateTripParams(String productKey) {
		this.productKey = productKey;
	}

	@NotNull
	public Map<String, Object> toQueryMap() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("productKey", productKey);
		return params;
	}
}
