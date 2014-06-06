package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

/**
 * @author doug
 */
public class TripBucketItemFlight extends TripBucketItem {

	FlightSearchParams mFlightSearchParams;
	FlightTrip mFlightTrip;

	public TripBucketItemFlight() {

	}

	public TripBucketItemFlight(FlightSearchParams params, FlightTrip flightTrip) {
		mFlightSearchParams = params.clone();
		mFlightTrip = flightTrip.clone();
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.FLIGHTS;
	}

	public FlightSearchParams getFlightSearchParams() {
		return mFlightSearchParams;
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
			JSONUtils.putJSONable(obj, "flightSearchParams", mFlightSearchParams);
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
		mFlightSearchParams = JSONUtils.getJSONable(obj, "flightSearchParams", FlightSearchParams.class);
		mFlightTrip = JSONUtils.getJSONable(obj, "flightTrip", FlightTrip.class);
		return true;
	}
}
