package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2.RegionType;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.data.SuggestionV2.SearchType;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

/**
 * Parses results from the Expedia suggest API
 * 
 * Docs: https://confluence/display/POS/Expedia+Suggest+API+Family
 */
public class SuggestionResponseHandler extends JsonResponseHandler<SuggestionResponse> {

	@Override
	public SuggestionResponse handleJson(JSONObject response) {
		SuggestionResponse suggestResponse = new SuggestionResponse();
		suggestResponse.setQuery(response.optString("q", null));

		JSONArray suggestionsArr = response.optJSONArray("sr");

		// Nearby responses return "r" instead of "sr"
		if (suggestionsArr == null) {
			suggestionsArr = response.optJSONArray("r");
		}

		if (suggestionsArr != null) {
			List<SuggestionV2> suggestions = new ArrayList<SuggestionV2>();

			int len = suggestionsArr.length();
			for (int a = 0; a < len; a++) {
				JSONObject suggestionJson = suggestionsArr.optJSONObject(a);

				SuggestionV2 suggestion = new SuggestionV2();

				String resultType = suggestionJson.optString("@type");
				if (resultType.equals("regionResult")) {
					suggestion.setResultType(ResultType.REGION);
				}
				else if (resultType.equals("hotelResult")) {
					suggestion.setResultType(ResultType.HOTEL);
				}
				else if (!TextUtils.isEmpty(resultType)) {
					Log.w("Unknown suggest result type: \"" + resultType + "\"");
				}

				String searchType = suggestionJson.optString("t");
				if (searchType.equals("CITY")) {
					suggestion.setSearchType(SearchType.CITY);
				}
				else if (searchType.equals("ATTRACTION")) {
					suggestion.setSearchType(SearchType.ATTRACTION);
				}
				else if (searchType.equals("AIRPORT")) {
					suggestion.setSearchType(SearchType.AIRPORT);
				}
				else if (searchType.equals("HOTEL")) {
					suggestion.setSearchType(SearchType.HOTEL);
				}
				else if (!TextUtils.isEmpty(searchType)) {
					Log.w("Unknown suggest search type: \"" + searchType + "\"");
				}

				String regionType = suggestionJson.optString("rt");
				if (regionType.equals("CITY")) {
					suggestion.setRegionType(RegionType.CITY);
				}
				else if (regionType.equals("MULTICITY")) {
					suggestion.setRegionType(RegionType.MULTICITY);
				}
				else if (regionType.equals("NEIGHBORHOOD")) {
					suggestion.setRegionType(RegionType.NEIGHBORHOOD);
				}
				else if (regionType.equals("POI")) {
					suggestion.setRegionType(RegionType.POI);
				}
				else if (regionType.equals("AIRPORT")) {
					suggestion.setRegionType(RegionType.AIRPORT);
				}
				else if (regionType.equals("METROCODE")) {
					suggestion.setRegionType(RegionType.METROCODE);
				}
				else if (regionType.equals("HOTEL")) {
					suggestion.setRegionType(RegionType.HOTEL);
				}
				else if (!TextUtils.isEmpty(regionType)) {
					Log.w("Unknown suggest region type: \"" + regionType + "\"");
				}

				suggestion.setFullName(suggestionJson.optString("f", null));
				suggestion.setDisplayName(suggestionJson.optString("d", null));

				suggestion.setIndex(suggestionJson.optInt("i"));

				suggestion.setHotelId(suggestionJson.optInt("id"));

				suggestion.setAirportCode(suggestionJson.optString("a", null));

				suggestion.setRegionId(suggestionJson.optInt("amc"));

				Location location = new Location();
				location.addStreetAddressLine(suggestionJson.optString("ad", null));
				location.setCity(suggestionJson.optString("ci", null));
				location.setStateCode(suggestionJson.optString("pr", null));
				location.setCountryCode(suggestionJson.optString("ccc", null));
				JSONObject latLngJson = suggestionJson.optJSONObject("ll");
				if (latLngJson != null) {
					location.setLatitude(latLngJson.optDouble("lat"));
					location.setLongitude(latLngJson.optDouble("lng"));
				}
				suggestion.setLocation(location);

				suggestions.add(suggestion);
			}

			// They say you shouldn't trust the parsed order, and should sort it regardless
			Collections.sort(suggestions);

			suggestResponse.setSuggestions(suggestions);
		}

		return suggestResponse;
	}

}
