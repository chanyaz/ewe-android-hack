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
import com.mobiata.android.Log;

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
				boolean unknown = false;
				JSONObject suggestionJson = suggestionsArr.optJSONObject(a);

				try {
					// #2128 - Filter out minor airports from results as they are never useful
					if (suggestionJson.optBoolean("isMinorAirport", false)) {
						continue;
					}

					SuggestionV2 suggestion = new SuggestionV2();

					String resultType = suggestionJson.optString("@type");
					if (resultType.equals("gaiaRegionResult")) {
						suggestion.setResultType(ResultType.REGION);
					}
					else if (resultType.equals("gaiaHotelResult")) {
						suggestion.setResultType(ResultType.HOTEL);
					}
					else if (!TextUtils.isEmpty(resultType)) {
						Log.w("Unknown suggest result type: \"" + resultType + "\"");
						unknown = true;
					}

					String searchType = suggestionJson.optString("type");
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
					else if (searchType.equals("POI")) {
						suggestion.setSearchType(SearchType.POI);
					}
					else if (searchType.equals("METROCODE")) {
						suggestion.setSearchType(SearchType.METROCODE);
					}
					else if (!TextUtils.isEmpty(searchType)) {
						Log.w("Unknown suggest search type: \"" + searchType + "\"");
						unknown = true;
					}

					if (searchType.equals("HOTEL")) {
						suggestion.setRegionId(suggestionJson.optInt("hotelId"));
					}
					else {
						suggestion.setRegionId(suggestionJson.optInt("gaiaId"));
					}

					String regionType = suggestionJson.optString("type");
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
						unknown = true;
					}

					if (unknown) {
						// Skip it
						continue;
					}

					JSONObject regionNames = suggestionJson.optJSONObject("regionNames");
					suggestion.setFullName(regionNames.getString("fullName"));
					suggestion.setDisplayName(regionNames.getString("displayName"));

					suggestion.setIndex(suggestionJson.optInt("index"));


					JSONObject hierarchyInfo = suggestionJson.optJSONObject("hierarchyInfo");
					JSONObject airport = hierarchyInfo != null ? hierarchyInfo.optJSONObject("airport") : null;
					if (airport != null) {
						suggestion.setAirportCode(airport.getString("airportCode"));
						suggestion.setMultiCityRegionId(airport.getInt("multicity"));
					}

					Location location = new Location();
					JSONObject hotelAddress = suggestionJson.optJSONObject("hotelAddress");
					if (hotelAddress != null) {
						location.addStreetAddressLine(hotelAddress.getString("street"));
						location.setCity(hotelAddress.getString("city"));
						location.setStateCode(hotelAddress.getString("province"));
					}

					JSONObject country = hierarchyInfo != null ? hierarchyInfo.optJSONObject("country") : null;
					if (country != null) {
						location.setCountryCode(country.getString("isoCode3"));
					}

					JSONObject coordinates = suggestionJson.optJSONObject("coordinates");
					if (coordinates != null) {
						location.setLatitude(coordinates.getDouble("lat"));
						location.setLongitude(coordinates.getDouble("long"));
					}
					suggestion.setLocation(location);

					suggestions.add(suggestion);
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			// They say you shouldn't trust the parsed order, and should sort it regardless
			Collections.sort(suggestions);

			suggestResponse.setSuggestions(suggestions);
		}

		return suggestResponse;
	}

}
