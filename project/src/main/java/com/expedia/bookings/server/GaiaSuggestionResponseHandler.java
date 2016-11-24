package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2.RegionType;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.data.SuggestionV2.SearchType;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.Log;

class GaiaSuggestionResponseHandler extends JsonResponseHandler<SuggestionResponse> {

	@Override
	public SuggestionResponse handleJson(JSONObject response) {
		SuggestionResponse suggestResponse = new SuggestionResponse();
		suggestResponse.setQuery(response.optString("q", null));

		JSONArray suggestionsArr = response.optJSONArray("response");

		if (suggestionsArr != null) {
			List<SuggestionV2> suggestions = new ArrayList<>();

			int len = suggestionsArr.length();
			for (int a = 0; a < len; a++) {
				boolean unknown = false;
				JSONObject suggestionJson = suggestionsArr.optJSONObject(a);
				try {
					SuggestionV2 suggestion = new SuggestionV2();

					suggestion.setResultType(ResultType.REGION);

					String searchType = suggestionJson.optString("type");
					if (searchType.equals("multi_city_vicinity")) {
						suggestion.setSearchType(SearchType.CITY);
					}
					else if (searchType.equals("airport")) {
						suggestion.setSearchType(SearchType.AIRPORT);
					}
					else if (!TextUtils.isEmpty(searchType)) {
						Log.w("Unknown suggest search type: \"" + searchType + "\"");
						unknown = true;
					}

					String regionType = suggestionJson.optString("type");
					if (regionType.equals("multi_city_vicinity")) {
						suggestion.setRegionType(RegionType.MULTICITY);
					}
					else if (regionType.equalsIgnoreCase("airport")) {
						suggestion.setRegionType(RegionType.AIRPORT);
					}
					else if (!TextUtils.isEmpty(regionType)) {
						Log.w("Unknown suggest region type: \"" + regionType + "\"");
						unknown = true;
					}

					if (unknown) {
						// Skip it
						continue;
					}

					suggestion.setRegionId(Integer.parseInt(suggestionJson.optString("id")));

					JSONObject regionNames = suggestionJson.optJSONArray("localizedNames").getJSONObject(0);
					suggestion.setFullName(regionNames.getString("extendedValue"));
					suggestion.setDisplayName(StrUtils
						.getDisplayNameForGaiaNearby(regionNames.getString("friendlyName"),
							regionNames.getString("airportName")));

					String airport = suggestionJson.optString("iataCode");
					if (airport != null) {
						suggestion.setAirportCode(airport);
					}

					Location location = new Location();
					JSONObject country = suggestionJson.optJSONObject("country");
					if (country != null) {
						location.setCountryCode(country.getString("code"));
					}
					JSONObject position = suggestionJson.optJSONObject("position");
					JSONArray coordinates = position.optJSONArray("coordinates");
					if (coordinates != null) {
						location.setLatitude(coordinates.getDouble(1));
						location.setLongitude(coordinates.getDouble(0));
					}
					suggestion.setLocation(location);

					suggestions.add(suggestion);
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}
			Collections.sort(suggestions);
			suggestResponse.setSuggestions(suggestions);
		}

		return suggestResponse;
	}

}
