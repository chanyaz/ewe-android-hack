package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Itinerary implements JSONable {

	private String mItineraryNumber;
	private String mTravelRecordLocator;
	private String mTripId;

	// List of associated product keys (or rate keys, as they are known in hotels)
	//
	// Not used yet, but it will be when we need to recover data about itineraries 
	private List<String> mProductKeys = new ArrayList<String>();

	public String getItineraryNumber() {
		return mItineraryNumber;
	}

	public void setItineraryNumber(String itineraryNumber) {
		mItineraryNumber = itineraryNumber;
	}

	public String getTravelRecordLocator() {
		return mTravelRecordLocator;
	}

	public void setTravelRecordLocator(String travelRecordLocator) {
		mTravelRecordLocator = travelRecordLocator;
	}

	public String getTripId() {
		return mTripId;
	}

	public void setTripId(String tripId) {
		mTripId = tripId;
	}

	public void addProductKey(String productKey) {
		mProductKeys.add(productKey);
	}

	public List<String> getProductKeys() {
		return mProductKeys;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("itineraryNumber", mItineraryNumber);
			obj.putOpt("travelRecordLocator", mTravelRecordLocator);
			obj.putOpt("tripId", mTripId);
			JSONUtils.putStringList(obj, "productKeys", mProductKeys);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert FlightItinerary object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mItineraryNumber = obj.optString("itineraryNumber");
		mTravelRecordLocator = obj.optString("travelRecordLocator");
		mTripId = obj.optString("tripId");
		mProductKeys = JSONUtils.getStringList(obj, "productKeys");
		return true;
	}
}
