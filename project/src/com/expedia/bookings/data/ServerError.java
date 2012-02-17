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

public class ServerError implements JSONable {

	public static final String FLAG_ITINERARY_BOOKED = "itineraryBooked";

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

	// This is for replacing (bad) EAN error messages with better ones
	@SuppressWarnings("serial")
	public static final HashMap<String, Integer> ERRORS = new HashMap<String, Integer>() {
		{
			put("Result was null", R.string.ean_error_no_results);
			put("Results NULL", R.string.ean_error_no_results);
			put("No Results Available", R.string.ean_error_no_results);
			put("Direct connect property unavailable.", R.string.ean_error_connect_unavailable);
		}
	};

	public static enum ErrorCode {
		SIMULATED, HOTEL_SERVICE_FATAL_FAILURE, UNKNOWN_ERROR, INVALID_INPUT_UNKNOWN, INVALID_INPUT, HOTEL_ROOM_UNAVAILABLE, HOTEL_OFFER_UNAVAILABLE, USER_SERVICE_FATAL_FAILURE, BOOKING_FAILED, PAYMENT_FAILED, BOOKING_SUCCEEDED_WITH_ERRORS
	}

	private static String LODGING_SERVICE_REQUEST_VALIDATION_EXCEPTION = "LODGING_SERVICE_REQUEST_VALIDATION_EXCEPTION";

	// Use the presentation message, except in special circumstances of lacking data
	// or when the data needs some gentle massaging
	public String getPresentableMessage(Context context) {
		String message = mPresentationMessage;
		if (TextUtils.isEmpty(message)) {
			message = mVerboseMessage;
			if (TextUtils.isEmpty(message)) {
				message = mMessage;
			}
		}

		if (TextUtils.isEmpty(message)) {
			return null;
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

		if (mDiagnosticFullText != null) {
			if (mDiagnosticFullText.contains("Phone number!")) {
				message = context.getString(R.string.ean_error_invalid_phone_number);
			}
		}

		// Handle special cases
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
