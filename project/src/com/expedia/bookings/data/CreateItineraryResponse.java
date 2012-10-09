package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;

public class CreateItineraryResponse extends Response {

	private Itinerary mItinerary;

	private FlightTrip mOffer;

	public void setItinerary(Itinerary itinerary) {
		mItinerary = itinerary;
	}

	public Itinerary getItinerary() {
		return mItinerary;
	}

	public void setOffer(FlightTrip offer) {
		mOffer = offer;
	}

	public FlightTrip getOffer() {
		return mOffer;
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
			JSONUtils.putJSONable(obj, "itinerary", mItinerary);
			JSONUtils.putJSONable(obj, "offer", mOffer);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mItinerary = JSONUtils.getJSONable(obj, "itinerary", Itinerary.class);
		mOffer = JSONUtils.getJSONable(obj, "offer", FlightTrip.class);

		return true;
	}
}
