package com.expedia.bookings.data;

import com.expedia.bookings.otto.Events;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains both the parameters (see {@link DeprecatedHotelSearchParams}) and search
 * results (see {@link com.expedia.bookings.data.HotelSearchResponse}) for a hotel search.
 */
public class HotelSearch implements JSONable {

	private DeprecatedHotelSearchParams mSearchParams;
	private HotelSearchResponse mSearchResponse;

	private Property mSelectedProperty;

	// Each map keyed off of propertyId
	private Map<String, Property> mPropertyMap;
	private Map<String, HotelAvailability> mAvailabilityMap;
	private Map<String, ReviewsResponse> mReviewsResponses;

	public HotelSearch() {
		mSearchParams = new DeprecatedHotelSearchParams();
	}

	public void setSearchParams(DeprecatedHotelSearchParams params) {
		mSearchParams = params;
	}

	public DeprecatedHotelSearchParams getSearchParams() {
		return mSearchParams;
	}

	public void setSearchResponse(HotelSearchResponse response) {
		Log.d("HotelSearch: setSearchResponse");
		mSearchResponse = response;

		mPropertyMap = new ConcurrentHashMap<>();
		if (response != null && response.getProperties() != null) {
			for (Property property : response.getProperties()) {
				mPropertyMap.put(property.getPropertyId(), property);
			}
		}

		mAvailabilityMap = new ConcurrentHashMap<>();
		mReviewsResponses = new ConcurrentHashMap<>();
	}

	public HotelSearchResponse getSearchResponse() {
		return mSearchResponse;
	}

	//////////////////////////////////////////////////////////////////////////
	// Update data

	public void updateFrom(HotelOffersResponse offersResponse) {
		if (offersResponse == null || offersResponse.getProperty() == null) {
			return;
		}

		String propertyId = offersResponse.getProperty().getPropertyId();
		Property property = mPropertyMap.get(propertyId);

		if (property == null) {
			return;
		}

		property.updateFrom(offersResponse.getProperty());

		HotelAvailability availability = mAvailabilityMap.get(propertyId);
		if (availability == null) {
			availability = new HotelAvailability();
			mAvailabilityMap.put(propertyId, availability);
		}
		availability.setHotelOffersResponse(offersResponse);
		Events.post(new Events.HotelAvailabilityUpdated());
	}

	//////////////////////////////////////////////////////////////////////////
	// Get data

	//////////////////////////////////////////////////////////////////////////
	// Booking / Checkout / Trip bucket conversion

	public HotelSearch generateForTripBucket() {
		HotelSearch hotelSearch = new HotelSearch();

		hotelSearch.mSelectedProperty = mSelectedProperty.clone();
		hotelSearch.mSearchParams = mSearchParams.clone();

		return hotelSearch;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONable(obj, "searchParams", mSearchParams);
			JSONUtils.putJSONable(obj, "searchResponse", mSearchResponse);
			JSONUtils.putJSONable(obj, "selectedProperty", mSelectedProperty);

			JSONUtils.putJSONableStringMap(obj, "availabilityMap", mAvailabilityMap);
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
		setSearchParams(JSONUtils.getJSONable(obj, "searchParams", DeprecatedHotelSearchParams.class));
		setSearchResponse(JSONUtils.getJSONable(obj, "searchResponse", HotelSearchResponse.class));

		mSelectedProperty = JSONUtils.getJSONable(obj, "selectedProperty", Property.class);

		Map<String, HotelAvailability> availabilityMap = JSONUtils.getJSONableStringMap(obj, "availabilityMap",
			HotelAvailability.class, mAvailabilityMap);
		if (availabilityMap != null) {
			mAvailabilityMap = new ConcurrentHashMap<>(availabilityMap);
		}

		Map<String, ReviewsResponse> reviewsMap = JSONUtils.getJSONableStringMap(obj, "reviewsResponses",
			ReviewsResponse.class, mReviewsResponses);
		if (reviewsMap != null) {
			mReviewsResponses = new ConcurrentHashMap<>(reviewsMap);
		}

		return true;
	}
}
