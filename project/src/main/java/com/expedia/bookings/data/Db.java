package com.expedia.bookings.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.model.WorkingBillingInfoManager;
import com.expedia.bookings.model.WorkingTravelerManager;
import com.expedia.bookings.utils.CalendarUtils;
import com.google.android.gms.wallet.MaskedWallet;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

/**
 * This represents an in-memory database of data for the app.
 * <p/>
 * Try to keep out information that is state data for a fragment.  For example,
 * keeping track of whether a field has been clicked is not for this.  This is
 * more for passing data between Activities.
 * <p/>
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

	// Launch hotel data - extracted from a HotelSearchResponse but cached as its own entity to keep data separate
	private List<Hotel> mLaunchListHotelData;

	// Hotel search object - represents both the parameters and
	// the returned results
	private HotelSearch mHotelSearch = new HotelSearch();

	// The filter applied to HotelSearchResponse.  Note that this HotelFilter can cause a memory leak;
	// One has to be sure to change the listeners on the HotelFilter whenever appropriate.
	private HotelFilter mFilter = new HotelFilter();

	// The billing info.  Make sure to properly clear this out when requested
	private BillingInfo mBillingInfo;

	//Is the billingInfo object dirty? This is to help the coder manage saves, and it is up to them to set it when needed
	private boolean mBillingInfoIsDirty = false;

	// Google Masked Wallet; kept separate from BillingInfo as it is more transient
	private MaskedWallet mMaskedWallet;

	// The currently logged in User profile
	private User mUser;

	// Stores routes for AirAsia POSes
	private FlightRoutes mFlightRoutes;

	// Flight search object - represents both the parameters and
	// the returned results
	private FlightSearch mFlightSearch = new FlightSearch();

	// GDE Flight histogram data
	private FlightSearchHistogramResponse mFlightSearchHistogramResponse;

	// Map of airline code --> airline name
	//
	// This data can be cached between requests, and we only need to save
	// it to disk when it becomes dirty.
	private Map<String, String> mAirlineNames = new HashMap<String, String>();
	private boolean mAirlineNamesDirty = false;

	// Trip Bucket
	private TripBucket mTripBucket = new TripBucket();

	// Flight Travelers (this is the list of travelers going on the trip, these must be valid for checking out)
	private List<Traveler> mTravelers = new ArrayList<Traveler>();
	private boolean mTravelersAreDirty = false;

	// This is the Traveler we've generated from Google Wallet data.
	// It is expected that you will generate this when you first
	// retrieve a masked wallet.
	//
	// This should be transient, as we do not want to restore this
	// if the user has changed the Wallet permissions.
	private Traveler mGoogleWalletTraveler;

	// The current traveler manager this helps us save state and edit a copy of the working traveler
	private WorkingTravelerManager mWorkingTravelerManager;

	//The working copy manager of billingInfo
	private WorkingBillingInfoManager mWorkingBillingInfoManager;

	// Abacus user bucket info
	private static AbacusResponse mAbacusResponse = new AbacusResponse();

	// To store the fullscreen average color for the ui
	private int mFullscreenAverageColor = 0x66000000;

	private String mGUID;
	//////////////////////////////////////////////////////////////////////////
	// Data access
	public static void setAbacusGuid(String guid) {
		sDb.mGUID = guid;
	}

	public static String getAbacusGuid() {
		return sDb.mGUID;
	}

	public static void setAbacusResponse(AbacusResponse abacusResponse) {
		sDb.mAbacusResponse = abacusResponse;
	}

	public static AbacusResponse getAbacusResponse() {
		return sDb.mAbacusResponse;
	}

	public static void setLaunchListHotelData(List<Hotel> launchHotelData) {
		sDb.mLaunchListHotelData = launchHotelData;
	}

	public static List<Hotel> getLaunchListHotelData() {
		return sDb.mLaunchListHotelData;
	}

	public static HotelSearch getHotelSearch() {
		return sDb.mHotelSearch;
	}

	public static void resetFilter() {
		sDb.mFilter.reset();
	}

	public static void setFilter(HotelFilter filter) {
		sDb.mFilter = filter;
	}

	public static HotelFilter getFilter() {
		return sDb.mFilter;
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

	public static boolean hasBillingInfo() {
		return sDb.mBillingInfo != null;
	}

	public static void setMaskedWallet(MaskedWallet maskedWallet) {
		sDb.mMaskedWallet = maskedWallet;
	}

	public static MaskedWallet getMaskedWallet() {
		return sDb.mMaskedWallet;
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

	public static void setFlightRoutes(FlightRoutes routes) {
		sDb.mFlightRoutes = routes;
	}

	public static FlightRoutes getFlightRoutes() {
		return sDb.mFlightRoutes;
	}

	public static void setFlightSearchHistogramResponse(FlightSearchHistogramResponse response) {
		sDb.mFlightSearchHistogramResponse = response;
	}

	public static FlightSearchHistogramResponse getFlightSearchHistogramResponse() {
		return sDb.mFlightSearchHistogramResponse;
	}

	/**
	 * WARNING: DO NOT USE UNLESS YOU KNOW WHAT YOU ARE DOING.
	 * <p/>
	 * Normally you just manipulate the FlightSearch in place,
	 * this is just for restoring state.  Do not idly use it,
	 * as you may mess up connections between objects otherwise.
	 */
	public static void setFlightSearch(FlightSearch flightSearch) {
		sDb.mFlightSearch = flightSearch;
	}

	public static FlightSearch getFlightSearch() {
		return sDb.mFlightSearch;
	}

	public static void addAirlineNames(Map<String, String> airlineNames) {
		for (String key : airlineNames.keySet()) {
			String airlineName = airlineNames.get(key);
			if (!sDb.mAirlineNames.containsKey(key)) {
				sDb.mAirlineNames.put(key, airlineName);
				sDb.mAirlineNamesDirty = true;
			}
			else {
				String oldName = sDb.mAirlineNames.get(key);
				if (oldName.startsWith("/") && !airlineName.startsWith("/")) {
					sDb.mAirlineNames.put(key, airlineName);
					sDb.mAirlineNamesDirty = true;
				}
			}
		}
	}

	public static Airline getAirline(String airlineCode) {
		// First, get the Airline from FS.db
		Airline airline = FlightStatsDbUtils.getAirline(airlineCode);

		if (airline == null) {
			airline = new Airline();
			airline.mAirlineCode = airlineCode;
		}

		// Fill in airline name if we have it
		String airlineName = sDb.mAirlineNames.get(airlineCode);
		if (!TextUtils.isEmpty(airlineName)) {
			if (airlineName.startsWith("/")) {
				airlineName = airlineName.substring(1);
			}

			airline.mAirlineName = airlineName;
		}

		return airline;
	}

	public static TripBucket getTripBucket() {
		return sDb.mTripBucket;
	}

	public static List<Traveler> getTravelers() {
		return sDb.mTravelers;
	}

	public static void setTravelers(List<Traveler> travelers) {
		sDb.mTravelers = travelers;
	}

	public static void setTravelersAreDirty(boolean dirty) {
		sDb.mTravelersAreDirty = dirty;
	}

	public static boolean getTravelersAreDirty() {
		return sDb.mTravelersAreDirty;
	}

	public static void setGoogleWalletTraveler(Traveler traveler) {
		sDb.mGoogleWalletTraveler = traveler;
	}

	public static Traveler getGoogleWalletTraveler() {
		return sDb.mGoogleWalletTraveler;
	}

	public static void setBillingInfoIsDirty(boolean dirty) {
		sDb.mBillingInfoIsDirty = dirty;
	}

	public static boolean getBillingInfoIsDirty() {
		return sDb.mBillingInfoIsDirty;
	}

	public static WorkingTravelerManager getWorkingTravelerManager() {
		if (sDb.mWorkingTravelerManager == null) {
			sDb.mWorkingTravelerManager = new WorkingTravelerManager();
		}
		return sDb.mWorkingTravelerManager;
	}

	public static WorkingBillingInfoManager getWorkingBillingInfoManager() {
		if (sDb.mWorkingBillingInfoManager == null) {
			sDb.mWorkingBillingInfoManager = new WorkingBillingInfoManager();
		}
		return sDb.mWorkingBillingInfoManager;
	}

	public static void clearGoogleWallet() {
		sDb.mMaskedWallet = null;
		sDb.mGoogleWalletTraveler = null;

		// Clear out the traveler from the Travelers array
		Iterator<Traveler> travelers = sDb.mTravelers.iterator();
		while (travelers.hasNext()) {
			Traveler traveler = travelers.next();
			if (traveler.fromGoogleWallet()) {
				travelers.remove();
			}
		}
	}

	public static void setFullscreenAverageColor(int color) {
		sDb.mFullscreenAverageColor = color;
	}

	public static int getFullscreenAverageColor() {
		return sDb.mFullscreenAverageColor;
	}

	public static void clear() {
		resetFilter();
		resetBillingInfo();
		getHotelSearch().resetSearchData();
		getHotelSearch().resetSearchParams();

		sDb.mUser = null;
		sDb.mLaunchListHotelData = null;
		sDb.mFlightRoutes = null;

		sDb.mTripBucket.clear();

		sDb.mFlightSearch.reset();
		sDb.mTravelers.clear();
	}

	//////////////////////////////////////////////////////////////////////////
	// Saving/loading data - general utilities

	/**
	 * This interface helps to standardize the disk writing tasks that take place to persist Db data.
	 * The method returns the number or characters saved to disk and it will thrown an Exception if
	 * the disk write fails.
	 */
	interface IDiskWrite {
		int doWrite(Context context) throws Exception, OutOfMemoryError;
	}

	/**
	 * This interface helps standardize loading from disk tasks that seem to be repeated throughout
	 * this class for various pieces of data.
	 */
	interface IDiskLoad {
		boolean doLoad(JSONObject json) throws Exception, OutOfMemoryError;
	}

	/**
	 * A general utility method that will spawn a new, low-priority thread to do work in the
	 * background. This is work that need to notify the UI and is also not on a schedule, so the
	 * timing isn't super important so long as it happens.
	 * @param context
	 * @param writer
	 * @param statsTag
	 */
	private static void saveDbDataToDiskInBackground(final Context context, final IDiskWrite writer, final String statsTag) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);

				long start = System.currentTimeMillis();
				Log.d("DbDisk - Saving " + statsTag + " to disk");

				synchronized (sDb) {
					try {
						int numCharWritten = writer.doWrite(context);
						Log.d("DbDisk - Saved " + statsTag + " in " + (System.currentTimeMillis() - start)
							+ " ms.  Size of data cache: "
							+ numCharWritten + " chars");
					}
					// TODO do we care enough about these exceptions to crash? the operations are not
					// TODO mission critical for the app, so methinks not.
					catch (Exception e) {
						Log.e("DbDisk - Exception saving " + statsTag, e);
					}
					catch (OutOfMemoryError e) {
						Log.e("DbDisk - Ran out of memory trying to save " + statsTag, e);
					}
				}
			}
		})).start();
	}

	/**
	 * The companion of saveDbDataToDiskInBackground
	 * @param context
	 * @param loader
	 * @param fileName
	 * @param statsTag
	 * @return
	 */
	private static boolean loadFromDisk(Context context, IDiskLoad loader, String fileName, String statsTag) {
		Log.d("DbDisk - Trying to load cached " + statsTag + " from disk.");

		long start = System.currentTimeMillis();

		File file = context.getFileStreamPath(fileName);
		if (!file.exists()) {
			Log.d("DbDisk - There is no cached " + statsTag + " on disk to load.");
			return false;
		}

		try {
			JSONObject obj = new JSONObject(IoUtils.readStringFromFile(fileName, context));
			boolean loadSuccess = loader.doLoad(obj);

			Log.d("DbDisk - Loaded cached " + statsTag + " in " + (System.currentTimeMillis() - start) + " ms");

			return loadSuccess;
		}
		catch (Exception e) {
			Log.w("DbDisk - Could not load cached " + statsTag, e);
			return false;
		}
		catch (OutOfMemoryError e) {
			Log.w("DbDisk - Out of Memory loading " + statsTag, e);
			return false;
		}
	}

	private static boolean deleteFromDisk(Context context, String fileName, String statsTag) {
		File file = context.getFileStreamPath(fileName);
		if (!file.exists()) {
			return true;
		}
		else {
			Log.i("Deleting cached " + statsTag + " data.");
			return file.delete();
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// Trip Bucket

	private static final String SAVED_TRIP_BUCKET_FILE_NAME = "trip-bucket.db";

	public static void saveTripBucket(Context context) {
		saveDbDataToDiskInBackground(context, new IDiskWrite() {
			@Override
			public int doWrite(Context context) throws Exception, OutOfMemoryError {
				JSONObject obj = new JSONObject();
				putJsonable(obj, "tripBucket", sDb.mTripBucket);
				String json = obj.toString();
				IoUtils.writeStringToFile(SAVED_TRIP_BUCKET_FILE_NAME, json, context);
				return json.length();
			}
		}, "TripBucket");
	}

	public static boolean loadTripBucket(Context context) {
		return loadFromDisk(context, new IDiskLoad() {
			@Override
			public boolean doLoad(JSONObject json) throws Exception, OutOfMemoryError {
				if (json.has("tripBucket")) {
					sDb.mTripBucket = getJsonable(json, "tripBucket", TripBucket.class, sDb.mTripBucket);
				}
				return true;
			}
		}, SAVED_TRIP_BUCKET_FILE_NAME, "TripBucket");
	}

	public static boolean deleteTripBucket(Context context) {
		return deleteFromDisk(context, SAVED_TRIP_BUCKET_FILE_NAME, "TripBucket");
	}

	//////////////////////////////////////////////////////////////////////////
	// Saving/loading hotel data

	private static final String HOTEL_SEARCH_START_TIME = "hotelsSearchTimestampSetting";
	private static final String HOTEL_SEARCH_DATA_FILE = "hotels-data.db";

	public static void saveHotelSearchTimestamp(final Context context) {
		// Save the timestamp of the original search to know when the results become invalid
		long startTimeMs = System.currentTimeMillis();
		SettingUtils.save(context, HOTEL_SEARCH_START_TIME, startTimeMs);
	}

	public static void kickOffBackgroundHotelSearchSave(final Context context) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
				Db.saveHotelSearchToDisk(context);
			}
		})).start();
	}

	private static void saveHotelSearchToDisk(Context context) {
		synchronized (sDb) {
			Log.i("Saving hotel data to disk.");

			long start = System.currentTimeMillis();
			try {
				String hotelSearchString = sDb.mHotelSearch.toJson().toString();
				IoUtils.writeStringToFile(HOTEL_SEARCH_DATA_FILE, hotelSearchString, context);

				Log.d("Saved hotel data cache in " + (System.currentTimeMillis() - start)
					+ " ms.  Size of data cache: "
					+ hotelSearchString.length() + " chars");
			}
			catch (IOException e) {
				Log.w("Failed to write hotel data to disk", e);
			}
			catch (OutOfMemoryError e) {
				Log.w("Failed to write hotel data to disk, ran out of memory", e);
			}

		}
	}

	public static boolean loadHotelSearchFromDisk(Context context) {
		return loadHotelSearchFromDisk(context, false);
	}

	public static boolean loadHotelSearchFromDisk(Context context, boolean bypassTimeout) {
		synchronized (sDb) {
			long start = System.currentTimeMillis();

			File file = context.getFileStreamPath(HOTEL_SEARCH_DATA_FILE);
			if (!file.exists()) {
				Log.d("There is no cached hotel data to load!");
				return false;
			}

			long searchTimestamp = SettingUtils.get(context, HOTEL_SEARCH_START_TIME, (long) 0);
			if (bypassTimeout) {
				Log.i("We don't care about hotel search timing out!!");
				if (BuildConfig.RELEASE) {
					throw new RuntimeException("Bypassing hotel search timeout with an RC. bad!");
				}
			}
			else if (CalendarUtils.isExpired(searchTimestamp, HotelSearch.SEARCH_DATA_TIMEOUT)) {
				Log.d("There is cached hotel data but it has expired, not loading.");
				Db.deleteHotelSearchData(context);
				return false;
			}

			try {
				JSONObject jsonObject = new JSONObject(IoUtils.readStringFromFile(HOTEL_SEARCH_DATA_FILE, context));
				HotelSearch hotelSearch = new HotelSearch();
				hotelSearch.fromJson(jsonObject);
				sDb.mHotelSearch = hotelSearch;

				Log.d("Loaded cached hotel data in " + (System.currentTimeMillis() - start) + " ms");
				return true;
			}
			catch (Exception e) {
				Log.w("Unable to load hotel search from disk", e);
				return false;
			}
		}
	}

	public static boolean deleteHotelSearchData(Context context) {
		SettingUtils.remove(context, HOTEL_SEARCH_START_TIME);

		File file = context.getFileStreamPath(HOTEL_SEARCH_DATA_FILE);
		if (!file.exists()) {
			return true;
		}
		else {
			Log.i("Deleting cached hotel data.");
			return file.delete();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Saving/loading flight data

	private static final String SAVED_FLIGHT_DATA_FILE = "flights-data.db";
	private static final String SAVED_AIRLINE_DATA_FILE = "airlines-data.db";

	private static final String FLIGHT_SEARCH_PARAMS_SETTING = "flightSearchParamsSetting";

	public static void saveFlightSearchParamsToDisk(Context context) {
		Log.i("Saving flight search params to disk.");
		SettingUtils.save(context, FLIGHT_SEARCH_PARAMS_SETTING, getFlightSearch().getSearchParams().toJson()
			.toString());
	}

	public static void loadFlightSearchParamsFromDisk(Context context) {
		String searchParamsJson = SettingUtils.get(context, FLIGHT_SEARCH_PARAMS_SETTING, null);
		if (!TextUtils.isEmpty(searchParamsJson)) {
			try {
				Log.i("Restoring flight search params from disk...");
				FlightSearchParams params = new FlightSearchParams();
				params.fromJson(new JSONObject(searchParamsJson));
				params.ensureValidDates();
				getFlightSearch().setSearchParams(params);
			}
			catch (JSONException e) {
				Log.w("Could not restore flight search params from disk", e);
			}
		}
	}

	public static void kickOffBackgroundFlightSearchSave(final Context context) {
		// Kick off a search to cache results to disk, in case app is killed
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
				Db.saveFlightDataCache(context);
				Db.saveAirlineDataCache(context);
			}
		})).start();
	}

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

				String json = obj.toString();
				IoUtils.writeStringToFile(SAVED_FLIGHT_DATA_FILE, json, context);

				Log.d("Saved flight data cache in " + (System.currentTimeMillis() - start)
					+ " ms.  Size of data cache: "
					+ json.length() + " chars");

				return true;
			}
			catch (Exception e) {
				// It's not a severe issue if this all fails - just sub-optimal
				Log.w("Failed to save flight data", e);
				return false;
			}
			catch (OutOfMemoryError err) {
				Log.e("Ran out of memory trying to save flight data cache", err);
				return false;
			}
		}
	}

	/**
	 * MAKE SURE TO CALL THIS IN A NON-UI THREAD
	 */
	public static boolean saveAirlineDataCache(Context context) {
		if (!sDb.mAirlineNamesDirty) {
			Log.d("Would have saved airline data, but it's up to date.");
			return true;
		}

		synchronized (sDb) {
			Log.d("Saving airline data cache...");

			long start = System.currentTimeMillis();
			try {
				JSONObject obj = new JSONObject();
				JSONUtils.putStringMap(obj, "airlineMap", sDb.mAirlineNames);
				String json = obj.toString();
				IoUtils.writeStringToFile(SAVED_AIRLINE_DATA_FILE, json, context);

				Log.d("Saved airline data cache in " + (System.currentTimeMillis() - start)
					+ " ms.  Size of data cache: " + json.length() + " chars");

				sDb.mAirlineNamesDirty = false;

				return true;
			}
			catch (Exception e) {
				// It's not a severe issue if this all fails
				Log.w("Failed to save airline data", e);
				return false;
			}
			catch (OutOfMemoryError err) {
				Log.e("Ran out of memory trying to save airline data cache", err);
				throw new RuntimeException(err);
			}
		}
	}

	public static boolean loadCachedFlightData(Context context) {
		Log.d("Trying to load cached flight data...");

		long start = System.currentTimeMillis();

		File file = context.getFileStreamPath(SAVED_FLIGHT_DATA_FILE);
		if (!file.exists()) {
			Log.d("There is no cached flight data to load!");
			return false;
		}

		try {
			JSONObject obj = new JSONObject(IoUtils.readStringFromFile(SAVED_FLIGHT_DATA_FILE, context));

			if (obj.has("flightSearch")) {
				sDb.mFlightSearch = JSONUtils.getJSONable(obj, "flightSearch", FlightSearch.class);
			}

			JSONObject obj2 = new JSONObject(IoUtils.readStringFromFile(SAVED_FLIGHT_DATA_FILE, context));
			if (obj2.has("airlineMap")) {
				sDb.mAirlineNames = JSONUtils.getStringMap(obj2, "airlineMap");
				sDb.mAirlineNamesDirty = false;
			}

			Log.d("Loaded cached flight data in " + (System.currentTimeMillis() - start) + " ms");

			return true;
		}
		catch (Exception e) {
			Log.w("Could not load cached flight data", e);
			return false;
		}
	}

	public static boolean deleteCachedFlightData(Context context) {
		SettingUtils.remove(context, FLIGHT_SEARCH_PARAMS_SETTING);

		File file = context.getFileStreamPath(SAVED_FLIGHT_DATA_FILE);
		if (!file.exists()) {
			return true;
		}
		else {
			Log.i("Deleting cached flight data.");
			return file.delete();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Saving/loading flight route data

	private static final String SAVED_FLIGHT_ROUTES_DATA_FILE = "flight-routes.db";

	public static void kickOffBackgroundFlightRouteSave(final Context context) {
		// Kick off a search to cache results to disk, in case app is killed
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
				Db.saveFlightRouteCache(context);
			}
		})).start();
	}

	public static boolean saveFlightRouteCache(Context context) {
		synchronized (sDb) {
			Log.d("Saving flight route cache...");

			try {
				long start = System.currentTimeMillis();
				JSONObject obj = new JSONObject();
				JSONUtils.putJSONable(obj, "flightRoutes", sDb.mFlightRoutes);
				String json = obj.toString();
				IoUtils.writeStringToFile(SAVED_FLIGHT_ROUTES_DATA_FILE, json, context);
				Log.d("Saved cached flight routes in " + (System.currentTimeMillis() - start) + " ms");
				return true;
			}
			catch (Exception e) {
				// It's not a severe issue if this all fails
				Log.w("Failed to save flight route data", e);
				return false;
			}
		}
	}

	public static boolean loadCachedFlightRoutes(Context context) {
		Log.d("Trying to load cached flight routes...");

		File file = context.getFileStreamPath(SAVED_FLIGHT_ROUTES_DATA_FILE);
		if (!file.exists()) {
			Log.d("There is no cached flight routes to load!");
			return false;
		}

		try {
			long start = System.currentTimeMillis();
			JSONObject obj = new JSONObject(IoUtils.readStringFromFile(SAVED_FLIGHT_ROUTES_DATA_FILE, context));
			sDb.mFlightRoutes = JSONUtils.getJSONable(obj, "flightRoutes", FlightRoutes.class);
			Log.d("Loaded cached flight routes in " + (System.currentTimeMillis() - start) + " ms");
			return true;
		}
		catch (Exception e) {
			Log.w("Could not load cached flight routes", e);
			return false;
		}
	}

	public static boolean deleteCachedFlightRoutes(Context context) {
		sDb.mFlightRoutes = null;

		File file = context.getFileStreamPath(SAVED_FLIGHT_ROUTES_DATA_FILE);
		if (!file.exists()) {
			return true;
		}
		else {
			Log.i("Deleting cached flight routes.");
			return file.delete();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Saving/loading traveler data

	private static final String SAVED_TRAVELER_DATA_FILE = "travelers.db";
	private static final String SAVED_TRAVELERS_TAG = "SAVED_TRAVELERS_TAG";

	public static void kickOffBackgroundTravelerSave(final Context context) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
				Db.saveTravelers(context);
			}
		})).start();
	}

	public static boolean saveTravelers(Context context) {
		synchronized (sDb) {
			Log.d("Saving traveler data cache...");
			long start = System.currentTimeMillis();
			try {
				JSONObject obj = new JSONObject();

				List<Traveler> travelersList = new ArrayList<Traveler>(Db.getTravelers());

				// There is only ever going to be a single Google Wallet traveler;
				// if the user has edited *anything*, then save it (and make it a non-
				// GWallet user)
				Traveler gwTraveler = Db.getGoogleWalletTraveler();
				if (gwTraveler != null) {
					Iterator<Traveler> travelers = travelersList.iterator();
					while (travelers.hasNext()) {
						Traveler traveler = travelers.next();
						if (traveler.fromGoogleWallet()) {
							if (traveler.compareTo(gwTraveler) == 0) {
								travelers.remove();
							}
							else {
								traveler.setFromGoogleWallet(false);
							}

							break;
						}
					}
				}

				JSONUtils.putJSONableList(obj, SAVED_TRAVELERS_TAG, travelersList);

				String json = obj.toString();
				IoUtils.writeStringToFile(SAVED_TRAVELER_DATA_FILE, json, context);

				Log.d("Saved traveler data cache in " + (System.currentTimeMillis() - start)
					+ " ms.  Size of data cache: "
					+ json.length() + " chars");

				Db.setTravelersAreDirty(false);
				return true;
			}
			catch (Exception e) {
				// It's not a severe issue if this all fails - just
				Log.w("Failed to save traveler data", e);
				return false;
			}
			catch (OutOfMemoryError err) {
				Log.e("Ran out of memory trying to save traveler data cache", err);
				throw new RuntimeException(err);
			}
		}
	}

	public static boolean loadTravelers(Context context) {
		long start = System.currentTimeMillis();

		File file = context.getFileStreamPath(SAVED_TRAVELER_DATA_FILE);
		if (!file.exists()) {
			List<Traveler> travelers = new ArrayList<Traveler>();
			travelers.add(new Traveler());
			Db.setTravelers(travelers);
			return false;
		}

		try {
			JSONObject obj = new JSONObject(IoUtils.readStringFromFile(SAVED_TRAVELER_DATA_FILE, context));
			if (obj.has(SAVED_TRAVELERS_TAG)) {
				List<Traveler> travelers = JSONUtils.getJSONableList(obj, SAVED_TRAVELERS_TAG, Traveler.class);
				Db.setTravelers(travelers);
			}

			Log.d("Loaded cached traveler data in " + (System.currentTimeMillis() - start) + " ms");

			return true;
		}
		catch (Exception e) {
			Log.e("Exception loading traveler data", e);
			return false;
		}
	}

	public static boolean deleteTravelers(Context context) {
		File file = context.getFileStreamPath(SAVED_TRAVELER_DATA_FILE);
		if (!file.exists()) {
			return true;
		}
		else {
			return file.delete();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Saving/loading the BillingInfo object

	public static void kickOffBackgroundBillingInfoSave(final Context context) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
				if (Db.getBillingInfo() != null && context != null) {
					BillingInfo tempBi = new BillingInfo(Db.getBillingInfo());
					tempBi.save(context);
				}
			}
		})).start();
	}

	public static boolean loadBillingInfo(Context context) {
		if (sDb.mBillingInfo == null) {
			sDb.mBillingInfo = new BillingInfo();
			return sDb.mBillingInfo.load(context);
		}
		else {
			return true;
		}
	}

	public static void deleteBillingInfo(Context context) {
		if (sDb.mBillingInfo != null) {
			sDb.mBillingInfo.delete(context);
		}
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

	// Do not let people use these methods in non-debug builds
	private static void safetyFirst(Context context) {
		if (BuildConfig.RELEASE) {
			throw new RuntimeException(
				"This debug method should NEVER be called in a release build"
					+ " (you should probably even remove it from the codebase)");
		}
	}

	/**
	 * Call in onCreate(); will either save (if we are in the middle of the app)
	 * or will reload (if you launched to this specific Activity)
	 * <p/>
	 * Should be used for TESTING ONLY.  Do not check in any calls to this!
	 */
	public static void saveOrLoadDbForTesting(Activity activity) {
		safetyFirst(activity);

		Intent intent = activity.getIntent();
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			loadTestData(activity);
		}
		else {
			saveDbForTesting(activity);
		}
	}

	private static void saveDbForTesting(Context context) {
		safetyFirst(context);

		Log.i("Saving test data...");

		JSONObject obj = new JSONObject();

		try {
			putJsonable(obj, "hotelSearch", sDb.mHotelSearch);
			putJsonable(obj, "filter", sDb.mFilter);
			putJsonable(obj, "billingInfo", sDb.mBillingInfo);
			putJsonable(obj, "flightSearch", sDb.mFlightSearch);
			putJsonable(obj, "tripBucket", sDb.mTripBucket);
			JSONUtils.putStringMap(obj, "airlines", sDb.mAirlineNames);
			putJsonable(obj, "user", sDb.mUser);
			putList(obj, "travelers", sDb.mTravelers);

			IoUtils.writeStringToFile(TEST_DATA_FILE, obj.toString(), context);
		}
		catch (OutOfMemoryError e) {
			Log.w("Could not save db for testing", e);
		}
		catch (Exception e) {
			Log.w("Could not save db for testing", e);
		}
	}

	// Fills the Db with test data that allows you to launch into just about any Activity
	private static void loadTestData(Context context) {
		safetyFirst(context);

		Log.i("Loading test data...");

		File file = context.getFileStreamPath(TEST_DATA_FILE);
		if (!file.exists()) {
			Log.w("Can't load test data, it doesn't exist!");
			return;
		}

		try {
			JSONObject obj = new JSONObject(IoUtils.readStringFromFile(TEST_DATA_FILE, context));

			sDb.mHotelSearch = getJsonable(obj, "hotelSearch", HotelSearch.class, sDb.mHotelSearch);
			sDb.mFilter = getJsonable(obj, "filter", HotelFilter.class, sDb.mFilter);
			sDb.mBillingInfo = getJsonable(obj, "billingInfo", BillingInfo.class, sDb.mBillingInfo);
			sDb.mFlightSearch = getJsonable(obj, "flightSearch", FlightSearch.class, sDb.mFlightSearch);
			sDb.mTripBucket = getJsonable(obj, "tripBucket", TripBucket.class, sDb.mTripBucket);
			sDb.mAirlineNames = JSONUtils.getStringMap(obj, "airlines");
			sDb.mUser = getJsonable(obj, "user", User.class, sDb.mUser);
			sDb.mTravelers = getList(obj, "travelers", Traveler.class, sDb.mTravelers);
		}
		catch (Exception e) {
			Log.w("Could not load db testing", e);
		}
	}

	private static void putJsonable(JSONObject obj, String key, JSONable jsonable) throws JSONException {
		JSONUtils.putJSONable(obj, key, jsonable);
	}

	private static <T extends JSONable> T getJsonable(JSONObject obj, String key, Class<T> c, T defaultVal)
		throws Exception {
		T t = JSONUtils.getJSONable(obj, key, c);
		if (t != null) {
			return t;
		}
		return defaultVal;
	}

	private static void putMap(JSONObject obj, String key, Map<String, ? extends JSONable> map) throws JSONException {
		JSONUtils.putJSONableStringMap(obj, key, map);
	}

	@SuppressWarnings("rawtypes")
	private static <T extends JSONable> Map<String, T> getMap(JSONObject obj, String key, Class<T> c,
															  Map<String, T> defaultVal) throws Exception {
		return JSONUtils.getJSONableStringMap(obj, key, c, defaultVal);
	}

	private static void putList(JSONObject obj, String key, List<? extends JSONable> arrlist) throws JSONException {
		JSONUtils.putJSONableList(obj, key, arrlist);
	}

	private static <T extends JSONable> List<T> getList(JSONObject obj, String key, Class<T> c, List<T> defaultVal) {
		List<T> list = JSONUtils.getJSONableList(obj, key, c);
		if (list != null) {
			return list;
		}
		return defaultVal;
	}
}
