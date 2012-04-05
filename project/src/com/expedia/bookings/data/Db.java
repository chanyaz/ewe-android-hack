package com.expedia.bookings.data;

import java.util.HashMap;
import java.util.Map;

import com.expedia.bookings.widget.SummarizedRoomRates;

import android.content.Context;

/**
 * This represents an in-memory database of data for the app.
 * 
 * Try to keep out information that is state data for a fragment.  For example,
 * keeping track of whether a field has been clicked is not for this.  This is
 * more for passing data between Activities.
 * 
 * Also, be sure to NEVER add anything that could leak memory (such as a Context).
 */
public class Db {

	//////////////////////////////////////////////////////////////////////////
	// Singleton setup
	//
	// We configure this as a singleton in case we ever need to handle
	// multiple instances of Db in the future.  Doubtful, but no reason not
	// to set things up this way.

	private static final Db sDb = new Db();

	private Db() {
		// Cannot be instantiated
	}

	//////////////////////////////////////////////////////////////////////////
	// Stored data

	// The search params (the details for how to do a search)
	private SearchParams mSearchParams = new SearchParams();

	// The search response (should correspond at all times to the SearchParams, or be null if SearchParams
	// has changed).
	private SearchResponse mSearchResponse;

	// Mapping of Property ID --> AvailabilityResponse
	private Map<String, AvailabilityResponse> mAvailabilityResponses = new HashMap<String, AvailabilityResponse>();

	// Mapping of Property ID --> ReviewsResponse
	private Map<String, ReviewsResponse> mReviewsResponses = new HashMap<String, ReviewsResponse>();

	// The billing info.  Make sure to properly clear this out when requested 
	private BillingInfo mBillingInfo;

	// The "currently selected" property/rate is not strictly necessary, but
	// provide a useful shorthand for commonly used functionality.  Note that
	// these will only work if you properly set them on selection.
	private String mSelectedPropertyId;
	private String mSelectedRateKey;

	//////////////////////////////////////////////////////////////////////////
	// Data access

	public static SearchParams resetSearchParams() {
		sDb.mSearchParams = new SearchParams();
		return sDb.mSearchParams;
	}

	public static SearchParams getSearchParams() {
		return sDb.mSearchParams;
	}

	public static void setSearchResponse(SearchResponse searchResponse) {
		sDb.mSearchResponse = searchResponse;
	}

	public static SearchResponse getSearchResponse() {
		return sDb.mSearchResponse;
	}

	public static Property getProperty(String propertyId) {
		return (sDb.mSearchResponse != null) ? sDb.mSearchResponse.getProperty(propertyId) : null;
	}

	public static void setSelectedProperty(Property property) {
		setSelectedProperty(property.getPropertyId());
	}

	public static void setSelectedProperty(String propertyId) {
		sDb.mSelectedPropertyId = propertyId;
	}

	public static Property getSelectedProperty() {
		return getProperty(sDb.mSelectedPropertyId);
	}

	public static void clearAvailabilityResponses() {
		sDb.mAvailabilityResponses.clear();
	}

	public static void addAvailabilityResponse(AvailabilityResponse availabilityResponse) {
		sDb.mAvailabilityResponses.put(sDb.mSelectedPropertyId, availabilityResponse);
	}

	public static AvailabilityResponse getAvailabilityResponse(String propertyId) {
		return sDb.mAvailabilityResponses.get(propertyId);
	}

	public static AvailabilityResponse getSelectedAvailabilityResponse() {
		return sDb.mAvailabilityResponses.get(sDb.mSelectedPropertyId);
	}

	public static SummarizedRoomRates getSummarizedRoomRates(String propertyId) {
		AvailabilityResponse response = getAvailabilityResponse(propertyId);
		if (response == null) {
			return null;
		}
		return response.getSummarizedRoomRates();
	}

	public static SummarizedRoomRates getSelectedSummarizedRoomRates() {
		return getSummarizedRoomRates(sDb.mSelectedPropertyId);
	}

	public static Rate getRate(String propertyId, String rateKey) {
		if (!sDb.mAvailabilityResponses.containsKey(propertyId)) {
			return null;
		}
		return sDb.mAvailabilityResponses.get(propertyId).getRate(rateKey);
	}

	public static void setSelectedRate(Rate rate) {
		setSelectedRate(rate.getRateKey());
	}

	public static void setSelectedRate(String rateKey) {
		sDb.mSelectedRateKey = rateKey;
	}

	public static Rate getSelectedRate() {
		return getRate(sDb.mSelectedPropertyId, sDb.mSelectedRateKey);
	}

	public static void clearReviewsResponses() {
		sDb.mReviewsResponses.clear();
	}

	public static void addReviewsResponse(ReviewsResponse reviewsResponse) {
		addReviewsResponse(sDb.mSelectedPropertyId, reviewsResponse);
	}

	public static void addReviewsResponse(String propertyId, ReviewsResponse reviewsResponse) {
		sDb.mReviewsResponses.put(propertyId, reviewsResponse);
	}

	public static ReviewsResponse getReviewsResponse(String propertyId) {
		return sDb.mReviewsResponses.get(propertyId);
	}

	public static ReviewsResponse getSelectedReviewsResponse() {
		return getReviewsResponse(sDb.mSelectedPropertyId);
	}

	public static boolean loadBillingInfo(Context context) {
		sDb.mBillingInfo = new BillingInfo();
		return sDb.mBillingInfo.load(context);
	}

	public static BillingInfo resetBillingInfo() {
		sDb.mBillingInfo = new BillingInfo();
		return sDb.mBillingInfo;
	}

	public static BillingInfo getBillingInfo() {
		if (sDb.mBillingInfo == null) {
			throw new RuntimeException("Need to call Database.loadBillingInfo() before attempting to use BillingInfo.");
		}

		return sDb.mBillingInfo;
	}

	public static void deleteBillingInfo(Context context) {
		if (sDb.mBillingInfo != null) {
			sDb.mBillingInfo.delete(context);
		}
	}
}
