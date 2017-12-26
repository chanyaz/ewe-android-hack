package com.expedia.bookings.data;

import android.text.TextUtils;

import com.expedia.bookings.otto.Events;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains both the parameters (see {@link com.expedia.bookings.data.HotelSearchParams}) and search
 * results (see {@link com.expedia.bookings.data.HotelSearchResponse}) for a hotel search.
 */
public class HotelSearch implements JSONable {

	private HotelSearchParams mSearchParams;
	private HotelSearchResponse mSearchResponse;

	private Property mSelectedProperty;

	// Each map keyed off of propertyId
	private Map<String, Property> mPropertyMap;
	private Map<String, HotelAvailability> mAvailabilityMap;
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
		mReviewsResponses = null;
		mSelectedProperty = null;
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

		mPropertyMap = new ConcurrentHashMap<String, Property>();
		if (response != null && response.getProperties() != null) {
			for (Property property : response.getProperties()) {
				mPropertyMap.put(property.getPropertyId(), property);
			}
		}

		mAvailabilityMap = new ConcurrentHashMap<String, HotelAvailability>();
		mReviewsResponses = new ConcurrentHashMap<String, ReviewsResponse>();
	}

	public HotelSearchResponse getSearchResponse() {
		return mSearchResponse;
	}

	public void clearSelectedProperty() {
		mSelectedProperty = null;
	}

	public void setSelectedProperty(Property property) {
		mSelectedProperty = property;
	}

	public Property getSelectedProperty() {
		// Sponsored hotels have duplicate keys, the non sponsored property
		// is the one being updated in the property map, so get that one for
		// setting up our views.
		if (mSelectedProperty != null) {
			Property property =  Db.getHotelSearch().getProperty(mSelectedProperty.getPropertyId());
			if (property != null) {
				return property;
			}
		}
		return mSelectedProperty;

	}

	public String getSelectedPropertyId() {
		return mSelectedProperty.getPropertyId();
	}

	/**
	 * Helper method to grab the rate of the currently selected room, based solely on the availability (and not coupon)
	 *
	 * @return the currently selected rate, selected from the rooms and rates screen
	 */
	public Rate getSelectedRate() {
		HotelAvailability availability = getAvailability(mSelectedProperty.getPropertyId());
		if (availability != null) {
			return availability.getSelectedRate();
		}
		return null;
	}

	/**
	 * Helper method to set the selected rate of the currently selected hotel.
	 *
	 * @param rate
	 */
	public void setSelectedRate(Rate rate) {
		HotelAvailability availability = getAvailability(mSelectedProperty.getPropertyId());
		availability.setSelectedRate(rate);
		Events.post(new Events.HotelRateSelected());
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

	public void addReviewsResponse(String id, ReviewsResponse response) {
		mReviewsResponses.put(id, response);
	}

	public void removeProperty(String id) {
		if (id != null) {
			Property property = getProperty(id);
			mSearchResponse.removeProperty(property);

			mPropertyMap.remove(id);
			mAvailabilityMap.remove(id);
			mReviewsResponses.remove(id);

		}
	}
	//////////////////////////////////////////////////////////////////////////
	// Get data

	public Property getProperty(String id) {
		if (mSearchResponse != null && !TextUtils.isEmpty(id)) {
			return mPropertyMap.get(id);
		}
		return null;
	}

	public HotelAvailability getAvailability(String id) {
		if (mSearchResponse != null && mAvailabilityMap != null && !TextUtils.isEmpty(id)) {
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

	public ReviewsResponse getReviewsResponse(String id) {
		if (mReviewsResponses != null && !TextUtils.isEmpty(id)) {
			return mReviewsResponses.get(id);
		}
		return null;
	}

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
		setSearchParams(JSONUtils.getJSONable(obj, "searchParams", HotelSearchParams.class));
		setSearchResponse(JSONUtils.getJSONable(obj, "searchResponse", HotelSearchResponse.class));

		mSelectedProperty = JSONUtils.getJSONable(obj, "selectedProperty", Property.class);

		Map<String, HotelAvailability> availabilityMap = JSONUtils.getJSONableStringMap(obj, "availabilityMap",
			HotelAvailability.class, mAvailabilityMap);
		if (availabilityMap != null) {
			mAvailabilityMap = new ConcurrentHashMap<String, HotelAvailability>(availabilityMap);
		}

		Map<String, ReviewsResponse> reviewsMap = JSONUtils.getJSONableStringMap(obj, "reviewsResponses",
			ReviewsResponse.class, mReviewsResponses);
		if (reviewsMap != null) {
			mReviewsResponses = new ConcurrentHashMap<String, ReviewsResponse>(reviewsMap);
		}

		return true;
	}
}
