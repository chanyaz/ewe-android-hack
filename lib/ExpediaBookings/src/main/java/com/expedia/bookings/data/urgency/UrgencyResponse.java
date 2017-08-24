package com.expedia.bookings.data.urgency;

import java.util.List;

public class UrgencyResponse {
	final int responseStatusCode;
	final List<RegionTicker> regionTicker;

	public boolean hasError() {
		return responseStatusCode != 0;
	}

	public RegionTicker getFirstRegionTicker() {
		return regionTicker.get(0);
	}
}
