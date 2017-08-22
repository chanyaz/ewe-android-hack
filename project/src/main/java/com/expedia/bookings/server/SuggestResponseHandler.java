package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.data.Suggestion;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class SuggestResponseHandler extends JsonResponseHandler<SuggestResponse> {

	public enum Type {
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

		if (!response.has("sr")) {
			Log.d("No suggestions.");
			return null;
		}

		JSONArray responseSuggestions;
		try {
			responseSuggestions = response.getJSONArray("sr");
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

				suggestion.setType(JSONUtils.getEnum(responseSuggestion, "type", Suggestion.Type.class));

				if (suggestion.getType() == Suggestion.Type.HOTEL) {
					suggestion.setId(responseSuggestion.optString("hotelId"));
				}
				else {
					suggestion.setId(responseSuggestion.optString("gaiaId"));
				}

				JSONObject country = responseSuggestion.optJSONObject("hierarchyInfo").optJSONObject("country");
				if (country != null) {
					suggestion.setCountryCode(country.getString("isoCode3"));
				}

				if (mType == Type.FLIGHTS) {
					JSONObject airport = responseSuggestion.optJSONObject("hierarchyInfo").optJSONObject("airport");
					if (airport != null) {
						suggestion.setAirportLocationCode(airport.optString("airportCode", null));
					}
				}

				JSONObject regionNames = responseSuggestion.optJSONObject("regionNames");
				if (regionNames != null) {
					String locationName = regionNames.getString("displayName");
					// Remove all html tags
					locationName = locationName.replaceAll("<[^>]*>", "");

					locationName = StrUtils.removeUSAFromAddress(locationName);
					suggestion.setDisplayName(locationName);
				}

				JSONObject latlng = responseSuggestion.optJSONObject("coordinates");
				if (latlng != null) {
					suggestion.setLatitude(latlng.getDouble("lat"));
					suggestion.setLongitude(latlng.getDouble("long"));
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
