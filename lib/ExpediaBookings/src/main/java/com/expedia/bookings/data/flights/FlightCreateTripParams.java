package com.expedia.bookings.data.flights;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import com.expedia.bookings.utils.Constants;

public class FlightCreateTripParams {

	private final String productKey;
	public boolean flexEnabled;
	private String featureOverride;

	public FlightCreateTripParams(String productKey) {
		this.productKey = productKey;
	}

	@NotNull
	public Map<String, Object> toQueryMap() {
		Map<String, Object> params = new HashMap<>();
		params.put("productKey", productKey);
		return params;
	}

	public void setFeatureOverride() {
		featureOverride = Constants.FEATURE_SUBPUB;
	}

	public String getFeatureOverride() {
		return featureOverride;
	}
}
