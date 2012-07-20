package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

/**
 * Represents a particular FlightTrip/FlightLeg pairing.
 */
public class FlightTripLeg implements JSONable {

	private String mProductKey;
	private String mLegId;

	public FlightTripLeg() {
		// For JSONable
	}

	public FlightTripLeg(FlightTrip trip, FlightLeg leg) {
		mProductKey = trip.getProductKey();
		mLegId = leg.getLegId();
	}

	public FlightTrip getFlightTrip() {
		return Db.getFlightSearch().getFlightTrip(mProductKey);
	}

	public FlightLeg getFlightLeg() {
		return Db.getFlightSearch().getFlightLeg(mLegId);
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("productKey", mProductKey);
			obj.putOpt("legId", mLegId);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mProductKey = obj.optString("productKey", null);
		mLegId = obj.optString("legId", null);
		return true;
	}
}
