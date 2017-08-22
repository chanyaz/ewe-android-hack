package com.expedia.bookings.data;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.squareup.phrase.Phrase;

@SuppressWarnings("serial")
public class ServerError implements JSONable {
	public enum ApiMethod {
		CHECKOUT,
		SEARCH_RESULTS,
		HOTEL_OFFERS,
		HOTEL_PRODUCT,
		HOTEL_INFORMATION,
		USER_REVIEWS,
		SIGN_IN,
		CREATE_TRIP,
		FLIGHT_ROUTES,
		FLIGHT_SEARCH,
		CREATE_FLIGHT_ITINERARY,
		ASSOCIATE_USER_TO_TRIP,
		FLIGHT_CHECKOUT,
		PROFILE,
		COMMIT_TRAVELER,
		BACKGROUND_IMAGE,
		TRIPS,
		TRIP_DETAILS,
	}

	public enum ErrorCode {
		BOOKING_FAILED,
		BOOKING_SUCCEEDED_WITH_ERRORS,
		HOTEL_OFFER_UNAVAILABLE,
		HOTEL_SERVICE_FATAL_FAILURE,
		HOTEL_ROOM_UNAVAILABLE,
		INVALID_INPUT,
		INVALID_INPUT_UNKNOWN,
		PAYMENT_FAILED,
		SIMULATED, // Not returned by e3, our own error
		UNKNOWN_ERROR, // Catch-all error code
		USER_SERVICE_FATAL_FAILURE,
		INVALID_INPUT_COUPON_CODE,
		APPLY_COUPON_ERROR,
		PRICE_CHANGE,
		TRIP_ALREADY_BOOKED,
		FLIGHT_SOLD_OUT,
		FLIGHT_PRODUCT_NOT_FOUND,
		SESSION_TIMEOUT,
		CANNOT_BOOK_WITH_MINOR,
		TRIP_SERVICE_ERROR,
		// Coupon Error Codes
		COUPON_UNRECOGNIZED,
		COUPON_EXPIRED,
		COUPON_NOT_REDEEMED,
		COUPON_DUPLICATE,
		COUPON_PRICE_CHANGE,
		COUPON_INVALID_REGION,
		COUPON_MIN_STAY_NOT_MET,
		COUPON_INVALID_TRAVEL_DATES,
		COUPON_MIN_PURCHASE_AMOUNT_NOT_MET,
		COUPON_INVALID_PRODUCT,
		COUPON_HOTEL_EXCLUDED,
		COUPON_SERVICE_DOWN,
		COUPON_FALLBACK,
		COUPON_INVALID_HOTEL,
		COUPON_INVALID_STAY_DATES,
		COUPON_EXCEEDED_EARN_LIMIT,
		COUPON_INVALID_AVERAGE_PRICE,
		COUPON_NOT_ACTIVE,
		COUPON_DOES_NOT_EXIST,
		COUPON_CAMPAIGN_NOT_CONFIGURED,
		COUPON_PACKAGE_MISSING,
		NOT_AUTHENTICATED
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

	private static final HashMap<ErrorCode, Integer> ERROR_MAP_CHECKOUT = new HashMap<ErrorCode, Integer>() {
		{
			put(ErrorCode.BOOKING_FAILED, R.string.e3_error_checkout_booking_failed);
			put(ErrorCode.BOOKING_SUCCEEDED_WITH_ERRORS, R.string.e3_error_checkout_booking_succeeded_with_errors_TEMPLATE);
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

	private static final HashMap<ErrorCode, Integer> ERROR_MAP_HOTEL_OFFERS = new HashMap<ErrorCode, Integer>() {
		{
			put(ErrorCode.HOTEL_OFFER_UNAVAILABLE, R.string.e3_error_hotel_offers_hotel_offer_unavailable);
			put(ErrorCode.HOTEL_ROOM_UNAVAILABLE, R.string.e3_error_hotel_offers_hotel_room_unavailable);
			put(ErrorCode.HOTEL_SERVICE_FATAL_FAILURE, R.string.e3_error_hotel_offers_hotel_service_failure_TEMPLATE);
			put(ErrorCode.INVALID_INPUT, R.string.e3_error_hotel_offers_invalid_input);
			put(ErrorCode.UNKNOWN_ERROR, R.string.e3_error_hotel_offers_unknown_error);
		}
	};

	private static final HashMap<ErrorCode, Integer> ERROR_MAP_HOTEL_PRODUCT = new HashMap<ErrorCode, Integer>() {
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

	private static final HashMap<String, ErrorCode> COUPON_ERROR_MAP_TYPE = new HashMap<String, ErrorCode>() {
		{
			put("Duplicate", ErrorCode.COUPON_DUPLICATE);
			put("Expired", ErrorCode.COUPON_EXPIRED);
			put("FallBack", ErrorCode.COUPON_FALLBACK);
			put("HotelExcluded", ErrorCode.COUPON_HOTEL_EXCLUDED);
			put("InvalidHotel", ErrorCode.COUPON_INVALID_HOTEL);
			put("InvalidProduct", ErrorCode.COUPON_INVALID_PRODUCT);
			put("InvalidRegion", ErrorCode.COUPON_INVALID_REGION);
			put("InvalidTravelDates", ErrorCode.COUPON_INVALID_TRAVEL_DATES);
			put("MinPurchaseAmountNotMet", ErrorCode.COUPON_MIN_PURCHASE_AMOUNT_NOT_MET);
			put("MinStayNotMet", ErrorCode.COUPON_MIN_STAY_NOT_MET);
			put("NotRedeemed", ErrorCode.COUPON_NOT_REDEEMED);
			put("PriceChange", ErrorCode.COUPON_PRICE_CHANGE);
			put("ServiceDown", ErrorCode.COUPON_SERVICE_DOWN);
			put("Unrecognized", ErrorCode.COUPON_UNRECOGNIZED);
			put("InvalidAveragePrice", ErrorCode.COUPON_INVALID_AVERAGE_PRICE);
			put("InvalidStayDates", ErrorCode.COUPON_INVALID_STAY_DATES);
			put("ExceededEarnLimit", ErrorCode.COUPON_EXCEEDED_EARN_LIMIT);
			put("NotActive", ErrorCode.COUPON_NOT_ACTIVE);
			put("DoesNotExist", ErrorCode.COUPON_DOES_NOT_EXIST);
			put("CampaignIsNotConfigured", ErrorCode.COUPON_CAMPAIGN_NOT_CONFIGURED);
			put("PackageProductMissing", ErrorCode.COUPON_PACKAGE_MISSING);
		}
	};

	public static final HashMap<ErrorCode, Integer> ERROR_MAP_COUPON_MESSAGES = new HashMap<ErrorCode, Integer>() {
		{
			put(ErrorCode.COUPON_DUPLICATE, R.string.coupon_error_duplicate);
			put(ErrorCode.COUPON_EXPIRED, R.string.coupon_error_expired);
			put(ErrorCode.COUPON_FALLBACK, R.string.coupon_error_fallback);
			put(ErrorCode.COUPON_HOTEL_EXCLUDED, R.string.coupon_error_hotel_excluded);
			put(ErrorCode.COUPON_INVALID_HOTEL, R.string.coupon_error_invalid_hotel);
			put(ErrorCode.COUPON_INVALID_PRODUCT, R.string.coupon_error_invalid_product);
			put(ErrorCode.COUPON_INVALID_REGION, R.string.coupon_error_invalid_region);
			put(ErrorCode.COUPON_INVALID_TRAVEL_DATES, R.string.coupon_error_invalid_travel_dates);
			put(ErrorCode.COUPON_MIN_PURCHASE_AMOUNT_NOT_MET, R.string.coupon_error_min_purchase_amount_not_met);
			put(ErrorCode.COUPON_MIN_STAY_NOT_MET, R.string.coupon_error_min_stay_not_met);
			put(ErrorCode.COUPON_NOT_REDEEMED, R.string.coupon_error_not_redeemed);
			put(ErrorCode.COUPON_PRICE_CHANGE, R.string.coupon_error_price_change);
			put(ErrorCode.COUPON_SERVICE_DOWN, R.string.coupon_error_service_down);
			put(ErrorCode.COUPON_UNRECOGNIZED, R.string.coupon_error_unrecognized);
			put(ErrorCode.COUPON_INVALID_AVERAGE_PRICE, R.string.coupon_error_invalid_average_price);
			put(ErrorCode.COUPON_INVALID_STAY_DATES, R.string.coupon_error_invalid_stay_dates);
			put(ErrorCode.COUPON_EXCEEDED_EARN_LIMIT, R.string.coupon_error_exceeded_earn_limit);
			put(ErrorCode.COUPON_NOT_ACTIVE, R.string.coupon_error_not_active);
			put(ErrorCode.COUPON_DOES_NOT_EXIST, R.string.coupon_error_unknown);
			put(ErrorCode.COUPON_CAMPAIGN_NOT_CONFIGURED, R.string.coupon_error_unknown);
			put(ErrorCode.COUPON_PACKAGE_MISSING, R.string.coupon_error_invalid_booking);
		}
	};

	private ApiMethod mApiMethod = null;
	private ErrorCode mErrorCode;
	private String mCode;
	private String mMessage;

	private String mDiagnosticFullText;

	// Expedia-specific items
	private String mPresentationMessage;

	// Expedia-specific error handling codes
	private String mCategory;
	private String mHandling;

	// Coupon specific error
	private String mCouponErrorType;
	private ErrorCode mCouponErrorCode;

	private Map<String, String> mExtras;

	public ServerError() {
	}

	public ServerError(ApiMethod apiMethod) {
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

	public String getCouponErrorType() {
		return mCouponErrorType;
	}

	public void setCouponErrorType(String couponErrorType) {
		this.mCouponErrorType = couponErrorType;
		if (mCouponErrorType != null && COUPON_ERROR_MAP_TYPE.containsKey(mCouponErrorType)) {
			mCouponErrorCode = COUPON_ERROR_MAP_TYPE.get(mCouponErrorType);
		}
	}

	/**
	 * We are defining "succeeded" in this case as itineraryBooked == true. There are other flags
	 * we could use: "itenerarySaved", "emailSent". Also, this really only applies to
	 * results from HotelBookingResponse.
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
					message = Phrase.from(context, ERROR_MAP_CHECKOUT.get(mErrorCode))
						.putOptional("brand", BuildConfig.brand).format().toString();
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
					message = Phrase.from(context, ERROR_MAP_HOTEL_OFFERS.get(mErrorCode)).putOptional("brand",
						BuildConfig.brand).format().toString();
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

		if (mExtras != null && mExtras.containsKey("emailSent") && mExtras.get("emailSent").equals("unknown")) {
			// This is a special case for E3
			message = context.getString(R.string.error_unable_to_send_email);
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

	public String getCouponErrorMessage(Context context) {
		// Let's make this the default message in case the errorCode sent is not recognized.
		String message = context.getString(R.string.coupon_error_fallback);
		if (mCouponErrorCode != null && ERROR_MAP_COUPON_MESSAGES.containsKey(mCouponErrorCode)) {
			message = context.getString(ERROR_MAP_COUPON_MESSAGES.get(mCouponErrorCode));
		}
		return message;
	}

	public boolean isProductKeyExpiration() {
		return mErrorCode == ServerError.ErrorCode.INVALID_INPUT && getExtra("field").equals("productKey");
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("code", mCode);
			obj.putOpt("message", mMessage);
			obj.putOpt("diagnosticFullText", mDiagnosticFullText);
			obj.putOpt("presentationMessage", mPresentationMessage);
			obj.putOpt("category", mCategory);
			obj.putOpt("handling", mHandling);
			obj.putOpt("couponErrorType", mCouponErrorType);
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
		mPresentationMessage = obj.optString("presentationMessage");
		mCategory = obj.optString("category");
		mHandling = obj.optString("handling");
		mExtras = JSONUtils.getStringMap(obj, "extras");
		mCouponErrorType = obj.optString("couponErrorType");
		if (mCouponErrorType != null && COUPON_ERROR_MAP_TYPE.containsKey(mCouponErrorType)) {
			mCouponErrorCode = COUPON_ERROR_MAP_TYPE.get(mCouponErrorType);
		}
		return true;
	}
}
