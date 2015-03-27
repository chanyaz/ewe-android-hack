package com.expedia.bookings.data.cars;

import java.util.List;

import com.google.gson.Gson;

// TODO make even more generic, template based upon error type (e.g. CarApiError, LxApiError).
// TODO or make CarApiError a generic error
public class BaseCarResponse {
	public String activityId;
	public List<CarApiError> errors;

	public boolean hasErrors() {
		return errors != null && errors.size() > 0;
	}

	public CarApiError getFirstError() {
		if (!hasErrors()) {
			throw new RuntimeException("No errors to get!");
		}
		return errors.get(0);
	}

	public boolean hasPriceChange() {
		return hasErrors() && getFirstError().errorCode == CarApiError.Code.PRICE_CHANGE;
	}

	public String errorsToString() {
		return new Gson().toJson(errors);
	}
}
