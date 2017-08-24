package com.expedia.bookings.data.insurance;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class InsuranceTripParams {
	private final String insuranceProductId;
	private final String tripId;

	public InsuranceTripParams(@NotNull String tripId) {
		this(tripId, null);
	}

	public InsuranceTripParams(@NotNull String tripId, String insuranceProductId) {
		this.tripId = tripId;
		this.insuranceProductId = insuranceProductId;
	}

	@NotNull
	public Map<String, Object> toQueryMap() {
		Map<String, Object> params = new HashMap<>();
		if (insuranceProductId != null) { // may be blank when removing insurance
			params.put("insuranceProductId", insuranceProductId);
		}
		params.put("tripId", tripId);
		return params;
	}
}
