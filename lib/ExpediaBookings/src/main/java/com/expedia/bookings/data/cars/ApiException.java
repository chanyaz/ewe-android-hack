package com.expedia.bookings.data.cars;

public class ApiException extends RuntimeException {
	public ApiError apiError;

	public ApiException(ApiError error) {
		super(error.errorCode.name());
		apiError = error;
	}
}

