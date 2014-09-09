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
	CreateItineraryResponse mItineraryResponse;
	FlightCheckoutResponse mCheckoutResponse;

	public TripBucketItemFlight() {

	}

	public TripBucketItemFlight(FlightSearch flightSearch) {
		mFlightSearch = flightSearch.generateForTripBucket();
		mFlightTrip = flightSearch.getSelectedFlightTrip().clone();
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.FLIGHTS;
	}

	public FlightSearchParams getFlightSearchParams() {
		return mFlightSearch.getSearchParams();
	}

	public FlightTrip getFlightTrip() {
		return mFlightTrip;
	}

	public FlightSearch getFlightSearch() {
		return mFlightSearch;
	}

	public CreateItineraryResponse getItineraryResponse() {
		return mItineraryResponse;
	}

	public Itinerary getItinerary() {
		return mItineraryResponse.getItinerary();
	}

	public void setItineraryResponse(CreateItineraryResponse itineraryResponse) {
		mItineraryResponse = itineraryResponse;
	}

	public FlightCheckoutResponse getCheckoutResponse() {
		return mCheckoutResponse;
	}

	public void setCheckoutResponse(FlightCheckoutResponse checkoutResponse) {
		mCheckoutResponse = checkoutResponse;
	}

	public void clearCheckoutData() {
		mFlightTrip.setItineraryNumber(null);
		mCheckoutResponse = null;
		mItineraryResponse = null;
	}


	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = super.toJson();
			JSONUtils.putJSONable(obj, "flightSearch", mFlightSearch);
			JSONUtils.putJSONable(obj, "flightTrip", mFlightTrip);
			JSONUtils.putJSONable(obj, "itineraryResponse", mItineraryResponse);
			JSONUtils.putJSONable(obj, "checkoutResponse", mCheckoutResponse);
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
		mItineraryResponse = JSONUtils.getJSONable(obj, "itineraryResponse", CreateItineraryResponse.class);
		mCheckoutResponse = JSONUtils.getJSONable(obj, "checkoutResponse", FlightCheckoutResponse.class);
		return true;
	}
}
