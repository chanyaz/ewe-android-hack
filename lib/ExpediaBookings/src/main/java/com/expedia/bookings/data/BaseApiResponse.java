package com.expedia.bookings.data;

import java.util.List;

import com.google.gson.Gson;

public class BaseApiResponse {
	public final String activityId;
	public List<ApiError> errors;

	public boolean hasErrors() {
		return errors != null && errors.size() > 0;
	}

	public ApiError getFirstError() {
		if (!hasErrors()) {
			throw new RuntimeException("No errors to get!");
		}
		return errors.get(0);
	}

	public boolean hasPriceChange() {
		return hasErrors() && getFirstError().errorCode == ApiError.Code.PRICE_CHANGE;
	}

	public String errorsToString() {
		return new Gson().toJson(errors);
	}
}
