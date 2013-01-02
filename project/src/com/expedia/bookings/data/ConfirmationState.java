package com.expedia.bookings.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * Saves a confirmation state, which allows us to reproduce a confirmation activity.
 */
public class ConfirmationState {

	public enum Type {
		HOTEL("confirmation.dat"),
		FLIGHT("flight-confirmation.dat");

		private String mFilename;

		private Type(String filename) {
			mFilename = filename;
		}
	}

	public static final String PREF_HAS_SHARED_VIA_EMAIL = "PREF_HAS_SHARED_VIA_EMAIL";
	public static final String PREF_HAS_TRACKED_WITH_FLIGHTTRACK = "PREF_HAS_TRACKED_WITH_FLIGHTTRACK";
	public static final String PREF_HAS_ADDED_TO_CALENDAR = "PREF_HAS_ADDED_TO_CALENDAR";
	public static final String PREF_HAS_ADDED_INSURANCE = "PREF_HAS_ADDED_INSURANCE";

	private static final String FIELD_VERSION = "ConfirmationState.Version";

	private Context mContext;
	private Type mType;

	private Traveler mPrimaryTraveler;

	public ConfirmationState(Context context, Type type) {
		mContext = context;
		mType = type;
	}

	public boolean save(SearchParams searchParams, Property property, Rate rate, BillingInfo billingInfo,
			BookingResponse bookingResponse, Rate discountRate, Traveler primaryTraveler) {
		if (mType != Type.HOTEL) {
			throw new RuntimeException("Tried to save " + Type.HOTEL + " data into " + mType + " state");
		}

		Log.i("Saving confirmation data of type " + mType);

		try {
			JSONObject data = new JSONObject();
			JSONUtils.putJSONable(data, Codes.SEARCH_PARAMS, searchParams);
			JSONUtils.putJSONable(data, Codes.PROPERTY, property);
			JSONUtils.putJSONable(data, Codes.RATE, rate);
			JSONUtils.putJSONable(data, Codes.BILLING_INFO, billingInfo);
			JSONUtils.putJSONable(data, Codes.BOOKING_RESPONSE, bookingResponse);
			if (discountRate != null) {
				JSONUtils.putJSONable(data, Codes.DISCOUNT_RATE, discountRate);
			}

			if (primaryTraveler != null) {
				mPrimaryTraveler = primaryTraveler;
				JSONUtils.putJSONable(data, "primaryTraveler", primaryTraveler);
			}

			writeData(data);

			return true;
		}
		catch (Exception e) {
			Log.e("Could not save confirmation data state of type " + mType, e);
			return false;
		}
	}

	public boolean save(FlightSearch flightSearch, Itinerary itinerary, BillingInfo billingInfo,
			List<Traveler> travelers, FlightCheckoutResponse checkoutResponse) {
		if (mType != Type.FLIGHT) {
			throw new RuntimeException("Tried to save " + Type.FLIGHT + " data into " + mType + " state");
		}

		Log.i("Saving confirmation data of type " + mType);

		try {
			JSONObject data = new JSONObject();
			JSONUtils.putJSONable(data, "flightSearch", flightSearch);
			JSONUtils.putJSONable(data, "itinerary", itinerary);
			JSONUtils.putJSONable(data, "billingInfo", billingInfo);
			JSONUtils.putJSONableList(data, "travelers", travelers);
			JSONUtils.putJSONable(data, "checkoutResponse", checkoutResponse);

			writeData(data);

			return true;
		}
		catch (Exception e) {
			Log.e("Could not save confirmation data state of type " + mType, e);
			return false;
		}
	}

	private void writeData(JSONObject data) throws JSONException, IOException {
		// Put version, just in case this might be useful
		data.put(FIELD_VERSION, Integer.toString(AndroidUtils.getAppCode(mContext)));

		IoUtils.writeStringToFile(mType.mFilename, data.toString(0), mContext);
	}

	public boolean load() {
		Log.i("Loading saved confirmation data of type " + mType);

		try {
			JSONObject data = new JSONObject(IoUtils.readStringFromFile(mType.mFilename, mContext));

			if (mType == Type.HOTEL) {
				Db.setSearchParams(JSONUtils.getJSONable(data, Codes.SEARCH_PARAMS, SearchParams.class));
				Db.setSelectedProperty(JSONUtils.getJSONable(data, Codes.PROPERTY, Property.class));
				Db.setSelectedRate(JSONUtils.getJSONable(data, Codes.RATE, Rate.class));
				Db.setBillingInfo(JSONUtils.getJSONable(data, Codes.BILLING_INFO, BillingInfo.class));
				Db.setBookingResponse(JSONUtils.getJSONable(data, Codes.BOOKING_RESPONSE, BookingResponse.class));
				if (data.has(Codes.DISCOUNT_RATE)) {
					Db.setCouponDiscountRate(JSONUtils.getJSONable(data, Codes.DISCOUNT_RATE, Rate.class));
				}

				mPrimaryTraveler = JSONUtils.getJSONable(data, "primaryTraveler", Traveler.class);
			}
			else if (mType == Type.FLIGHT) {
				Db.setFlightSearch(JSONUtils.getJSONable(data, "flightSearch", FlightSearch.class));
				Db.addItinerary(JSONUtils.getJSONable(data, "itinerary", Itinerary.class));
				Db.setBillingInfo(JSONUtils.getJSONable(data, "billingInfo", BillingInfo.class));
				Db.setTravelers(JSONUtils.getJSONableList(data, "travelers", Traveler.class));
				Db.setFlightCheckout(JSONUtils.getJSONable(data, "checkoutResponse", FlightCheckoutResponse.class));
			}

			return true;
		}
		catch (Exception e) {
			Log.e("Could not load confirmation data state of type " + mType, e);
			return false;
		}
	}

	public boolean delete() {
		Log.i("Deleting saved confirmation data of type " + mType);

		// Make sure to delete these preferences that display checkmarks in the UI
		if (mType == Type.FLIGHT) {
			deleteFlightActionCompletionPrefs(mContext);
		}

		File savedConfResults = mContext.getFileStreamPath(mType.mFilename);
		return savedConfResults.delete();
	}

	public boolean hasSavedData() {
		File savedConfResults = mContext.getFileStreamPath(mType.mFilename);
		return savedConfResults.exists();
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience methods

	public static boolean hasSavedData(Context context, Type type) {
		ConfirmationState state = new ConfirmationState(context, type);
		return state.hasSavedData();
	}

	public static void delete(Context context, Type type) {
		ConfirmationState confirmationState = new ConfirmationState(context, type);
		if (confirmationState.hasSavedData()) {
			confirmationState.delete();
		}
	}

	private static void deleteFlightActionCompletionPrefs(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(PREF_HAS_SHARED_VIA_EMAIL, false);
		editor.putBoolean(PREF_HAS_ADDED_TO_CALENDAR, false);
		editor.putBoolean(PREF_HAS_TRACKED_WITH_FLIGHTTRACK, false);
		editor.putBoolean(PREF_HAS_ADDED_INSURANCE, false);
		SettingUtils.commitOrApply(editor);
	}

	//////////////////////////////////////////////////////////////////////////
	// Getters setters

	public Traveler getPrimaryTraveler() {
		return mPrimaryTraveler;
	}
}
