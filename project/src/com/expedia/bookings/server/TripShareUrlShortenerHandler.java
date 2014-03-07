package com.expedia.bookings.server;

import org.json.JSONObject;

import com.expedia.bookings.data.trips.TripShareUrlShortenerResponse;

public class TripShareUrlShortenerHandler extends JsonResponseHandler<TripShareUrlShortenerResponse> {

	private static final String JSON_KEY_SHORT_URL = "short_url";
	private static final String JSON_KEY_LONG_URL = "long_url";

	public TripShareUrlShortenerHandler() {
	}

	@Override
	public TripShareUrlShortenerResponse handleJson(JSONObject response) {
		if (response != null && response.has(JSON_KEY_LONG_URL) && response.has(JSON_KEY_SHORT_URL)) {
			TripShareUrlShortenerResponse resp = new TripShareUrlShortenerResponse();
			resp.setShortUrl(response.optString(JSON_KEY_SHORT_URL));
			resp.setLongUrl(response.optString(JSON_KEY_LONG_URL));
			return resp;
		}
		else {
			//This is a useless payload if we dont have long and short url information, so we return null;
			return null;
		}
	}

}
