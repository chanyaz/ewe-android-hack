package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.AssociateUserToTripResponse;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;

public class AssociateUserToTripResponseHandler extends JsonResponseHandler<AssociateUserToTripResponse> {

	private final Context mContext;

	public AssociateUserToTripResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public AssociateUserToTripResponse handleJson(JSONObject response) {
		AssociateUserToTripResponse assocUserToTripResp = new AssociateUserToTripResponse();

		try {
			assocUserToTripResp
					.addErrors(ParserUtils.parseErrors(ApiMethod.ASSOCIATE_USER_TO_TRIP, response));
			if (!assocUserToTripResp.isSuccess()) {
				return assocUserToTripResp;
			}
		}
		catch (JSONException e) {
			Log.e("Error parsing create flight itinerary response JSON", e);
			return null;
		}

		if (response != null && response.has("newTrip") && response.has("rewardsPoints")) {
			//Rewards points
			String rewardsPoints = response.optString("rewardsPoints");
			if (!TextUtils.isEmpty(rewardsPoints)) {
				assocUserToTripResp.setRewardsPoints(rewardsPoints);
			}

			// Parse itinerary
			JSONObject itineraryJson = response.optJSONObject("newTrip");
			Itinerary itinerary = new Itinerary();
			itinerary.setItineraryNumber(itineraryJson.optString("itineraryNumber"));
			itinerary.setTravelRecordLocator(itineraryJson.optString("travelRecordLocator"));
			itinerary.setTripId(itineraryJson.optString("tripId"));
			assocUserToTripResp.setItinerary(itinerary);
		}
		else {
			ServerError missingValuesError = new ServerError(ApiMethod.ASSOCIATE_USER_TO_TRIP);
			missingValuesError.setMessage("Required fields were missing from the server response.");
			assocUserToTripResp.addError(missingValuesError);
		}

		return assocUserToTripResp;
	}
}
