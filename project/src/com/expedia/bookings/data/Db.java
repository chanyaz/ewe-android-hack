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

	// The filter applied to SearchResponse.  Note that this Filter can cause a memory leak;
	// One has to be sure to change the listeners on the Filter whenever appropriate.
	private Filter mFilter = new Filter();

	// Mapping of Property ID --> AvailabilityResponse
	private Map<String, AvailabilityResponse> mAvailabilityResponses = new HashMap<String, AvailabilityResponse>();

	// Mapping of Property ID --> ReviewsResponse
	private Map<String, ReviewsResponse> mReviewsResponses = new HashMap<String, ReviewsResponse>();

	// The billing info.  Make sure to properly clear this out when requested 
	private BillingInfo mBillingInfo;

	// The booking response.  Make sure to properly clear this out after finishing booking.
	private BookingResponse mBookingResponse;

	// The "currently selected" property/rate is not strictly necessary, but
	// provide a useful shorthand for commonly used functionality.  Note that
	// these will only work if you properly set them on selection.
	private String mSelectedPropertyId;
	private String mSelectedRateKey;

	// Thesea are here in the case that a single property/rate is loaded
	// (without the corresponding SearchResponse/AvailabilityResponse).
	// This can happen when reloading a single saved piece of info (such
	// as on the confirmation page).
	private Property mSelectedProperty;
	private Rate mSelectedRate;

	//////////////////////////////////////////////////////////////////////////
	// Data access

	public static SearchParams resetSearchParams() {
		sDb.mSearchParams = new SearchParams();
		return sDb.mSearchParams;
	}

	public static void setSearchParams(SearchParams searchParams) {
		sDb.mSearchParams = searchParams;
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

	public static void resetFilter() {
		sDb.mFilter.reset();
		sDb.mFilter.clearOnFilterChangedListeners();
	}

	public static Filter getFilter() {
		return sDb.mFilter;
	}

	public static Property getProperty(String propertyId) {
		return (sDb.mSearchResponse != null) ? sDb.mSearchResponse.getProperty(propertyId) : null;
	}

	public static void setSelectedProperty(Property property) {
		sDb.mSelectedProperty = property;
		setSelectedProperty(property.getPropertyId());
	}

	public static void setSelectedProperty(String propertyId) {
		sDb.mSelectedPropertyId = propertyId;
	}

	public static Property getSelectedProperty() {
		if (sDb.mSelectedProperty != null) {
			return sDb.mSelectedProperty;
		}
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
		sDb.mSelectedRate = rate;
		setSelectedRate(rate.getRateKey());
	}

	public static void setSelectedRate(String rateKey) {
		sDb.mSelectedRateKey = rateKey;
	}

	public static Rate getSelectedRate() {
		if (sDb.mSelectedRate != null) {
			return sDb.mSelectedRate;
		}
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

	public static void setBillingInfo(BillingInfo billingInfo) {
		sDb.mBillingInfo = billingInfo;
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

	public static void setBookingResponse(BookingResponse bookingResponse) {
		sDb.mBookingResponse = bookingResponse;
	}

	public static BookingResponse getBookingResponse() {
		return sDb.mBookingResponse;
	}
}
