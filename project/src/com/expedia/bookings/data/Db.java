package com.expedia.bookings.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Process;

import com.expedia.bookings.widget.SummarizedRoomRates;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;

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

	// Mapping of Property ID --> AvailabilityResponse (for HotelInformation calls)
	private Map<String, AvailabilityResponse> mInfoResponses = new HashMap<String, AvailabilityResponse>();

	// Mapping of Property ID --> AvailabilityResponse (for expensive realtime calls)
	private Map<String, AvailabilityResponse> mAvailabilityResponses = new HashMap<String, AvailabilityResponse>();

	// Mapping of Property ID --> ReviewsStatisticsResponse
	private Map<String, ReviewsStatisticsResponse> mReviewsStatisticsResponses = new HashMap<String, ReviewsStatisticsResponse>();

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

	// These are here in the case that a single property/rate is loaded
	// (without the corresponding SearchResponse/AvailabilityResponse).
	// This can happen when reloading a single saved piece of info (such
	// as on the confirmation page).
	private Property mSelectedProperty;
	private Rate mSelectedRate;

	// The currently logged in User profile
	private User mUser;

	// Flight search object - represents both the parameters and
	// the returned results
	private FlightSearch mFlightSearch = new FlightSearch();

	// Itineraries (for flights and someday hotels)
	private Map<String, Itinerary> mItineraries = new HashMap<String, Itinerary>();

	// Flight Travelers
	private ArrayList<Traveler> mTravelers = new ArrayList<Traveler>();

	// The result of a call to e3 for a coupon code discount
	private CreateTripResponse mCreateTripResponse;
	private Rate mCouponDiscountRate;

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

	public static void setFilter(Filter filter) {
		sDb.mFilter = filter;
	}

	public static Filter getFilter() {
		return sDb.mFilter;
	}

	public static Property getProperty(String propertyId) {
		return (sDb.mSearchResponse != null) ? sDb.mSearchResponse.getProperty(propertyId) : null;
	}

	public static void clearSelectedProperty() {
		sDb.mSelectedPropertyId = null;
		sDb.mSelectedProperty = null;
	}

	public static void setSelectedProperty(Property property) {
		sDb.mSelectedProperty = property;
		if (property != null) {
			setSelectedProperty(property.getPropertyId());
		}
		else {
			setSelectedProperty("");
		}
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
		sDb.mInfoResponses.clear();
	}

	public static AvailabilityResponse getSelectedInfoResponse() {
		return getInfoResponse(sDb.mSelectedPropertyId);
	}

	public static AvailabilityResponse getInfoResponse(String propertyId) {
		AvailabilityResponse response = sDb.mInfoResponses.get(propertyId);

		// Try to retrieve it out of the availability responses, as long as
		// it doesn't have any errors
		if (response == null) {
			response = getAvailabilityResponse(propertyId);
			if (response == null || response.hasErrors()) {
				return null;
			}
		}

		return response;
	}

	public static void addAvailabilityResponse(AvailabilityResponse availabilityResponse) {
		if (availabilityResponse != null && availabilityResponse.getProperty() != null) {
			if (availabilityResponse.canRequestMoreData()) {
				sDb.mInfoResponses.put(availabilityResponse.getProperty().getPropertyId(), availabilityResponse);
			}
			else {
				sDb.mAvailabilityResponses
						.put(availabilityResponse.getProperty().getPropertyId(), availabilityResponse);
			}
		}
	}

	public static AvailabilityResponse getAvailabilityResponse(String propertyId) {
		return sDb.mAvailabilityResponses.get(propertyId);
	}

	public static AvailabilityResponse getSelectedAvailabilityResponse() {
		return getAvailabilityResponse(sDb.mSelectedPropertyId);
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

	public static void clearReviewsStatisticsResponses() {
		sDb.mReviewsStatisticsResponses.clear();
	}

	public static void addReviewsStatisticsResponse(ReviewsStatisticsResponse reviewsStatisticsResponse) {
		addReviewsStatisticsResponse(sDb.mSelectedPropertyId, reviewsStatisticsResponse);
	}

	public static void addReviewsStatisticsResponse(String propertyId,
			ReviewsStatisticsResponse reviewsStatisticsResponse) {
		sDb.mReviewsStatisticsResponses.put(propertyId, reviewsStatisticsResponse);
	}

	public static ReviewsStatisticsResponse getReviewsStatisticsResponse(String propertyId) {
		return sDb.mReviewsStatisticsResponses.get(propertyId);
	}

	public static ReviewsStatisticsResponse getSelectedReviewsStatisticsResponse() {
		return getReviewsStatisticsResponse(sDb.mSelectedPropertyId);
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

	public static void loadUser(Context context) {
		sDb.mUser = new User(context);
	}

	public static void setUser(User user) {
		sDb.mUser = user;
	}

	public static User getUser() {
		return sDb.mUser;
	}

	public static FlightSearch getFlightSearch() {
		return sDb.mFlightSearch;
	}

	public static void addItinerary(Itinerary itinerary) {
		sDb.mItineraries.put(itinerary.getItineraryNumber(), itinerary);
	}

	public static Itinerary getItinerary(String itineraryNumber) {
		return sDb.mItineraries.get(itineraryNumber);
	}

	public static ArrayList<Traveler> getTravelers() {
		return sDb.mTravelers;
	}

	public static void setTravelers(ArrayList<Traveler> travelers) {
		sDb.mTravelers = travelers;
	}

	public static void setCreateTripResponse(CreateTripResponse response) {
		sDb.mCreateTripResponse = response;
	}

	public static CreateTripResponse getCreateTripResponse() {
		return sDb.mCreateTripResponse;
	}

	public static void setCouponDiscountRate(Rate couponDiscountRate) {
		sDb.mCouponDiscountRate = couponDiscountRate;
	}

	public static Rate getCouponDiscountRate() {
		return sDb.mCouponDiscountRate;
	}

	public static void clear() {
		clearAvailabilityResponses();
		clearReviewsResponses();
		resetFilter();
		resetBillingInfo();
		resetSearchParams();

		sDb.mSelectedPropertyId = null;
		sDb.mSelectedProperty = null;
		sDb.mSelectedRateKey = null;
		sDb.mSelectedRate = null;
		sDb.mSearchResponse = null;
		sDb.mBookingResponse = null;
		sDb.mUser = null;

		sDb.mFlightSearch.reset();
		sDb.mTravelers.clear();
	}

	//////////////////////////////////////////////////////////////////////////
	// Saving/loading data

	private static final String SAVED_FLIGHT_DATA_FILE = "flights-data.db";

	/**
	 * MAKE SURE TO CALL THIS IN A NON-UI THREAD
	 */
	public static boolean saveFlightDataCache(Context context) {
		synchronized (sDb) {
			Log.d("Saving flight data cache...");

			long start = System.currentTimeMillis();
			try {
				JSONObject obj = new JSONObject();
				JSONUtils.putJSONable(obj, "flightSearch", sDb.mFlightSearch);

				List<Itinerary> itineraryList = new ArrayList<Itinerary>(sDb.mItineraries.values());
				JSONUtils.putJSONableList(obj, "itineraries", itineraryList);

				String json = obj.toString();
				IoUtils.writeStringToFile(SAVED_FLIGHT_DATA_FILE, json, context);

				Log.d("Saved flight data cache in " + (System.currentTimeMillis() - start)
						+ " ms.  Size of data cache: "
						+ json.length() + " chars");

				return true;
			}
			catch (Exception e) {
				// It's not a severe issue if this all fails - just 
				Log.w("Failed to save flight data", e);
				return false;
			}
			catch (OutOfMemoryError err) {
				Log.e("Ran out of memory trying to save flight data cache", err);
				throw new RuntimeException(err);
			}

		}
	}

	public static boolean loadCachedFlightData(Context context) {
		long start = System.currentTimeMillis();

		File file = context.getFileStreamPath(SAVED_FLIGHT_DATA_FILE);
		if (!file.exists()) {
			return false;
		}

		try {
			JSONObject obj = new JSONObject(IoUtils.readStringFromFile(SAVED_FLIGHT_DATA_FILE, context));

			if (obj.has("flightSearch")) {
				sDb.mFlightSearch = JSONUtils.getJSONable(obj, "flightSearch", FlightSearch.class);
			}
			if (obj.has("itineraries")) {
				List<Itinerary> itineraries = JSONUtils.getJSONableList(obj, "itineraries", Itinerary.class);
				for (Itinerary itinerary : itineraries) {
					addItinerary(itinerary);
				}
			}

			Log.d("Loaded cached flight data in " + (System.currentTimeMillis() - start) + " ms");

			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static boolean deleteCachedFlightData(Context context) {
		File file = context.getFileStreamPath(SAVED_FLIGHT_DATA_FILE);
		if (!file.exists()) {
			return true;
		}
		else {
			return file.delete();
		}
	}

	public static void kickOffBackgroundSave(final Context context) {
		// #517: Trying to track down issues
		try {
			throw new RuntimeException("FAKE EXCEPTION FOR STACK TRACE");
		}
		catch (Exception e) {
			Log.d("Kicking off background flight data save", e);
		}

		// Kick off a search to cache results to disk, in case app is killed
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
				Db.saveFlightDataCache(context);
			}
		})).start();
	}

	//////////////////////////////////////////////////////////////////////////
	// Test data
	//
	// Allows you to "checkpoint" the database using saveDbForTesting(), then
	// restore the entire db by using loadTestData().  You can use this to
	// test a particular dataset, or to reload the same Activity over and
	// over again.
	//
	// ONLY USE IN DEBUG BUILDS

	private static final String TEST_DATA_FILE = "testdata.json";

	public static void saveDbForTesting(Context context) {
		Log.i("Saving test data...");

		JSONObject obj = new JSONObject();

		try {
			putJsonable(obj, "searchParams", sDb.mSearchParams);
			putJsonable(obj, "searchResponse", sDb.mSearchResponse);
			putJsonable(obj, "filter", sDb.mFilter);
			putMap(obj, "info", sDb.mInfoResponses);
			putMap(obj, "offers", sDb.mAvailabilityResponses);
			putMap(obj, "reviewsStatistics", sDb.mReviewsStatisticsResponses);
			putMap(obj, "reviews", sDb.mReviewsResponses);
			putJsonable(obj, "billingInfo", sDb.mBillingInfo);
			putJsonable(obj, "bookingResponse", sDb.mBookingResponse);
			obj.putOpt("selectedPropertyId", sDb.mSelectedPropertyId);
			obj.putOpt("selectedRateKey", sDb.mSelectedRateKey);
			putJsonable(obj, "selectedProperty", sDb.mSelectedProperty);
			putJsonable(obj, "selectedRate", sDb.mSelectedRate);
			putJsonable(obj, "user", sDb.mUser);
			putArrayList(obj, "travelers", sDb.mTravelers);

			IoUtils.writeStringToFile(TEST_DATA_FILE, obj.toString(), context);
		}
		catch (Exception e) {
			Log.w("Could not save db for testing", e);
		}
	}

	// Fills the Db with test data that allows you to launch into just about any Activity
	public static void loadTestData(Context context) {
		Log.i("Loading test data...");

		File file = context.getFileStreamPath(TEST_DATA_FILE);
		if (!file.exists()) {
			Log.w("Can't load test data, it doesn't exist!");
			return;
		}

		try {
			JSONObject obj = new JSONObject(IoUtils.readStringFromFile(TEST_DATA_FILE, context));

			sDb.mSearchParams = getJsonable(obj, "searchParams", SearchParams.class, sDb.mSearchParams);
			sDb.mSearchResponse = getJsonable(obj, "searchResponse", SearchResponse.class, sDb.mSearchResponse);
			sDb.mFilter = getJsonable(obj, "filter", Filter.class, sDb.mFilter);
			sDb.mInfoResponses = getMap(obj, "info", AvailabilityResponse.class, sDb.mInfoResponses);
			sDb.mAvailabilityResponses = getMap(obj, "offers", AvailabilityResponse.class, sDb.mAvailabilityResponses);
			sDb.mReviewsStatisticsResponses = getMap(obj, "reviewsStatistics", ReviewsStatisticsResponse.class,
					sDb.mReviewsStatisticsResponses);
			sDb.mReviewsResponses = getMap(obj, "reviews", ReviewsResponse.class, sDb.mReviewsResponses);
			sDb.mBillingInfo = getJsonable(obj, "billingInfo", BillingInfo.class, sDb.mBillingInfo);
			sDb.mBookingResponse = getJsonable(obj, "bookingResponse", BookingResponse.class, sDb.mBookingResponse);
			sDb.mSelectedPropertyId = obj.optString("selectedPropertyId", null);
			sDb.mSelectedRateKey = obj.optString("selectedRateKey", null);
			sDb.mSelectedProperty = getJsonable(obj, "selectedProperty", Property.class, sDb.mSelectedProperty);
			sDb.mSelectedRate = getJsonable(obj, "selectedRate", Rate.class, sDb.mSelectedRate);
			sDb.mUser = getJsonable(obj, "user", User.class, sDb.mUser);
			sDb.mTravelers = getArrayList(obj, "travelers", Traveler.class, sDb.mTravelers);
		}
		catch (Exception e) {
			Log.w("Could not load db testing", e);
		}
	}

	private static void putJsonable(JSONObject obj, String key, JSONable jsonable) throws JSONException {
		if (jsonable != null) {
			obj.putOpt(key, jsonable.toJson());
		}
	}

	private static <T extends JSONable> T getJsonable(JSONObject obj, String key, Class<T> c, T defaultVal)
			throws Exception {
		if (obj.has(key)) {
			T jsonable = c.newInstance();
			if (jsonable.fromJson(obj.getJSONObject(key))) {
				return jsonable;
			}
		}

		return defaultVal;
	}

	private static void putMap(JSONObject obj, String key, Map<String, ? extends JSONable> map) throws JSONException {
		if (map != null) {
			JSONObject mapObj = new JSONObject();
			for (String mapKey : map.keySet()) {
				mapObj.putOpt(mapKey, map.get(mapKey).toJson());
			}
			obj.putOpt(key, mapObj);
		}
	}

	@SuppressWarnings("rawtypes")
	private static <T extends JSONable> Map<String, T> getMap(JSONObject obj, String key, Class<T> c,
			Map<String, T> defaultVal) throws Exception {
		if (obj.has(key)) {
			JSONObject mapObj = obj.getJSONObject(key);
			Map<String, T> retMap = new HashMap<String, T>();

			Iterator it = mapObj.keys();
			while (it.hasNext()) {
				String mapKey = (String) it.next();
				T jsonable = c.newInstance();
				jsonable.fromJson(mapObj.getJSONObject(mapKey));
				retMap.put(mapKey, jsonable);
			}

			return retMap;
		}

		return defaultVal;
	}

	private static void putArrayList(JSONObject obj, String key, ArrayList<? extends JSONable> arrlist)
			throws JSONException {
		if (arrlist != null) {
			JSONArray jsonarr = new JSONArray(arrlist);
			obj.putOpt(key, jsonarr);
		}
	}

	private static <T extends JSONable> ArrayList<T> getArrayList(JSONObject obj, String key, Class<T> c,
			ArrayList<T> defaultVal) throws Exception {

		if (obj.has(key)) {
			ArrayList<T> retArr = new ArrayList<T>();
			JSONArray arr = obj.getJSONArray(key);

			for (int i = 0; i < arr.length(); i++) {
				T jsonable = c.newInstance();
				jsonable.fromJson(arr.getJSONObject(i));
				retArr.add(jsonable);
			}

			return retArr;
		}
		return defaultVal;
	}
}
