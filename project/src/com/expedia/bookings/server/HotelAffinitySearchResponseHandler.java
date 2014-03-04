package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.HotelAffinitySearchResponse;
import com.expedia.bookings.data.Property;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class HotelAffinitySearchResponseHandler extends JsonResponseHandler<HotelAffinitySearchResponse> {

	// http://afs.integration.bgb.karmalab.net:52418/affinity/api/v1/get/hotels?format=json&regionId=3121&userId=ama

	@Override
	public HotelAffinitySearchResponse handleJson(JSONObject response) {
		HotelAffinitySearchResponse affinityResponse = new HotelAffinitySearchResponse();

		try {
			JSONObject searchResultsJson = response.getJSONObject("searchResults");
			JSONArray searchResultsJsonArr = searchResultsJson.getJSONArray("searchResult");
			JSONObject searchResultJson = searchResultsJsonArr.getJSONObject(0);

			JSONObject resultsJson = searchResultJson.getJSONObject("results");
			JSONArray resultJsonArr = resultsJson.getJSONArray("result");
			List<Property> properties = new ArrayList<Property>();

			for (int i = 0; i < resultJsonArr.length(); i++) {
				JSONObject propertyJson = resultJsonArr.getJSONObject(i).getJSONObject("item");
				Property property = new Property();

				property.setName(propertyJson.getString("geoName"));
				property.setAverageExpediaRating(propertyJson.getDouble("guestRating"));
				property.setHotelRating(propertyJson.getDouble("starRating"));

				properties.add(property);
			}

			affinityResponse.setProperties(properties);
		}
		catch (JSONException e) {
			Log.e("Unable to parse Flight Search Histogram response");
			return null;
		}

		return affinityResponse;
	}

}
