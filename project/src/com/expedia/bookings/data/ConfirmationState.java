package com.expedia.bookings.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;

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

	private static final String FIELD_VERSION = "ConfirmationState.Version";

	private Context mContext;
	private Type mType;

	public ConfirmationState(Context context, Type type) {
		mContext = context;
		mType = type;
	}

	public boolean save(SearchParams searchParams, Property property, Rate rate, BillingInfo billingInfo,
			BookingResponse bookingResponse, Rate discountRate) {
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
}
