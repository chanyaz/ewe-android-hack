package com.expedia.bookings.data;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.widget.SummarizedRoomRates;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class HotelSearch implements JSONable {

	private SearchParams mSearchParams;
	private SearchResponse mSearchResponse;
	private String mSelectedPropertyId;

	// Each map keyed off of propertyId
	private Map<String, Property> mPropertyMap;
	private Map<String, HotelAvailability> mAvailabilityMap;
	private Map<String, ReviewsStatisticsResponse> mReviewsStatisticsResponses;
	private Map<String, ReviewsResponse> mReviewsResponses;

	public void reset() {
		resetSearchParams();
		clearSelectedProperty();
		mSearchResponse = null;
		mPropertyMap = null;
		mAvailabilityMap = null;
		mReviewsStatisticsResponses = null;
		mReviewsResponses = null;
	}

	public void resetSearchParams() {
		mSearchParams = new SearchParams();
	}

	public void setSearchParams(SearchParams params) {
		mSearchParams = params;
	}

	public SearchParams getSearchParams() {
		return mSearchParams;
	}

	public void setSearchResponse(SearchResponse response) {
		mSearchResponse = response;

		mPropertyMap = new HashMap<String, Property>();
		for (Property property : response.getProperties()) {
			mPropertyMap.put(property.getPropertyId(), property);
		}

		mAvailabilityMap = new HashMap<String, HotelAvailability>();
		mReviewsStatisticsResponses = new HashMap<String, ReviewsStatisticsResponse>();
		mReviewsResponses = new HashMap<String, ReviewsResponse>();
	}

	public SearchResponse getSearchResponse() {
		return mSearchResponse;
	}

	public void clearSelectedProperty() {
		mSelectedPropertyId = null;
	}

	public void setSelectedProperty(Property property) {
		mSelectedPropertyId = property.getPropertyId();
	}

	public Property getSelectedProperty() {
		return getProperty(mSelectedPropertyId);
	}

	//////////////////////////////////////////////////////////////////////////
	// Update data

	public void updateFrom(AvailabilityResponse offersResponse) {
		if (offersResponse == null || offersResponse.getProperty() == null) {
			return;
		}

		String propertyId = offersResponse.getProperty().getPropertyId();
		Property property = mPropertyMap.get(propertyId);
		property.updateFrom(offersResponse.getProperty());

		HotelAvailability availability = mAvailabilityMap.get(propertyId);
		if (availability == null) {
			availability = new HotelAvailability();
			mAvailabilityMap.put(propertyId, availability);
		}
		availability.setHotelOffersResponse(offersResponse);
	}

	public void addReviewsStatisticsResponse(String id, ReviewsStatisticsResponse response) {
		mReviewsStatisticsResponses.put(id, response);
	}

	public void addReviewsResponse(String id, ReviewsResponse response) {
		mReviewsResponses.put(id, response);
	}

	//////////////////////////////////////////////////////////////////////////
	// Get data

	public Property getProperty(String id) {
		if (mSearchResponse != null) {
			return mPropertyMap.get(id);
		}
		return null;
	}

	public HotelAvailability getAvailability(String id) {
		if (mSearchResponse != null && mAvailabilityMap != null) {
			HotelAvailability availability = mAvailabilityMap.get(id);
			return availability;
		}
		return null;
	}

	public AvailabilityResponse getHotelOffersResponse(String id) {
		HotelAvailability availability = getAvailability(id);
		if (availability == null) {
			return null;
		}
		return availability.getHotelOffersResponse();
	}

	public SummarizedRoomRates getSummarizedRoomRates(String id) {
		AvailabilityResponse response = getHotelOffersResponse(id);
		if (response == null) {
			return null;
		}
		return response.getSummarizedRoomRates();
	}

	public ReviewsStatisticsResponse getReviewsStatisticsResponse(String id) {
		return mReviewsStatisticsResponses.get(id);
	}

	public ReviewsResponse getReviewsResponse(String id) {
		return mReviewsResponses.get(id);
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONable(obj, "searchParams", mSearchParams);
			JSONUtils.putJSONable(obj, "searchResponse", mSearchResponse);
			if (mSelectedPropertyId != null) {
				String selectedId = mSelectedPropertyId;
				obj.putOpt("selectedPropertyId", selectedId);
			}

			JSONUtils.putJSONableStringMap(obj, "availabilityMap", mAvailabilityMap);
			JSONUtils.putJSONableStringMap(obj, "reviewsStatisticsResponses", mReviewsStatisticsResponses);
			JSONUtils.putJSONableStringMap(obj, "reviewsResponses", mReviewsResponses);

			return obj;
		}
		catch (JSONException e) {
			Log.e("HotelSearch toJson() failed", e);
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		setSearchParams(JSONUtils.getJSONable(obj, "searchParams", SearchParams.class));
		setSearchResponse(JSONUtils.getJSONable(obj, "searchResponse", SearchResponse.class));

		String selectedPropertyId = obj.optString("selectedPropertyId");
		if (!TextUtils.isEmpty(selectedPropertyId)) {
			setSelectedProperty(getProperty(selectedPropertyId));
		}

		mAvailabilityMap = JSONUtils.getJSONableStringMap(obj, "availabilityMap", HotelAvailability.class, mAvailabilityMap);
		mReviewsStatisticsResponses = JSONUtils.getJSONableStringMap(obj, "reviewsStatisticsResponses", ReviewsStatisticsResponse.class, mReviewsStatisticsResponses);
		mReviewsResponses = JSONUtils.getJSONableStringMap(obj, "reviewsResponses", ReviewsResponse.class, mReviewsResponses);

		return false;
	}
}
