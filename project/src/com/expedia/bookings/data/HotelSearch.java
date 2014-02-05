package com.expedia.bookings.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.widget.SummarizedRoomRates;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * Contains both the parameters (see {@link com.expedia.bookings.data.HotelSearchParams}) and search
 * results (see {@link com.expedia.bookings.data.HotelSearchResponse}) for a hotel search.
 */
public class HotelSearch implements JSONable {

	// Product key lifetime: http://ask.karmalab.net/question/4116/how-long-are-piidproductkeys-valid-for
	// Note: product keys last for 60 minutes, although, in practice, rooms tend to sell out and
	// offer a poor user experience when trying to view and book. We set the timeout to 30 minutes
	// as a middle ground.
	public static final long SEARCH_DATA_TIMEOUT = 30 * DateUtils.MINUTE_IN_MILLIS;

	private HotelSearchParams mSearchParams;
	private HotelSearchResponse mSearchResponse;
	private String mSelectedPropertyId;

	// The result of a call to e3 for a coupon code discount
	private CreateTripResponse mCreateTripResponse;

	// Each map keyed off of propertyId
	private Map<String, Property> mPropertyMap;
	private Map<String, HotelAvailability> mAvailabilityMap;
	private Map<String, ReviewsResponse> mReviewsResponses;

	// Keep track if coupon is applied at checkout or not.
	private boolean mIsCouponApplied;

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
	 * Helper method to set the selected rate of the currently selected hotel.
	 * @param rate
	 */
	public void setSelectedRate(Rate rate) {
		getAvailability(mSelectedPropertyId).setSelectedRate(rate);
	}

	/**
	 * Helper method to grab the new rate from the CreateTripResponse
	 * @return the new Rate from the application of a coupon, null if no coupon has been applied
	 */
	public Rate getCouponRate() {
		if (isCouponApplied()) {
			return mCreateTripResponse.getNewRate();
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the rate that we expect to book with, which will either be
	 * the coupon rate (if a coupon is applied) or the selected rate.
	 * 
	 * This should be the preferred method for retrieving the current rate,
	 * unless you are specifically trying to get the original or coupon
	 * rate.
	 */
	public Rate getBookingRate() {
		if (isCouponApplied()) {
			return getCouponRate();
		}
		else {
			return getSelectedRate();
		}
	}

	public boolean isCouponApplied() {
		return mIsCouponApplied;
	}

	public void setCouponApplied(boolean isCouponApplied) {
		this.mIsCouponApplied = isCouponApplied;
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

	public SummarizedRoomRates getSummarizedRoomRates(String id) {
		HotelOffersResponse response = getHotelOffersResponse(id);
		if (response == null) {
			return null;
		}
		return response.getSummarizedRoomRates();
	}

	public ReviewsResponse getReviewsResponse(String id) {
		if (mReviewsResponses != null && !TextUtils.isEmpty(id)) {
			return mReviewsResponses.get(id);
		}
		return null;
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
