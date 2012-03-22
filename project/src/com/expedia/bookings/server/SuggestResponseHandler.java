package com.expedia.bookings.server;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.model.Search;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class SuggestResponseHandler extends JsonResponseHandler<SuggestResponse> {

	Context mContext;

	public SuggestResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public SuggestResponse handleJson(JSONObject response) {
		SuggestResponse suggestResponse = new SuggestResponse();
		ArrayList<Search> found = null;

		suggestResponse.setQuery(response.optString("q"));
		JSONArray responseSuggestions;
		try {
			responseSuggestions = response.getJSONArray("r");
		}
		catch (JSONException e) {
			Log.d("Could not parse JSON autosuggest response.", e);
			return null;
		}

		int len = responseSuggestions.length();
		found = new ArrayList<Search>(len);
		for (int i = 0; i < len; i++) {
			try {
				JSONObject responseSuggestion = responseSuggestions.getJSONObject(i);
				String locationName = responseSuggestion.getString("f");
				//String cityName = responseSuggestion.getString("s");
				//String countryName = responseSuggestion.getString("c");
				String locationId = responseSuggestion.getString("id");
				
				SearchParams searchParams = new SearchParams();
				searchParams.setFreeformLocation(locationName);
				searchParams.setDestinationId(locationId);

				JSONObject latlng = responseSuggestion.optJSONObject("ll");
				if (latlng != null) {
					double latitude = latlng.getDouble("lat");
					double longitude = latlng.getDouble("lng");
					searchParams.setSearchLatLon(latitude, longitude);
				}

				found.add(new Search(mContext, searchParams));
			}
			catch (JSONException e) {
				Log.d("Could not parse JSON autosuggest response item.", e);
			}
		}
		suggestResponse.setSuggestions(found);

		return suggestResponse;
	}

}
