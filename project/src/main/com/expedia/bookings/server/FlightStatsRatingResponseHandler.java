package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.FlightStatsRating;
import com.expedia.bookings.data.FlightStatsRatingResponse;
import com.mobiata.android.Log;

public class FlightStatsRatingResponseHandler extends JsonResponseHandler<FlightStatsRatingResponse> {

	public FlightStatsRatingResponseHandler() {

	}

	@Override
	public FlightStatsRatingResponse handleJson(JSONObject response) {
		FlightStatsRatingResponse fsResponse = new FlightStatsRatingResponse();

		try {
			FlightStatsRating fsRating = new FlightStatsRating();

			JSONArray ratingsArrJson = response.getJSONArray("ratings");
			JSONObject ratingJson = ratingsArrJson.getJSONObject(0);
			fsRating.setOnTimePercent(ratingJson.getDouble("ontimePercent"));
			fsRating.setNumObservations(ratingJson.getInt("observations"));

			fsResponse.setFlightStatsRating(fsRating);
		}
		catch (JSONException e) {
			Log.w("Error parsing Flight rating response from FlightStats", e);
			return null;
		}

		return fsResponse;
	}

}
