package com.expedia.bookings.data.cars;

public class CarApiError {

	public enum Code {
		// Common errors
		UNKNOWN_ERROR, // retry
		INVALID_INPUT,
		PRICE_CHANGE,

		// Airport search errors
		CARPRODUCTNOT_AVAILABLE, // No cars available at this location
		CARSERVICEERROR,
		CARSEARCHERROR,

		// Create trip errors
		INVALID_CAR_PRODUCT_KEY, // Most likely invalid dates
		TRIP_SERVICE_ERROR, // retry candidate

		// Checkout errors
		PAYMENT_FAILED, // Tell user to double-check payment info; more granular if so desired?
		SESSION_TIMEOUT, // do a fresh create trip ... time passed or cookies got messed up
		MOBILE_USER_CREATION_FAILED_DURING_CHECKOUT // account already created
		;
	}

	public static class ErrorInfo {
		public String summary;
		public String field;
		public String cause;
	}

	public Code errorCode;
	public int diagnosticId;
	public String diagnosticFullText;
	public ErrorInfo errorInfo;
}
