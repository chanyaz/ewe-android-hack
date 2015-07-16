package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.data.trips.TripResponse;
import com.mobiata.android.Log;

public class TripResponseHandler extends JsonResponseHandler<TripResponse> {

	private Context mContext;

	public TripResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public TripResponse handleJson(JSONObject response) {
		TripParser parser = new TripParser();

		TripResponse tripResponse = new TripResponse();

		try {
			// Check for errors, return if found
			tripResponse.addErrors(ParserUtils.parseErrors(ApiMethod.TRIPS, response));
			if (!tripResponse.isSuccess()) {
				return tripResponse;
			}

			JSONArray tripsArr = response.optJSONArray("responseData");

			// Back-compat method of grabbing trips response (can be deleted later)
			if (tripsArr == null) {
				tripsArr = response.optJSONArray("response");
			}

			int len = tripsArr.length();
			for (int a = 0; a < len; a++) {
				JSONObject tripJson = tripsArr.optJSONObject(a);
				tripResponse.addTrip(parser.parseTrip(tripJson));
			}

			// TripParser.parseTrip modifies global air attach qualification state.
			// We save the trip bucket to disk here to ensure we show air attach
			// messaging when re-launching the app.
			Db.saveTripBucket(mContext);
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON trip response", e);
			return null;
		}

		return tripResponse;
	}
}
