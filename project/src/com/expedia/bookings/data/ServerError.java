package com.expedia.bookings.data;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

@SuppressWarnings("serial")
public class ServerError implements JSONable {
	public static enum ApiMethod {
		CHECKOUT, SEARCH_RESULTS, HOTEL_OFFERS, HOTEL_PRODUCT, HOTEL_INFORMATION, BAZAAR_REVIEWS, SIGN_IN, CREATE_TRIP,
		FLIGHT_SEARCH,
		FLIGHT_DETAILS,
		FLIGHT_CHECKOUT,
	}

	public static enum ErrorCode {
		BOOKING_FAILED, BOOKING_SUCCEEDED_WITH_ERRORS, HOTEL_OFFER_UNAVAILABLE, HOTEL_SERVICE_FATAL_FAILURE, HOTEL_ROOM_UNAVAILABLE, INVALID_INPUT, INVALID_INPUT_UNKNOWN, PAYMENT_FAILED, SIMULATED, // Not returned by e3, our own error
		UNKNOWN_ERROR, // Catch-all error code
		USER_SERVICE_FATAL_FAILURE, INVALID_INPUT_COUPON_CODE, APPLY_COUPON_ERROR
	}

	public static final String FLAG_ITINERARY_BOOKED = "itineraryBooked";

	private static final String LODGING_SERVICE_REQUEST_VALIDATION_EXCEPTION = "LODGING_SERVICE_REQUEST_VALIDATION_EXCEPTION";

	// This is for replacing (bad) EAN error messages with better ones
	public static final HashMap<String, Integer> ERRORS = new HashMap<String, Integer>() {
		{
			put("Result was null", R.string.ean_error_no_results);
			put("Results NULL", R.string.ean_error_no_results);
			put("No Results Available", R.string.ean_error_no_results);
			put("Direct connect property unavailable.", R.string.ean_error_connect_unavailable);
		}
	};

	public static final HashMap<ErrorCode, Integer> ERROR_MAP_CHECKOUT = new HashMap<ErrorCode, Integer>() {
		{
			put(ErrorCode.BOOKING_FAILED, R.string.e3_error_checkout_booking_failed);
			put(ErrorCode.BOOKING_SUCCEEDED_WITH_ERRORS, R.string.e3_error_checkout_booking_succeeded_with_errors);
			put(ErrorCode.HOTEL_ROOM_UNAVAILABLE, R.string.e3_error_checkout_hotel_room_unavailable);
			put(ErrorCode.INVALID_INPUT, R.string.e3_error_checkout_invalid_input);
			put(ErrorCode.PAYMENT_FAILED, R.string.e3_error_checkout_payment_failed);
		}
	};

	public static final HashMap<ErrorCode, Integer> ERROR_MAP_SEARCH_RESULTS = new HashMap<ErrorCode, Integer>() {
		{
			put(ErrorCode.HOTEL_SERVICE_FATAL_FAILURE, R.string.e3_error_search_results_hotel_service_failure);
			put(ErrorCode.INVALID_INPUT, R.string.e3_error_search_results_invalid_input);
			put(ErrorCode.INVALID_INPUT_UNKNOWN, R.string.e3_error_search_results_invalid_input_unknown);
			put(ErrorCode.UNKNOWN_ERROR, R.string.e3_error_search_results_unknown_error);
		}
	};

	public static final HashMap<ErrorCode, Integer> ERROR_MAP_HOTEL_OFFERS = new HashMap<ErrorCode, Integer>() {
		{
			put(ErrorCode.HOTEL_OFFER_UNAVAILABLE, R.string.e3_error_hotel_offers_hotel_offer_unavailable);
			put(ErrorCode.HOTEL_ROOM_UNAVAILABLE, R.string.e3_error_hotel_offers_hotel_room_unavailable);
			put(ErrorCode.HOTEL_SERVICE_FATAL_FAILURE, R.string.e3_error_hotel_offers_hotel_service_failure);
			put(ErrorCode.INVALID_INPUT, R.string.e3_error_hotel_offers_invalid_input);
			put(ErrorCode.UNKNOWN_ERROR, R.string.e3_error_hotel_offers_unknown_error);
		}
	};

	public static final HashMap<ErrorCode, Integer> ERROR_MAP_HOTEL_PRODUCT = new HashMap<ErrorCode, Integer>() {
		{
			put(ErrorCode.INVALID_INPUT, R.string.e3_error_hotel_product_invalid_input);
			put(ErrorCode.UNKNOWN_ERROR, R.string.e3_error_hotel_product_unknown_error);
		}
	};

	public static final HashMap<ErrorCode, Integer> ERROR_MAP_HOTEL_INFORMATION = new HashMap<ErrorCode, Integer>() {
		{
			put(ErrorCode.HOTEL_SERVICE_FATAL_FAILURE, R.string.e3_error_hotel_information_hotel_service_failure);
			put(ErrorCode.INVALID_INPUT, R.string.e3_error_hotel_information_invalid_input);
			put(ErrorCode.UNKNOWN_ERROR, R.string.e3_error_hotel_information_unknown_error);
		}
	};

	private ApiMethod mApiMethod = null;
	private ErrorCode mErrorCode;
	private String mCode;
	private String mMessage;

	private String mDiagnosticFullText;

	// Expedia-specific items
	private String mVerboseMessage;
	private String mPresentationMessage;

	// Expedia-specific error handling codes
	private String mCategory;
	private String mHandling;

	private Map<String, String> mExtras;

	public ServerError() {
	}

	public ServerError(ApiMethod apiMethod) {
		mApiMethod = apiMethod;
	}

	public ApiMethod getApiMethod() {
		return mApiMethod;
	}

	public void setApiMethod(ApiMethod apiMethod) {
		mApiMethod = apiMethod;
	}

	public String getCode() {
		return mCode;
	}

	public void setCode(String code) {
		this.mCode = code;

		try {
			this.mErrorCode = ErrorCode.valueOf(code);
		}
		catch (Exception e) {
			this.mErrorCode = ErrorCode.UNKNOWN_ERROR;
		}
	}

	public ErrorCode getErrorCode() {
		return mErrorCode;
	}

	/**
	 * We are defining "succeeded" in this case as itineraryBooked == true. There are other flags 
	 * we could use: "itenerarySaved", "emailSent". Also, this really only applies to 
	 * results from BookingResponse.
	 * @return
	 */
	public boolean succeededWithErrors() {
		return mErrorCode == ErrorCode.BOOKING_SUCCEEDED_WITH_ERRORS && getExtra(FLAG_ITINERARY_BOOKED) != null
				&& getExtra(FLAG_ITINERARY_BOOKED).equals("true");
	}

	public String getDiagnosticFullText() {
		return mDiagnosticFullText;
	}

	public void setDiagnosticFullText(String diagnosticFullText) {
		mDiagnosticFullText = diagnosticFullText;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		this.mMessage = message;
	}

	public String getVerboseMessage() {
		return mVerboseMessage;
	}

	public void setVerboseMessage(String verboseMessage) {
		this.mVerboseMessage = verboseMessage;
	}

	public String getPresentationMessage() {
		return mPresentationMessage;
	}

	public void setPresentationMessage(String presentationMessage) {
		this.mPresentationMessage = presentationMessage;
	}

	public String getCategory() {
		return mCategory;
	}

	public void setCategory(String category) {
		this.mCategory = category;
	}

	public String getHandling() {
		return mHandling;
	}

	public void setHandling(String handling) {
		this.mHandling = handling;
	}

	public void addExtra(String key, String value) {
		if (mExtras == null) {
			mExtras = new HashMap<String, String>();
		}
		if (value != null) {
			mExtras.put(key, value);
		}
	}

	public String getExtra(String key) {
		if (mExtras == null) {
			return null;
		}
		return mExtras.get(key);
	}

	// Use the presentation message, except in special circumstances of lacking data
	// or when the data needs some gentle massaging
	public String getPresentableMessage(Context context) {
		String message = mPresentationMessage;

		if (mApiMethod != null && mErrorCode != null) {
			switch (mApiMethod) {
			case CHECKOUT: {
				if (ERROR_MAP_CHECKOUT.containsKey(mErrorCode)) {
					message = context.getString(ERROR_MAP_CHECKOUT.get(mErrorCode));
				}
				break;
			}
			case SEARCH_RESULTS: {
				if (ERROR_MAP_SEARCH_RESULTS.containsKey(mErrorCode)) {
					message = context.getString(ERROR_MAP_SEARCH_RESULTS.get(mErrorCode));
				}
				break;
			}
			case HOTEL_OFFERS: {
				if (ERROR_MAP_HOTEL_OFFERS.containsKey(mErrorCode)) {
					message = context.getString(ERROR_MAP_HOTEL_OFFERS.get(mErrorCode));
				}
				break;
			}
			case HOTEL_PRODUCT: {
				if (ERROR_MAP_HOTEL_PRODUCT.containsKey(mErrorCode)) {
					message = context.getString(ERROR_MAP_HOTEL_PRODUCT.get(mErrorCode));
				}
				break;
			}
			case HOTEL_INFORMATION: {
				if (ERROR_MAP_HOTEL_INFORMATION.containsKey(mErrorCode)) {
					message = context.getString(ERROR_MAP_HOTEL_INFORMATION.get(mErrorCode));
				}
				break;
			}
			}
		}

		if (TextUtils.isEmpty(message)) {
			message = mVerboseMessage;
			if (TextUtils.isEmpty(message)) {
				message = mMessage;
			}
		}

		if (mExtras != null && mExtras.containsKey("emailSent") && mExtras.get("emailSent").equals("unknown")) {
			// This is a special case for E3
			message = context.getString(R.string.error_unable_to_send_email);
		}
		else if (message.equals("TravelNow.com cannot service this request.") && mVerboseMessage != null) {
			message = mVerboseMessage.replace("Data in this request could not be validated: ", "");
		}
		else if (ERRORS.containsKey(message)) {
			message = context.getString(ERRORS.get(message));
		}

		// Handle special cases
		if (mDiagnosticFullText != null) {
			if (mDiagnosticFullText.contains("Phone number!")) {
				message = context.getString(R.string.ean_error_invalid_phone_number);
			}
			if (mDiagnosticFullText.contains("INVALID_EXPIRATIONDATE")) {
				message = context.getString(R.string.e3_error_checkout_invalid_expiration);
			}
		}

		switch (mErrorCode) {
		case INVALID_INPUT: {
			if (mExtras != null && mExtras.containsKey("field")) {
				String field = mExtras.get("field");
				if (field.equals(LODGING_SERVICE_REQUEST_VALIDATION_EXCEPTION)) {
					message = context.getString(R.string.ean_error_no_results);
				}
			}
			break;
		}
		}

		// If message is empty, set it to null
		if (TextUtils.isEmpty(message)) {
			message = null;
		}

		return message;
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("code", mCode);
			obj.putOpt("message", mMessage);
			obj.putOpt("diagnosticFullText", mDiagnosticFullText);
			obj.putOpt("verboseMessage", mVerboseMessage);
			obj.putOpt("presentationMessage", mPresentationMessage);
			obj.putOpt("category", mCategory);
			obj.putOpt("handling", mHandling);
			JSONUtils.putStringMap(obj, "extras", mExtras);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert ServerError to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		setCode(obj.optString("code")); // handles mCode, mErrorCode
		mMessage = obj.optString("message");
		mDiagnosticFullText = obj.optString("diagnosticFullText");
		mVerboseMessage = obj.optString("verboseMessage");
		mPresentationMessage = obj.optString("presentationMessage");
		mCategory = obj.optString("category");
		mHandling = obj.optString("handling");
		mExtras = JSONUtils.getStringMap(obj, "extras");
		return true;
	}
}
