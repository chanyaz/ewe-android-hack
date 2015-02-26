package com.expedia.bookings.data.cars;

import java.util.List;

import org.joda.time.DateTime;

import com.google.gson.Gson;

public class CarSearchResponse {
	public DateTime pickupTime;
	public DateTime dropOffTime;
	public List<SearchCarOffer> offers;

	public List<CarApiError> errors;

	public boolean hasErrors() {
		return errors != null && errors.size() > 0;
	}

	public String errorsToString() {
		return new Gson().toJson(errors);
	}
}
