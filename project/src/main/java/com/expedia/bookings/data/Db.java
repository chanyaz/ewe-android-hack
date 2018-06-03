package com.expedia.bookings.data;

import android.content.Context;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.multiitem.BundleSearchResponse;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.data.trips.TripBucket;
import com.expedia.bookings.model.WorkingBillingInfoManager;
import com.expedia.bookings.model.WorkingTravelerManager;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;
import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

	public static final Db sharedInstance = new Db();

	private Db() {
		// Cannot be instantiated
	}

	//////////////////////////////////////////////////////////////////////////
	// Stored data

	// The billing info.  Make sure to properly clear this out when requested
	private BillingInfo mBillingInfo;

	//It will be set only if user chose 'Save' on filling in new card details. If he chose 'No Thanks', this won't be set then.
	private BillingInfo temporarilySavedCard ;

	// Stores routes for AirAsia POSes
	private FlightRoutes mFlightRoutes;

	// Flight search object - represents both the parameters and
	// the returned results
	private FlightSearch mFlightSearch = new FlightSearch();

	// Trip Bucket
	private TripBucket mTripBucket = new TripBucket();

	// Flight Travelers (this is the list of travelers going on the trip, these must be valid for checking out)
	private ArrayList<Traveler> mTravelers = new ArrayList<>();

	// The current traveler manager this helps us save state and edit a copy of the working traveler
	private WorkingTravelerManager mWorkingTravelerManager;

	//The working copy manager of billingInfo
	private WorkingBillingInfoManager mWorkingBillingInfoManager;

	// Abacus user bucket info
	private AbacusResponse mAbacusResponse = new AbacusResponse();

	private String mAbacusGuid;

	private PackageSearchParams mPackageParams;
	private com.expedia.bookings.data.flights.FlightSearchParams mFlightSearchParams;
	private BundleSearchResponse mPackageResponse;
	private BundleSearchResponse mUnfilteredResponse;
	private Hotel mPackageSelectedHotel;
	private HotelOffersResponse.HotelRoomResponse mPackageSelectedRoom;
	private FlightLeg mPackageSelectedOutboundFlight;
	private BundleSearchResponse cachedPackageResponse;

	//Package outbound and inbound flight pair
	//Save inbound flight in this pair, to avoid stale inbound info if outbound is changed
	private Pair<FlightLeg, FlightLeg> mPackageFlightBundle;

	private SignInTypeEnum signInTypeEnum = null;

	public boolean deleteCachedFlightRoutes(Context context) {
		mFlightRoutes = null;

		File file = context.getFileStreamPath(SAVED_FLIGHT_ROUTES_DATA_FILE);
		if (!file.exists()) {
			return true;
		}
		else {
			Log.i("Deleting cached flight routes.");
			return file.delete();
		}
	}

	public boolean loadCachedFlightRoutes(Context context) {
		Log.d("Trying to load cached flight routes...");

		File file = context.getFileStreamPath(SAVED_FLIGHT_ROUTES_DATA_FILE);
		if (!file.exists()) {
			Log.d("There is no cached flight routes to load!");
			return false;
		}

		try {
			long start = System.currentTimeMillis();
			JSONObject obj = new JSONObject(IoUtils.readStringFromFile(SAVED_FLIGHT_ROUTES_DATA_FILE, context));
			mFlightRoutes = JSONUtils.getJSONable(obj, "flightRoutes", FlightRoutes.class);
			Log.d("Loaded cached flight routes in " + (System.currentTimeMillis() - start) + " ms");
			return true;
		}
		catch (Exception e) {
			Log.w("Could not load cached flight routes", e);
			return false;
		}
	}

	public void kickOffBackgroundFlightRouteSave(final Context context) {
		// Kick off a search to cache results to disk, in case app is killed
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
				saveFlightRouteCache(context);
			}
		})).start();
	}

	public FlightRoutes getFlightRoutes() {
		return mFlightRoutes;
	}

	public void setFlightRoutes(FlightRoutes routes) {
		mFlightRoutes = routes;
	}

	public void setTravelers(List<Traveler> travelers) {
		mTravelers = new ArrayList<>(travelers);
	}

	public void resetTravelers() {
		List<Traveler> travelers = getTravelers();
		int numOfTravelers = travelers.size();
		travelers.clear();

		for (int i = 0; i < numOfTravelers; i++) {
			travelers.add(i, new Traveler());
		}
	}

	public List<Traveler> getTravelers() {
		return mTravelers;
	}

	public BillingInfo getTemporarilySavedCard() {
		return temporarilySavedCard;
	}

	public void setAbacusResponse(AbacusResponse abacusResponse) {
		mAbacusResponse = abacusResponse;
	}

	public AbacusResponse getAbacusResponse() {
		return mAbacusResponse;
	}

	public String getAbacusGuid() {
		return mAbacusGuid;
	}

	public void setAbacusGuid(String guid) {
		mAbacusGuid = guid;
	}

	private boolean saveFlightRouteCache(Context context) {
		synchronized (this) {
			Log.d("Saving flight route cache...");

			try {
				long start = System.currentTimeMillis();
				JSONObject obj = new JSONObject();
				JSONUtils.putJSONable(obj, "flightRoutes", mFlightRoutes);
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

	private void saveDbDataToDiskInBackgroundOnLowPriorityThread(final Context context, final IDiskWrite writer, final String statsTag) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);

				long start = System.currentTimeMillis();
				Log.d("DbDisk - Saving " + statsTag + " to disk");

				synchronized (this) {
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

	public void clear() {
		resetBillingInfo();

		mFlightRoutes = null;

		mTripBucket.clear();

		mFlightSearch.reset();
		mTravelers.clear();

		instanceGetWorkingBillingInfoManager().clearWorkingBillingInfo();
		instanceGetWorkingTravelerManager().clearWorkingTraveler();
	}

	@NonNull
	private WorkingTravelerManager instanceGetWorkingTravelerManager() {
		if (mWorkingTravelerManager == null) {
			mWorkingTravelerManager = new WorkingTravelerManager();
		}
		return mWorkingTravelerManager;
	}

	@NonNull
	private WorkingBillingInfoManager instanceGetWorkingBillingInfoManager() {
		if (mWorkingBillingInfoManager == null) {
			mWorkingBillingInfoManager = new WorkingBillingInfoManager();
		}
		return mWorkingBillingInfoManager;
	}

	public void resetBillingInfo() {
		mBillingInfo = new BillingInfo();
		temporarilySavedCard = null;
	}

	public void setSignInType(SignInTypeEnum signInResultEnum) {
		signInTypeEnum = signInResultEnum;
	}

	public SignInTypeEnum getSignInType() {
		return signInTypeEnum;
	}

	public enum SignInTypeEnum {
		BRAND_SIGN_IN,
		FACEBOOK_SIGN_IN
	}

	//////////////////////////////////////////////////////////////////////////
	// Data access

	public PackageSearchParams getPackageParams() {
		return mPackageParams;
	}

	public static com.expedia.bookings.data.flights.FlightSearchParams getFlightSearchParams() {
		return sharedInstance.mFlightSearchParams;
	}

	public static void setPackageSelectedHotel(Hotel packageSelectedHotel, HotelOffersResponse.HotelRoomResponse packageSelectedRoom) {
		sharedInstance.mPackageSelectedHotel = packageSelectedHotel;
		sharedInstance.mPackageSelectedRoom = packageSelectedRoom;
	}

	public static Hotel getPackageSelectedHotel() {
		return sharedInstance.mPackageSelectedHotel;
	}

	private void clearPackageHotelSelection() {
		mPackageSelectedHotel = null;
		mPackageSelectedRoom = null;
	}

	public void clearPackageHotelRoomSelection() {
		if (mPackageSelectedRoom != null) {
			mPackageSelectedRoom.ratePlanCode = null;
			mPackageSelectedRoom.roomTypeCode = null;
		}
	}

	public void clearPackageFlightSelection() {
		mPackageSelectedOutboundFlight = null;
		mPackageFlightBundle = null;
	}

	public void clearPackageSelection() {
		clearPackageHotelSelection();
		clearPackageFlightSelection();
	}

	public HotelOffersResponse.HotelRoomResponse getPackageSelectedRoom() {
		return sharedInstance.mPackageSelectedRoom;
	}

	public FlightLeg getPackageSelectedOutboundFlight() {
		return mPackageSelectedOutboundFlight;
	}

	public static void setPackageSelectedOutboundFlight(FlightLeg mPackageSelectedFlight) {
		sharedInstance.mPackageSelectedOutboundFlight = mPackageSelectedFlight;
	}

	public static Pair<FlightLeg, FlightLeg> getPackageFlightBundle() {
		return sharedInstance.mPackageFlightBundle;
	}

	public static void setPackageFlightBundle(FlightLeg outbound, FlightLeg inbound) {
		sharedInstance.mPackageFlightBundle = new Pair<>(outbound, inbound);
	}

	public static void setPackageParams(PackageSearchParams params) {
		sharedInstance.mPackageParams = params;
	}

	public static void setFlightSearchParams(com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams) {
		sharedInstance.mPackageParams = null;
		sharedInstance.mFlightSearchParams = flightSearchParams;
	}

	public static BundleSearchResponse getPackageResponse() {
		return sharedInstance.mPackageResponse;
	}

	public static void setPackageResponse(BundleSearchResponse hotelPackage) {
		sharedInstance.mPackageResponse = hotelPackage;
	}

	public static void setUnfilteredResponse(BundleSearchResponse unfilteredResponse) {
		sharedInstance.mUnfilteredResponse = unfilteredResponse;
	}

	public static void setCachedPackageResponse(BundleSearchResponse hotelPackage) {
		sharedInstance.cachedPackageResponse = hotelPackage;
	}

	public static BundleSearchResponse getUnfilteredRespnse() {
		return sharedInstance.mUnfilteredResponse;
	}

	public static BundleSearchResponse getCachedPackageResponse() {
		return sharedInstance.cachedPackageResponse;
	}

	public void clearTemporaryCard() {
		temporarilySavedCard = null;
	}

	public void setBillingInfo(BillingInfo billingInfo) {
		mBillingInfo = billingInfo;
	}

	public static BillingInfo getBillingInfo() {
		if (sharedInstance.mBillingInfo == null) {
			sharedInstance.mBillingInfo = new BillingInfo();
		}

		return sharedInstance.mBillingInfo;
	}

	public void setTemporarilySavedCard(BillingInfo temporarilySavedCard) {
		this.temporarilySavedCard = temporarilySavedCard;
	}

	public boolean hasBillingInfo() {
		return mBillingInfo != null;
	}

	public static FlightSearch getFlightSearch() {
		return sharedInstance.mFlightSearch;
	}

	public static Airline getAirline(String airlineCode) {
		// First, get the Airline from FS.db
		Airline airline = FlightStatsDbUtils.getAirline(airlineCode);

		if (airline == null) {
			airline = new Airline();
			airline.mAirlineCode = airlineCode;
		}

		return airline;
	}

	public static TripBucket getTripBucket() {
		return sharedInstance.mTripBucket;
	}

	@NotNull
	public static WorkingTravelerManager getWorkingTravelerManager() {
		return sharedInstance.instanceGetWorkingTravelerManager();
	}

	@NotNull
	public static WorkingBillingInfoManager getWorkingBillingInfoManager() {
		return sharedInstance.instanceGetWorkingBillingInfoManager();
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

	private boolean loadFromDisk(Context context, IDiskLoad loader, String fileName, String statsTag) {
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

	private boolean deleteFromDisk(Context context, String fileName, String statsTag) {
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
		sharedInstance.saveDbDataToDiskInBackgroundOnLowPriorityThread(context, new IDiskWrite() {
			@Override
			public int doWrite(Context context) throws Exception, OutOfMemoryError {
				JSONObject obj = new JSONObject();
				putJsonable(obj, "tripBucket", sharedInstance.mTripBucket);
				String json = obj.toString();
				IoUtils.writeStringToFile(SAVED_TRIP_BUCKET_FILE_NAME, json, context);
				return json.length();
			}
		}, "TripBucket");
	}

	public static boolean loadTripBucket(Context context) {
		return sharedInstance.loadFromDisk(context, new IDiskLoad() {
			@Override
			public boolean doLoad(JSONObject json) throws Exception, OutOfMemoryError {
				if (json.has("tripBucket")) {
					sharedInstance.mTripBucket = getJsonable(json, "tripBucket", TripBucket.class, sharedInstance.mTripBucket);
				}
				return true;
			}
		}, SAVED_TRIP_BUCKET_FILE_NAME, "TripBucket");
	}

	public static boolean deleteTripBucket(Context context) {
		return sharedInstance.deleteFromDisk(context, SAVED_TRIP_BUCKET_FILE_NAME, "TripBucket");
	}

	//////////////////////////////////////////////////////////////////////////
	// Saving/loading flight route data

	private static final String SAVED_FLIGHT_ROUTES_DATA_FILE = "flight-routes.db";

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
