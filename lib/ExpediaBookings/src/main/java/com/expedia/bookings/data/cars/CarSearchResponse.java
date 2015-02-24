package com.expedia.bookings.data.cars;

import java.util.List;

import org.joda.time.DateTime;

public class CarSearchResponse {
	public DateTime pickupTime;
	public DateTime dropOffTime;
	public List<SearchCarOffer> offers;

	public List<CarApiError> errors;

	public boolean hasErrors() {
		return errors != null && errors.size() > 0;
	}

	public String printErrors() {
		StringBuilder builder = new StringBuilder();
		for (CarApiError error : errors) {
			builder.append(error.toDebugString());
			builder.append("\n");
		}
		return builder.toString();
	}
}
