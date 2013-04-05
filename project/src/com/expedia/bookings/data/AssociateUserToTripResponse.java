package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;

public class AssociateUserToTripResponse extends Response {

	//04-01 14:24:55.149: V/ExpediaBookings(1850): Response: {"newTrip":{"itineraryNumber":"156115659073","travelRecordLocator":"561156590","tripId":"2cf4aa3b-91f3-4914-b361-73af085d4424"},"rewardsPoints":"69"}


	private Itinerary mItinerary;
	private String mRewardsPoints;

	public void setItinerary(Itinerary itinerary) {
		mItinerary = itinerary;
	}

	public Itinerary getItinerary() {
		return mItinerary;
	}

	public void setRewardsPoints(String rewardsPoints) {
		mRewardsPoints = rewardsPoints;
	}

	public String getRewardsPoints() {
		return mRewardsPoints;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONable(obj, "newTrip", mItinerary);
			obj.put("rewardsPoints", mRewardsPoints);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mItinerary = JSONUtils.getJSONable(obj, "newTrip", Itinerary.class);
		mRewardsPoints = obj.optString("rewardsPoints");

		return true;
	}
}
