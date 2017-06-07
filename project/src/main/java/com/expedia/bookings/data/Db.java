package com.expedia.bookings.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Pair;

import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.data.packages.PackageSearchResponse;
import com.expedia.bookings.data.trips.TripBucket;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.model.WorkingBillingInfoManager;
import com.expedia.bookings.model.WorkingTravelerManager;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;
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

	//It will be set only if user chose 'Save' on filling in new card details. If he chose 'No Thanks', this won't be set then.
	private BillingInfo temporarilySavedCard ;

	// The currently logged in User profile
	private User mUser;

	// Stores routes for AirAsia POSes
	private FlightRoutes mFlightRoutes;

	// Flight search object - represents both the parameters and
	// the returned results
	private FlightSearch mFlightSearch = new FlightSearch();

	// Map of airline code --> airline name
	//
	// This data can be cached between requests, and we only need to save
	// it to disk when it becomes dirty.
	private Map<String, String> mAirlineNames = new HashMap<String, String>();

	// Trip Bucket
	private TripBucket mTripBucket = new TripBucket();

	// Flight Travelers (this is the list of travelers going on the trip, these must be valid for checking out)
	private ArrayList<Traveler> mTravelers = new ArrayList();

	// The current traveler manager this helps us save state and edit a copy of the working traveler
	private WorkingTravelerManager mWorkingTravelerManager;

	//The working copy manager of billingInfo
	private WorkingBillingInfoManager mWorkingBillingInfoManager;

	// Abacus user bucket info
	private static AbacusResponse mAbacusResponse = new AbacusResponse();

	// To store the fullscreen average color for the ui
	private int mFullscreenAverageColor = 0x66000000;

	private String mAbacusGuid;

	private PackageSearchParams mPackageParams;
	private com.expedia.bookings.data.flights.FlightSearchParams mFlightSearchParams;
	private PackageSearchResponse mPackageResponse;
	private Hotel mPackageSelectedHotel;
	private HotelOffersResponse.HotelRoomResponse mPackageSelectedRoom;
	private FlightLeg mPackageSelectedOutboundFlight;

	//Package outbound and inbound flight pair
	//Save inbound flight in this pair, to avoid stale inbound info if outbound is changed
	private Pair<FlightLeg, FlightLeg> mPackageFlightBundle;

	private SignInTypeEnum signInTypeEnum = null;

	public enum SignInTypeEnum {
		BRAND_SIGN_IN,
		FACEBOOK_SIGN_IN
	}

	public static SignInTypeEnum getSignInType() {
		return sDb.signInTypeEnum;
	}

	public static void setSignInType(SignInTypeEnum signInResultEnum) {
		sDb.signInTypeEnum = signInResultEnum;
	}

	//////////////////////////////////////////////////////////////////////////
	// Data access

	public static PackageSearchParams getPackageParams() {
		return sDb.mPackageParams;
	}

	public static com.expedia.bookings.data.flights.FlightSearchParams getFlightSearchParams() {
		return sDb.mFlightSearchParams;
	}

	public static void setPackageSelectedHotel(Hotel packageSelectedHotel, HotelOffersResponse.HotelRoomResponse packageSelectedRoom) {
		sDb.mPackageSelectedHotel = packageSelectedHotel;
		sDb.mPackageSelectedRoom = packageSelectedRoom;
	}

	public static Hotel getPackageSelectedHotel() {
		return sDb.mPackageSelectedHotel;
	}

	public static void clearPackageHotelSelection () {
		sDb.mPackageSelectedHotel = null;
		sDb.mPackageSelectedRoom = null;
	}

	public static void clearPackageHotelRoomSelection() {
		if (sDb.mPackageSelectedRoom != null) {
			sDb.mPackageSelectedRoom.ratePlanCode = null;
			sDb.mPackageSelectedRoom.roomTypeCode = null;
		}
	}

	public static void clearPackageFlightSelection() {
		sDb.mPackageSelectedOutboundFlight = null;
		sDb.mPackageFlightBundle = null;
	}

	public static void clearPackageSelection() {
		clearPackageHotelSelection();
		clearPackageFlightSelection();
	}

	public static HotelOffersResponse.HotelRoomResponse getPackageSelectedRoom() {
		return sDb.mPackageSelectedRoom;
	}

	public static FlightLeg getPackageSelectedOutboundFlight() {
		return sDb.mPackageSelectedOutboundFlight;
	}

	public static void setPackageSelectedOutboundFlight(FlightLeg mPackageSelectedFlight) {
		sDb.mPackageSelectedOutboundFlight = mPackageSelectedFlight;
	}

	public static Pair<FlightLeg, FlightLeg> getPackageFlightBundle() {
		return sDb.mPackageFlightBundle;
	}

	public static void setPackageFlightBundle(FlightLeg outbound, FlightLeg inbound) {
		sDb.mPackageFlightBundle = new Pair<>(outbound, inbound);
	}

	public static void setPackageParams(PackageSearchParams params) {
		sDb.mPackageParams = params;
	}

	public static void setFlightSearchParams(com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams) {
		sDb.mPackageParams = null;
		sDb.mFlightSearchParams = flightSearchParams;
	}

	public static PackageSearchResponse getPackageResponse() {
		return sDb.mPackageResponse;
	}

	public static void setPackageResponse(PackageSearchResponse hotelPackage) {
		sDb.mPackageResponse = hotelPackage;
	}

	public static void setAbacusGuid(String guid) {
		sDb.mAbacusGuid = guid;
	}

	public static String getAbacusGuid() {
		return sDb.mAbacusGuid;
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

	public static HotelFilter getFilter() {
		return sDb.mFilter;
	}

	public static void resetBillingInfo() {
		sDb.mBillingInfo = new BillingInfo();
		sDb.temporarilySavedCard = null;
	}

	public static void clearTemporaryCard() {
		sDb.temporarilySavedCard = null;
	}

	public static void setBillingInfo(BillingInfo billingInfo) {
		sDb.mBillingInfo = billingInfo;
	}

	public static BillingInfo getBillingInfo() {
		if (sDb.mBillingInfo == null) {
			sDb.mBillingInfo = new BillingInfo();
		}

		return sDb.mBillingInfo;
	}

	public static BillingInfo getTemporarilySavedCard() {
		return sDb.temporarilySavedCard;
	}

	public static void setTemporarilySavedCard(BillingInfo temporarilySavedCard) {
		sDb.temporarilySavedCard = temporarilySavedCard;
	}
	public static boolean hasBillingInfo() {
		return sDb.mBillingInfo != null;
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

	public static FlightSearch getFlightSearch() {
		return sDb.mFlightSearch;
	}

	public static void addAirlineNames(Map<String, String> airlineNames) {
		for (String key : airlineNames.keySet()) {
			String airlineName = airlineNames.get(key);
			if (!sDb.mAirlineNames.containsKey(key)) {
				sDb.mAirlineNames.put(key, airlineName);
			}
			else {
				String oldName = sDb.mAirlineNames.get(key);
				if (oldName.startsWith("/") && !airlineName.startsWith("/")) {
					sDb.mAirlineNames.put(key, airlineName);
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
		sDb.mTravelers = new ArrayList<>(travelers);
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

		if (Db.getWorkingBillingInfoManager() != null) {
			Db.getWorkingBillingInfoManager().clearWorkingBillingInfo();
		}

		if (Db.getWorkingTravelerManager() != null) {
			Db.getWorkingTravelerManager().clearWorkingTraveler();
		}
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
		boolean hasTrip = loadFromDisk(context, new IDiskLoad() {
			@Override
			public boolean doLoad(JSONObject json) throws Exception, OutOfMemoryError {
				if (json.has("tripBucket")) {
					sDb.mTripBucket = getJsonable(json, "tripBucket", TripBucket.class, sDb.mTripBucket);
				}
				return true;
			}
		}, SAVED_TRIP_BUCKET_FILE_NAME, "TripBucket");
		boolean isAirAttachQualified = hasTrip ? sDb.mTripBucket.isUserAirAttachQualified() : false;
		return hasTrip;
	}

	public static boolean deleteTripBucket(Context context) {
		return deleteFromDisk(context, SAVED_TRIP_BUCKET_FILE_NAME, "TripBucket");
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
}
