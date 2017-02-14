package com.expedia.bookings.data.urgency;

import java.util.List;

public class UrgencyResponse {
	int responseStatusCode;
	List<RegionTicker> regionTicker;

	public boolean hasError() {
		return responseStatusCode != 0;
	}

	public RegionTicker getFirstRegionTicker() {
		return regionTicker.get(0);
	}
}
