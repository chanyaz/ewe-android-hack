package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.data.Suggestion;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.net.JsonResponseHandler;

public class SuggestResponseHandler extends JsonResponseHandler<SuggestResponse> {

	public static enum Type {
		HOTELS,
		FLIGHTS,
	}

	// Default to hotels
	private Type mType = Type.HOTELS;

	public void setType(Type type) {
		mType = type;
	}

	@Override
	public SuggestResponse handleJson(JSONObject response) {
		SuggestResponse suggestResponse = new SuggestResponse();

		if (!response.has("r")) {
			Log.d("No suggestions.");
			return null;
		}

		JSONArray responseSuggestions;
		try {
			responseSuggestions = response.getJSONArray("r");
		}
		catch (JSONException e) {
			Log.d("Could not parse JSON autosuggest response.", e);
			return null;
		}

		int len = responseSuggestions.length();
		for (int i = 0; i < len; i++) {
			try {
				JSONObject responseSuggestion = responseSuggestions.getJSONObject(i);
				Suggestion suggestion = new Suggestion();

				suggestion.setId(responseSuggestion.optString("id"));
				suggestion.setType(JSONUtils.getEnum(responseSuggestion, "t", Suggestion.Type.class));

				if (mType == Type.FLIGHTS) {
					suggestion.setDisplayName(responseSuggestion.optString("l"));
					suggestion.setAirportLocationCode(responseSuggestion.optString("a", null));
				}
				else {
					String locationName = responseSuggestion.getString("f");
					locationName = StrUtils.removeUSAFromAddress(locationName);
					suggestion.setDisplayName(locationName);
				}

				JSONObject latlng = responseSuggestion.optJSONObject("ll");
				if (latlng != null) {
					suggestion.setLatitude(latlng.getDouble("lat"));
					suggestion.setLongitude(latlng.getDouble("lng"));
				}

				suggestResponse.addSuggestion(suggestion);
			}
			catch (JSONException e) {
				Log.d("Could not parse JSON autosuggest response item.", e);
			}
		}

		return suggestResponse;
	}

}
