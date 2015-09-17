package com.expedia.bookings.data.cars;

public class CarApiException extends RuntimeException {
	private CarApiError apiError;

	public CarApiException(CarApiError error) {
		super(error.errorCode.name());
		apiError = error;
	}

	public CarApiError getApiError() {
		return apiError;
	}
}

