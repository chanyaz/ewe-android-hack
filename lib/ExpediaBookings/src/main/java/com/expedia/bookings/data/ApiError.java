package com.expedia.bookings.data;

import org.jetbrains.annotations.Nullable;
import com.expedia.bookings.utils.Strings;

public class ApiError extends RuntimeException {

	public enum Code {
		// Common errors
		UNMAPPED_ERROR, // error not in this list
		UNKNOWN_ERROR, // retry
		INVALID_INPUT,
		PRICE_CHANGE,

		//Suggestion Service Errors
		CURRENT_LOCATION_ERROR,
		SUGGESTIONS_NO_RESULTS,

		// Airport search errors
		CAR_PRODUCT_NOT_AVAILABLE, // No cars available at this location
		CAR_SERVICE_ERROR,
		CAR_SEARCH_ERROR,
		CAR_SEARCH_WINDOW_VIOLATION,
		CAR_FILTER_NO_RESULTS,

		// Create trip errors
		// flights
		FLIGHT_PRODUCT_NOT_FOUND,
		FLIGHT_SOLD_OUT,
		// cars
		INVALID_CAR_PRODUCT_KEY, // Most likely invalid dates
		TRIP_SERVICE_ERROR, // retry candidate

		// Checkout errors
		OMS_ERROR, // downstream payment service failed, behaves as UNKNOWN_ERROR
		PAYMENT_FAILED, // Tell user to double-check payment info; more granular if so desired?
		SESSION_TIMEOUT, // do a fresh create trip ... time passed or cookies got messed up
		MOBILE_USER_CREATION_FAILED_DURING_CHECKOUT, // account already created
		TRIP_ALREADY_BOOKED, // a similar reservation exists on our backend

		// LX Api Errors
		LX_SEARCH_NO_RESULTS,
		LX_DETAILS_FETCH_ERROR,
		LX_PRODUCT_LOOKUP_ERROR,
		LIMITED_INVENTORY,

		//Hotel Coupon Errors
		APPLY_COUPON_ERROR,
		REMOVE_COUPON_ERROR,

		//Hotel Search Errors
		HOTEL_SEARCH_NO_RESULTS,
		HOTEL_MAP_SEARCH_NO_RESULTS,
		HOTEL_FILTER_NO_RESULTS,
		HOTEL_PINNED_NOT_FOUND,
		REGION_BLOCKED,

		//Hotel Checkout Errors
		HOTEL_CHECKOUT_CARD_DETAILS,
		HOTEL_CHECKOUT_TRAVELLER_DETAILS,
		HOTEL_CHECKOUT_UNKNOWN,

		HOTEL_PRODUCT_KEY_EXPIRY,
		HOTEL_ROOM_UNAVAILABLE,

		//Calculate Points Errors
		POINTS_CONVERSION_UNAUTHENTICATED_ACCESS,

		//Package Create Trip
		PACKAGE_DATE_MISMATCH_ERROR,

		//Package Checkout Errors
		PACKAGE_SEARCH_ERROR,
		PACKAGE_CHECKOUT_CARD_DETAILS,
		PACKAGE_CHECKOUT_TRAVELLER_DETAILS,
		PACKAGE_CHECKOUT_UNKNOWN,
		INVALID_CARD_NUMBER,
		CID_DID_NOT_MATCHED,
		INVALID_CARD_EXPIRATION_DATE,
		CARD_LIMIT_EXCEEDED,

		//Flight Search Errors
		FLIGHT_SEARCH_ERROR,
		FLIGHT_SEARCH_NO_RESULTS,

		//Rail errors
		RAIL_PRODUCT_LOOKUP_ERROR,
		// This is not returned by the rails domain API but set manually.
		RAIL_SEARCH_NO_RESULTS,
		// Any other error other than INVALID_INPUT is handled as unknown.
		RAIL_UNKNOWN_CKO_ERROR,

		//MID errors
		MID_COULD_NOT_FIND_RESULTS
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
		public String source;
		public String sourceErrorId;
		public String field;
		public String cause;
		public String couponErrorType;

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ErrorInfo) {
				ErrorInfo other = ((ErrorInfo) obj);
				if (other.field != null && field != null && !Strings.equals(other.field, field)) {
					return false;
				}
				return Strings.equals(other.couponErrorType, couponErrorType);
			}
			return false;
		}
	}

	//TODO Rename this to indicate it is error key from API.
	private String errorCode = "";
	public DetailCode errorDetailCode;
	public int diagnosticId;
	public ErrorInfo errorInfo;
	public String regionId;
	public String message;

	public ApiError(Code code) {
		super(code.name());
		errorCode = code.name();
	}

	public ApiError() {
		// ignore
	}

	public String getApiErrorMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Nullable
	public Code getErrorCode() {
		try {
			return Code.valueOf(errorCode);
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}

	public String getErrorKey() {
		return errorCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ApiError) {
			ApiError other = ((ApiError) obj);
			if (other.errorCode != errorCode) {
				return false;
			}
			if (other.errorInfo == null || errorInfo == null) {
				return true;
			}
			else {
				return other.errorInfo.equals(errorInfo);
			}
		}
		return false;
	}
}
