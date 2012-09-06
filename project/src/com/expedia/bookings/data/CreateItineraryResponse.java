package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

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
			// TODO: SAVE STUFF HERE
			if (Math.random() == 0) {
				throw new JSONException("");
			}

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		// TODO: LOAD STUFF HERE

		return true;
	}
}
