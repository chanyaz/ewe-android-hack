package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

/**
 * @author doug
 */
public class TripBucketItemFlight extends TripBucketItem {

	FlightSearchState mFlightSearchState;

	public TripBucketItemFlight() {

	}

	public TripBucketItemFlight(FlightSearchState state) {
		mFlightSearchState = state;
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.FLIGHTS;
	}

	public FlightSearchState getFlightSearchState() {
		return mFlightSearchState;
	}

	public FlightTrip getFlightTrip() {
		int numLegs = Db.getFlightSearch().getSearchParams().isRoundTrip() ? 2 : 1;
		FlightTripLeg[] legs = mFlightSearchState.getSelectedLegs(numLegs);

		return FlightSearch.getSelectedFlightTrip(mFlightSearchState.getSelectedLegs(numLegs), Db.getFlightSearch().getSearchResponse());
	}


	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = super.toJson();
			JSONUtils.putJSONable(obj, "flightSearchState", mFlightSearchState);
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
		mFlightSearchState = JSONUtils.getJSONable(obj, "flightSearchState", FlightSearchState.class);
		return true;
	}
}
