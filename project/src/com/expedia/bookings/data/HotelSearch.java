package com.expedia.bookings.data;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.widget.SummarizedRoomRates;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class HotelSearch implements JSONable {

	private HotelSearchParams mSearchParams;
	private HotelSearchResponse mSearchResponse;
	private String mSelectedPropertyId;

	// The result of a call to e3 for a coupon code discount
	private CreateTripResponse mCreateTripResponse;

	// Each map keyed off of propertyId
	private Map<String, Property> mPropertyMap;
	private Map<String, HotelAvailability> mAvailabilityMap;
	private Map<String, ReviewsStatisticsResponse> mReviewsStatisticsResponses;
	private Map<String, ReviewsResponse> mReviewsResponses;

	public HotelSearch() {
		mSearchParams = new HotelSearchParams();
	}

	public void resetSearchData() {
		Log.d("HotelSearch: resetSearchData");
		clearSelectedProperty();
		mSearchResponse = null;
		mPropertyMap = null;
		mAvailabilityMap = null;
		mReviewsStatisticsResponses = null;
		mReviewsResponses = null;
	}

	public void resetSearchParams() {
		Log.d("HotelSearch: resetSearchParams");
		mSearchParams = new HotelSearchParams();
	}

	public void setSearchParams(HotelSearchParams params) {
		mSearchParams = params;
	}

	public HotelSearchParams getSearchParams() {
		return mSearchParams;
	}

	public void setSearchResponse(HotelSearchResponse response) {
		Log.d("HotelSearch: setSearchResponse");
		mSearchResponse = response;

		mPropertyMap = new HashMap<String, Property>();
		if (response != null && response.getProperties() != null) {
			for (Property property : response.getProperties()) {
				mPropertyMap.put(property.getPropertyId(), property);
			}
		}

		mAvailabilityMap = new HashMap<String, HotelAvailability>();
		mReviewsStatisticsResponses = new HashMap<String, ReviewsStatisticsResponse>();
		mReviewsResponses = new HashMap<String, ReviewsResponse>();
	}

	public HotelSearchResponse getSearchResponse() {
		return mSearchResponse;
	}

	public void setCreateTripResponse(CreateTripResponse createTripResponse) {
		mCreateTripResponse = createTripResponse;
	}

	public CreateTripResponse getCreateTripResponse() {
		return mCreateTripResponse;
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

	public String getSelectedPropertyId() {
		return mSelectedPropertyId;
	}

	/**
	 * Helper method to grab the rate of the currently selected room, based solely on the availability (and not coupon)
	 * @return the currently selected rate, selected from the rooms and rates screen
	 */
	public Rate getSelectedRate() {
		return getAvailability(mSelectedPropertyId).getSelectedRate();
	}

	/**
	 * Helper method to grab the new rate from the CreateTripResponse
	 * @return the new Rate from the application of a coupon, null if no coupon has been applied
	 */
	public Rate getCouponRate() {
		if (mCreateTripResponse == null) {
			return null;
		}
		return mCreateTripResponse.getNewRate();
	}

	//////////////////////////////////////////////////////////////////////////
	// Update data

	public void updateFrom(HotelOffersResponse offersResponse) {
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

	public void removeProperty(String id) {
		if (id != null) {
			Property property = getProperty(id);
			mSearchResponse.removeProperty(property);

			mPropertyMap.remove(id);
			mAvailabilityMap.remove(id);
			mReviewsStatisticsResponses.remove(id);
			mReviewsResponses.remove(id);

		}
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

	public HotelOffersResponse getHotelOffersResponse(String id) {
		HotelAvailability availability = getAvailability(id);
		if (availability == null) {
			return null;
		}
		return availability.getHotelOffersResponse();
	}

	public SummarizedRoomRates getSummarizedRoomRates(String id) {
		HotelOffersResponse response = getHotelOffersResponse(id);
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
			obj.putOpt("selectedPropertyId", mSelectedPropertyId);
			JSONUtils.putJSONable(obj, "createTripResponse", mCreateTripResponse);

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
		setSearchParams(JSONUtils.getJSONable(obj, "searchParams", HotelSearchParams.class));
		setSearchResponse(JSONUtils.getJSONable(obj, "searchResponse", HotelSearchResponse.class));

		mSelectedPropertyId = obj.optString("selectedPropertyId", null);
		mCreateTripResponse = JSONUtils.getJSONable(obj, "createTripResponse", CreateTripResponse.class);

		mAvailabilityMap = JSONUtils.getJSONableStringMap(obj, "availabilityMap", HotelAvailability.class,
				mAvailabilityMap);
		mReviewsStatisticsResponses = JSONUtils.getJSONableStringMap(obj, "reviewsStatisticsResponses",
				ReviewsStatisticsResponse.class, mReviewsStatisticsResponses);
		mReviewsResponses = JSONUtils.getJSONableStringMap(obj, "reviewsResponses", ReviewsResponse.class,
				mReviewsResponses);

		return true;
	}
}
