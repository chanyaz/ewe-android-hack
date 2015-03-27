package com.expedia.bookings.data.cars;

public class CarApiError {

	public enum Code {
		// Common errors
		UNKNOWN_ERROR, // retry
		INVALID_INPUT,
		PRICE_CHANGE,

		// Airport search errors
		CAR_PRODUCT_NOT_AVAILABLE, // No cars available at this location
		CAR_SERVICE_ERROR,
		CAR_SEARCH_ERROR,
		CAR_SEARCH_WINDOW_VIOLATION,

		// Create trip errors
		INVALID_CAR_PRODUCT_KEY, // Most likely invalid dates
		TRIP_SERVICE_ERROR, // retry candidate

		// Checkout errors
		OMS_ERROR, // downstream payment service failed, behaves as UNKNOWN_ERROR
		PAYMENT_FAILED, // Tell user to double-check payment info; more granular if so desired?
		SESSION_TIMEOUT, // do a fresh create trip ... time passed or cookies got messed up
		MOBILE_USER_CREATION_FAILED_DURING_CHECKOUT, // account already created
		TRIP_ALREADY_BOOKED, // a similar reservation exists on our backend
		;
	}

	public enum DetailCode {
		//CAR SEARCH WINDOW VIOLATION detail errors
		DROP_OFF_DATE_TOO_LATE,
		SEARCH_DURATION_TOO_SMALL,
		PICKUP_DATE_TOO_EARLY,
		SEARCH_DURATION_TOO_LARGE,
		PICKUP_DATE_IN_THE_PAST,
		PICKUP_DATE_AND_DROP_OFF_DATE_ARE_THE_SAME,
	}

	public static class ErrorInfo {
		public String summary;
		public String field;
		public String cause;
	}

	public Code errorCode;
	public DetailCode errorDetailCode;
	public int diagnosticId;
	public String diagnosticFullText;
	public ErrorInfo errorInfo;
}
