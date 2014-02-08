package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

/**
 * @author doug
 */
public class TripBucketItemFlight extends TripBucketItem {

	FlightSearch mFlightSearch;
	FlightTrip mFlightTrip;

	public TripBucketItemFlight() {

	}

	public TripBucketItemFlight(FlightSearch flight, FlightTrip trip) {
		mFlightSearch = flight;
		mFlightTrip = trip;
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.FLIGHTS;
	}

	public FlightSearch getFlightSearch() {
		return mFlightSearch;
	}

	public FlightTrip getFlightTrip() {
		return mFlightTrip;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = super.toJson();
			JSONUtils.putJSONable(obj, "flightSearch", mFlightSearch);
			JSONUtils.putJSONable(obj, "flightTrip", mFlightTrip);
			obj.putOpt("type", "flight");
			return obj;
		}
		catch (JSONException e) {
			Log.e("TripBucketItemFlight toJson() failed", e);
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mFlightSearch = JSONUtils.getJSONable(obj, "flightSearch", FlightSearch.class);
		mFlightTrip = JSONUtils.getJSONable(obj, "flightTrip", FlightTrip.class);
		return true;
	}
}
