package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightStatsRatingResponse extends Response implements JSONable {

	private FlightStatsRating mFlightStatsRating;

	public FlightStatsRating getFlightStatsRating() {
		return mFlightStatsRating;
	}

	public void setFlightStatsRating(FlightStatsRating flightStatsRating) {
		mFlightStatsRating = flightStatsRating;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONable(obj, "rating", mFlightStatsRating);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert FlightStatsFlightResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mFlightStatsRating = JSONUtils.getJSONable(obj, "rating", FlightStatsRating.class);
		return true;
	}

}
