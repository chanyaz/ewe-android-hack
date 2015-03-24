package com.expedia.bookings.data.cars;

public class CarApiException extends RuntimeException {
	private CarApiError apiError;

	public CarApiException(CarApiError error) {
		super(error.errorDetailCode.name());
		apiError = error;
	}

	public CarApiError getApiError() {
		return apiError;
	}
}

