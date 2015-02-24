package com.expedia.bookings.data.cars;

import com.expedia.bookings.utils.Strings;

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


		public String toDebugString() {
			StringBuilder builder = new StringBuilder();
			builder
				.append("summary=")
				.append(summary);
			if (!Strings.isEmpty(field)) {
				builder
					.append("\n")
					.append("field=")
					.append(field);
			}
			if (!Strings.isEmpty(cause)) {
				builder
					.append("\n")
					.append("cause=")
					.append(cause);
			}

			return builder.toString();
		}

	}

	public Code errorCode;
	public int diagnosticId;
	public String diagnosticFullText;
	public ErrorInfo errorInfo;


	public String toDebugString() {
		StringBuilder builder = new StringBuilder();
		builder
			.append("errorCode=")
			.append(errorCode)
			.append("\n");
		if (errorInfo != null) {
			builder
				.append("errorInfo=")
				.append(errorInfo.toDebugString());
		}
		builder
			.append("diagnosticId=")
			.append(diagnosticId)
			.append("\n")
			.append("diagnosticFullText=")
			.append(diagnosticFullText);

		return builder.toString();
	}


}
