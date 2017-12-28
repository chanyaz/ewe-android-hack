package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.data.trips.TripDetailsResponse;
import com.mobiata.android.Log;

public class TripDetailsResponseHandler extends JsonResponseHandler<TripDetailsResponse> {

	@Override
	public TripDetailsResponse handleJson(JSONObject response) {
		TripParser parser = new TripParser();

		TripDetailsResponse tripResponse = new TripDetailsResponse();

		try {
			if (response == null) {
				return null;
			}

			// Check for errors, return if found
			tripResponse.addErrors(ParserUtils.parseErrors(ApiMethod.TRIP_DETAILS, response));
			if (!tripResponse.isSuccess()) {
				return tripResponse;
			}

			JSONObject responseData = response.optJSONObject("responseData");

			// Back-compat method of grabbing trip details response (can be deleted later)
			if (responseData == null) {
				responseData = response;
			}

			tripResponse.setTrip(parser.parseTrip(responseData));
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON trip details response", e);
			return null;
		}

		return tripResponse;
	}

}
