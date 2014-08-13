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

	public TripBucketItemFlight() {

	}

	public TripBucketItemFlight(FlightSearch flightSearch) {
		mFlightSearch = flightSearch.generateForTripBucket();
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.FLIGHTS;
	}

	public FlightSearchParams getFlightSearchParams() {
		return mFlightSearch.getSearchParams();
	}

	public FlightTrip getFlightTrip() {
		return mFlightSearch.getSelectedFlightTrip();
	}


	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = super.toJson();
			JSONUtils.putJSONable(obj, "flightSearch", mFlightSearch);
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
		return true;
	}
}
