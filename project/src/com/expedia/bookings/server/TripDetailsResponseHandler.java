package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.data.trips.TripDetailsResponse;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class TripDetailsResponseHandler extends JsonResponseHandler<TripDetailsResponse> {

	private Context mContext;

	public TripDetailsResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public TripDetailsResponse handleJson(JSONObject response) {
		TripParser parser = new TripParser();

		TripDetailsResponse tripResponse = new TripDetailsResponse();

		try {
			// Check for errors, return if found
			tripResponse.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.TRIP_DETAILS, response));
			if (!tripResponse.isSuccess()) {
				return tripResponse;
			}

			tripResponse.setTrip(parser.parseTrip(response));
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON trip details response", e);
			return null;
		}

		return tripResponse;
	}

}
