package com.expedia.bookings.data.trips;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.crashlytics.android.Crashlytics;
import com.expedia.account.AccountService;
import com.expedia.account.data.FacebookLinkResponse;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.trips.Trip.LevelOfDetail;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.features.Features;
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils;
import com.expedia.bookings.itin.utils.NotificationScheduler;
import com.expedia.bookings.notification.INotificationManager;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.TripDetailsResponseHandler;
import com.expedia.bookings.services.TripsServicesInterface;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.tracking.TimeSource;
import com.expedia.bookings.tracking.TripsTracking;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.Flight;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.HttpException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This singleton keeps all itinerary data together from loading, sync & storage.
 * <p/>
 * Call init() before using in the app for the first time
 * Call startSync() before manipulating data.
 * <p/>
 * Sync uses a Priority Operation queue, allowing dynamic ordering and reordering of tasks.
 * <p/>
 * Operation Steps:-
 * 1. Initial loadStateFromDisk - can be safely called whenever
 * 2. Refresh/loadStateFromDisk trips - loadStateFromDisk data from all sources
 * 3. Load ancillary data for trips e.g. flight stats data about trips.
 * Any call other than normal refresh goes here.
 * Loading trip data in #2 should be followed by this step.
 * 4. Post-processing operations - these assume that all of the data in the itins have been loaded.
 * These operations include
 * saving the loaded data to disk
 * generating data for the app to consume
 * registering loaded data with notifications.
 * <p/>
 * See more in Operation enum.
 */
public class ItineraryManager implements JSONable, ItineraryManagerInterface {

	/* ********* DATA TYPES *************************** */

	public enum SyncError {
		OFFLINE,
		USER_LIST_REFRESH_FAILURE,
		CANCELLED,
	}

	/* ********* CLASS DATA *************************** */

	private static final int CUTOFF_HOURS = 48;

	private static final long FIVE_MINUTES = TimeUnit.MINUTES.toMillis(5);
	private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
	private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
	private static final long REFRESH_TRIP_CUTOFF = TimeUnit.MINUTES.toMillis(15);
	private static final long SEVEN_DAYS = TimeUnit.DAYS.toMillis(7);
	private static final long SEVENTY_TWO_HOURS = TimeUnit.HOURS.toMillis(72);
	private static final long TWELVE_HOURS = TimeUnit.HOURS.toMillis(12);
	private static final long TWENTY_FOUR_HOURS = TimeUnit.HOURS.toMillis(24);

	private static final long DEEP_REFRESH_RATE_LIMIT = ONE_MINUTE;
	private static final long UPDATE_CUTOFF = ONE_MINUTE;

	private static final DateTime FAKE_END_TIME = new DateTime(0);

	private static final String LOGGING_TAG = "ItineraryManager";
	private static final String MANAGER_PATH = "itin-manager.dat";
	private static final String MANAGER_START_END_TIMES_PATH = "itin-starts-and-ends.dat";

	private static final ItineraryManager ITINERARY_MANAGER = new ItineraryManager();

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat SORT_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");

	static {
		final TimeZone tz = TimeZone.getTimeZone("UTC");
		if (tz != null) {
			SORT_DATE_FORMATTER.setTimeZone(tz);
		}
	}

	/* ********* INSTANCE DATA *************************** */

	// In memory trips, updated with every sync, assumed to be sorted
	private final List<ItinCardData> itinCardData;
	private final PublishSubject<List<ItinCardData>> syncFinishObservable;

	private Context context;
	private UserStateManager userStateManager;
	private INotificationManager notificationManager;
	private ITripsJsonFileUtils tripsJsonFileUtils;
	private long mLastUpdateTime;
	private Map<String, Trip> trips;
	private SyncTask mSyncTask;

	private List<DateTime> endTimes = new ArrayList<>();
	private List<DateTime> startTimes = new ArrayList<>();
	private Queue<Task> mSyncOpQueue = new TaskPriorityQueue();
	private Set<ItinerarySyncListener> mSyncListeners = new HashSet<>();

	private Comparator<ItinCardData> mItinCardDataComparator = (dataOne, dataTwo) -> {

		final long startMillis1 = getStartMillisUtc(dataOne);
		final long startMillis2 = getStartMillisUtc(dataTwo);
		final int startDate1 = Integer.parseInt(SORT_DATE_FORMATTER.format(startMillis1));
		final int startDate2 = Integer.parseInt(SORT_DATE_FORMATTER.format(startMillis2));

		// checkInDate (but ignoring the time)
		int comparison = startDate1 - startDate2;
		if (comparison != 0) {
			return comparison;
		}

		// Type (flight < car < activity < hotel < cruise)
		comparison = dataOne.getTripComponentType().ordinal() - dataTwo.getTripComponentType().ordinal();
		if (comparison != 0) {
			return comparison;
		}

		// checkInDate (including time)
		long millisComp = startMillis1 - startMillis2;
		if (millisComp > 0) {
			return 1;
		} else if (millisComp < 0) {
			return -1;
		}

		// Unique ID
		comparison = dataOne.getId().compareTo(dataTwo.getId());

		return comparison;
	};

	/* ********* CLASS METHODS *************************** */

	public static ItineraryManager getInstance() {
		return ITINERARY_MANAGER;
	}

	public static boolean haveTimelyItinItem() {
		return hasUpcomingOrInProgressTrip(
				ITINERARY_MANAGER.startTimes,
				ITINERARY_MANAGER.endTimes
		);
	}

	@VisibleForTesting
	static boolean hasUpcomingOrInProgressTrip(List<DateTime> startTimes, List<DateTime> endTimes) {
		if (startTimes != null && endTimes != null && startTimes.size() == endTimes.size()) {
			final DateTime now = DateTime.now();
			final DateTime oneWeekFromNow = now.plusWeeks(1);
			for (int i = 0; i < startTimes.size(); i++) {
				final DateTime start = startTimes.get(i);
				final DateTime end = endTimes.get(i);
				if (now.isBefore(end) && oneWeekFromNow.isAfter(start)) {
					return true;
				}
			}
		}
		return false;
	}

	/* ********* INSTANCE METHODS *************************** */

	//***** initializer *****//

	private ItineraryManager() {
		this.itinCardData = new ArrayList<>();
		this.syncFinishObservable = PublishSubject.create();
	}

	public void init(Context context) {
		final long start = System.nanoTime();

		this.context = context;
		userStateManager = Ui.getApplication(context).appComponent().userStateManager();
		notificationManager = Ui.getApplication(context).appComponent().notificationManager();
		final NotificationScheduler notificationScheduler = Ui.getApplication(context)
																	.appComponent()
																	.notificationScheduler();
		if (!ExpediaBookingApp.isAutomation()) {
			notificationScheduler.subscribeToListener(syncFinishObservable);
		}
		tripsJsonFileUtils = Ui.getApplication(context).appComponent().tripJsonFileUtils();
		loadStartAndEndTimes();

		Log.d(LOGGING_TAG, "Initialized ItineraryManager in "
								   + ((System.nanoTime() - start) / 1000000) + " ms");
	}

	//***** JSONable *****//

	@Override
	public JSONObject toJson() {
		try {
			final JSONObject obj = new JSONObject();
			JSONUtils.putJSONableList(obj, "trips", trips.values());
			return obj;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		this.trips = new HashMap<>();
		final List<Trip> tripsFromJson = JSONUtils.getJSONableList(obj, "trips", Trip.class);
		for (Trip tripFromJson : tripsFromJson) {
			trips.put(tripFromJson.getItineraryKey(), tripFromJson);
		}
		return true;
	}

	//***** cleanup *****//

	public void clear() {
		if (isSyncing()) {
			mSyncTask.cancel(true);
			mSyncTask.cancelDownloads();
		} else {
			doClearData();
		}
	}

	//***** getters *****//

	public Collection<Trip> getTrips() {
		return trips != null ? trips.values() : Collections.emptyList();
	}

	public List<ItinCardData> getItinCardData() {
		return itinCardData;
	}

	public String getItinIdByTripNumber(String tripNumber) {
		if (tripNumber == null || tripNumber.length() == 0) {
			return null;
		}

		String itinerary = "";
		synchronized (itinCardData) {
			for (ItinCardData currentItin : itinCardData) {
				if (tripNumber.equals(currentItin.getTripNumber())) {
					itinerary = currentItin.getId();
					break;
				}
			}
		}
		return itinerary;
	}

	public Observable<List<ItinCardData>> getSyncFinishObservable() {
		return syncFinishObservable;
	}

	public ItinCardData getItinCardDataFromFlightHistoryId(int fhid) {
		final TripFlight tripFlight = getTripComponentFromFlightHistoryId(fhid);
		if (tripFlight != null) {
			synchronized (itinCardData) {
				for (ItinCardData data : itinCardData) {
					if (data.getTripComponent() == tripFlight) {
						return data;
					}
				}
			}
		}
		return null;
	}

	@Nullable
	public ItinCardData getItinCardDataFromItinId(String itinId) {
		synchronized (itinCardData) {
			for (ItinCardData data : itinCardData) {
				if (data.getId().equals(itinId) && data.hasDetailData()) {
					return data;
				}
			}
		}
		return null;
	}

	//***** setters *****//

	public void addGuestTrip(String email, String tripNumber) {
		this.addGuestTrip(email, tripNumber, null);
	}

	public void addGuestTrip(String email, String tripNumber, String tripID) {
		Log.i(LOGGING_TAG, "Adding guest trip, email=" + email + " tripNum=" + tripNumber);
		if (this.trips == null) {
			Log.w(LOGGING_TAG, "ItineraryManager - Attempt to add guest trip, trips == null. Init");
			this.trips = new HashMap<>();
		}

		final Trip trip = new Trip(email, tripNumber);
		trip.setTripId(tripID);
		trips.put(tripNumber, trip);

		mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP, trip));
		mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));
		mSyncOpQueue.add(new Task(Operation.GENERATE_ITIN_CARDS));
		startSyncIfNotInProgress();
	}

	/* ********* BACKGROUND TASKS *************************** */

	private void loadStateFromDisk() {
		if (trips != null) {
			return;
		}

		trips = new HashMap<>();
		final File file = context.getFileStreamPath(MANAGER_PATH);
		if (file.exists()) {
			try {
				fromJson(new JSONObject(
						IoUtils.readStringFromFile(MANAGER_PATH, context)
				));
				Log.i(LOGGING_TAG, "Loaded " + trips.size() + " trips from disk.");
			} catch (Exception e) {
				Log.w(LOGGING_TAG, "Could not loadStateFromDisk ItineraryManager data.", e);
				//noinspection ResultOfMethodCallIgnored
				file.delete();
				Log.i(LOGGING_TAG, "Starting with a fresh set of itineraries.");
			}
		}
	}

	private void saveStateToDisk() {
		Log.i(LOGGING_TAG, "Saving ItineraryManager data to disk...");
		saveStartAndEndTimes();
		try {
			IoUtils.writeStringToFile(
					MANAGER_PATH,
					toJson().toString(),
					context
			);
		} catch (IOException e) {
			Log.w(LOGGING_TAG, "Could not saveStateToDisk ItineraryManager data", e);
		}
	}

	private void generateItinCards() {
		synchronized (itinCardData) {
			itinCardData.clear();

			final DateTime pastCutOffDateTime = DateTime.now().minusHours(CUTOFF_HOURS);
			for (Trip trip : trips.values()) {
				if (trip.getTripComponents() != null) {
					final List<TripComponent> components = trip.getTripComponents(true);
					for (TripComponent comp : components) {
						final List<ItinCardData> items = ItinCardDataFactory.generateCardData(comp);
						if (items != null) {
							for (ItinCardData item : items) {
								final DateTime endDate = item.getEndDate();
								if (endDate != null && endDate.isAfter(pastCutOffDateTime)) {
									itinCardData.add(item);
								}
							}
						}
					}
				}
			}

			Collections.sort(itinCardData, mItinCardDataComparator);
		}
	}

	/* ********* PRIVATE METHODS *************************** */

	private long getStartMillisUtc(ItinCardData data) {
		DateTime date = data.getStartDate();
		if (date == null) {
			return 0;
		}
		return date.withZoneRetainFields(DateTimeZone.UTC).getMillis();
	}

	private void loadStartAndEndTimes() {
		Log.d(LOGGING_TAG, "Loading start and end times...");

		final File file = context.getFileStreamPath(MANAGER_START_END_TIMES_PATH);
		if (file.exists()) {
			try {
				final JSONObject obj = new JSONObject(IoUtils.readStringFromFile(MANAGER_START_END_TIMES_PATH, context));
				startTimes = JodaUtils.getDateTimeListFromJsonBackCompat(obj, "startDateTimes", "startTimes");
				endTimes = JodaUtils.getDateTimeListFromJsonBackCompat(obj, "endDateTimes", "endTimes");
			} catch (Exception e) {
				Log.w(LOGGING_TAG, "Could not loadStateFromDisk start times", e);
				//noinspection ResultOfMethodCallIgnored
				file.delete();
			}
		}
	}

	private void doClearData() {
		Log.i(LOGGING_TAG, "Clearing all data from ItineraryManager...");

		final File file = context.getFileStreamPath(MANAGER_PATH);
		if (file.exists()) {
			//noinspection ResultOfMethodCallIgnored
			file.delete();
		}

		tripsJsonFileUtils.deleteAllFiles();

		clearStartAndEndTimes(true, true);

		mLastUpdateTime = 0;

		synchronized (itinCardData) {
			itinCardData.clear();
		}

		if (trips == null) {
			return;
		}

		Log.d(LOGGING_TAG, "Informing the removal of " + trips.size()
								   + " trips due to clearing of ItineraryManager...");
		for (Trip trip : trips.values()) {
			onTripRemoved(trip);
		}

		notificationManager.deleteAll();

		trips.clear();
	}

	private TripFlight getTripComponentFromFlightHistoryId(int fhid) {
		ItinCardDataFlight fData = null;
		synchronized (itinCardData) {
			for (ItinCardData data : itinCardData) {
				if (data instanceof ItinCardDataFlight) {
					fData = (ItinCardDataFlight) data;
					for (Flight segment : fData.getFlightLeg().getSegments()) {
						if (segment.mFlightHistoryId == fhid) {
							return (TripFlight) fData.getTripComponent();
						}
					}
				}
			}
		}
		final String key = context.getString(R.string.preference_push_notification_any_flight);
		if (fData != null && SettingUtils.get(context, key, false)) {
			return (TripFlight) fData.getTripComponent();
		}

		return null;
	}

	private List<ItinCardData> getImmutableItinCardDatas() {
		return Collections.unmodifiableList(new ArrayList<>(itinCardData));
	}

	/* ********* PRIVATE METHODS - L2 *************************** */

	private void saveStartAndEndTimes() {
		// Sync data before disk write
		clearStartAndEndTimes(true, false);
		for (Trip trip : trips.values()) {
			final DateTime startDate = trip.getStartDate();
			final DateTime endDate = trip.getEndDate();
			if (startDate != null) {
				startTimes.add(startDate);
				endTimes.add(endDate != null ? endDate : FAKE_END_TIME);
			}
		}

		if (startTimes.size() <= 0 && endTimes.size() <= 0) {
			clearStartAndEndTimes(false, true);
		} else {
			try {
				final JSONObject jsonObject = new JSONObject();
				JodaUtils.putDateTimeListInJson(jsonObject, "startDateTimes", startTimes);
				JodaUtils.putDateTimeListInJson(jsonObject, "endDateTimes", endTimes);
				IoUtils.writeStringToFile(MANAGER_START_END_TIMES_PATH, jsonObject.toString(), context);
			} catch (Exception e) {
				Log.w(LOGGING_TAG, "Could not save start & end times to disk", e);
			}
		}
	}

	private void clearStartAndEndTimes(boolean fromMemory, boolean fromDisk) {
		if (fromMemory) {
			startTimes.clear();
			endTimes.clear();
		}

		if (fromDisk) {
			final File file = context.getFileStreamPath(MANAGER_START_END_TIMES_PATH);
			if (file.exists()) {
				//noinspection ResultOfMethodCallIgnored
				file.delete();
			}
		}
	}

	/* *********** /^\ *************************** */
	/* ********** (@_@) *************************** */
	/* ********** <(@)> *************************** */

	//////////////////////////////////////////////////////////////////////////
	// Sync listener


	public interface ItinerarySyncListener {

		/**
		 * Notes when a trip is added with basic info.
		 * <p/>
		 * Note: Guest trips will not have this called right when you add them
		 * (because they have no meaningful info at that point, and may not
		 * even be a valid trip).
		 */
		void onTripAdded(Trip trip);

		/**
		 * Each Trip that is updated during a sync gets its own callback
		 * so that you can update the UI before the entire sync process
		 * is complete.
		 * <p/>
		 * Not all Trips may get an updated trip call (e.g., a trip doesn't
		 * need an update because it was just updated a few minutes ago).
		 */
		void onTripUpdated(Trip trip);

		/**
		 * Notification when a trip failed to update
		 * <p/>
		 * This can be particularly useful to know when a guest trip that
		 * was added can't be updated at all.
		 * <p/>
		 * POSSIBLE TODO: info on why the update failed?
		 */
		void onTripUpdateFailed(Trip trip);

		void onTripFailedFetchingGuestItinerary();

		void onTripFailedFetchingRegisteredUserItinerary();

		/**
		 * Notification for when a Trip has been removed, either automatically
		 * from a logged in user account or manually for guest trips.
		 */
		void onTripRemoved(Trip trip);

		/**
		 * Notification for when a Trip added by the user is already completed + 48h
		 */
		void onCompletedTripAdded(Trip trip);

		/**
		 * Notification for when a Trip added by the user has a cancelled status
		 */
		void onCancelledTripAdded(Trip trip);

		/**
		 * Notification if sync itself has a failure.  There can be multiple
		 * failures during the sync process.  onSyncFinished() will still
		 * be called at the end.
		 */
		void onSyncFailure(SyncError error);

		/**
		 * Once the sync process is done it returns the list of Trips as
		 * it thinks exists.  Returns all trips currently in the ItineraryManager.
		 */
		void onSyncFinished(Collection<Trip> trips);
	}

	// Makes it so you don't have to implement everything from the interface
	public static class ItinerarySyncAdapter implements ItinerarySyncListener {
		public void onTripAdded(Trip trip) {
		}

		public void onTripUpdated(Trip trip) {
		}

		public void onTripUpdateFailed(Trip trip) {
		}

		public void onTripFailedFetchingGuestItinerary() {
		}

		public void onTripFailedFetchingRegisteredUserItinerary() {
		}

		public void onTripRemoved(Trip trip) {
		}

		public void onCompletedTripAdded(Trip trip) {
		}

		public void onCancelledTripAdded(Trip trip) {
		}

		public void onSyncFailure(SyncError error) {
		}

		public void onSyncFinished(Collection<Trip> trips) {
		}
	}

	@Override
	public void addSyncListener(ItinerarySyncListener listener) {
		mSyncListeners.add(listener);
	}

	@Override
	public void removeSyncListener(ItinerarySyncListener listener) {
		mSyncListeners.remove(listener);
	}

	private void onTripAdded(Trip trip) {
		Set<ItinerarySyncListener> listeners = new HashSet<>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onTripAdded(trip);
		}
	}

	private void onTripUpdated(Trip trip) {
		Set<ItinerarySyncListener> listeners = new HashSet<>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onTripUpdated(trip);
		}
	}

	private void onTripUpdateFailed(Trip trip) {
		Set<ItinerarySyncListener> listeners = new HashSet<>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onTripUpdateFailed(trip);
		}
	}

	@VisibleForTesting
	public void onTripFailedFetchingGuestItinerary() {
		Set<ItinerarySyncListener> listeners = new HashSet<>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onTripFailedFetchingGuestItinerary();
		}
	}

	@VisibleForTesting
	public void onTripFailedFetchingRegisteredUserItinerary() {
		Set<ItinerarySyncListener> listeners = new HashSet<>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onTripFailedFetchingRegisteredUserItinerary();
		}
	}

	private void onTripRemoved(Trip trip) {
		Set<ItinerarySyncListener> listeners = new HashSet<>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onTripRemoved(trip);
		}
	}

	private void onCompletedTripAdded(Trip trip) {
		Set<ItinerarySyncListener> listeners = new HashSet<>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onCompletedTripAdded(trip);
		}
	}

	private void onCancelledTripAdded(Trip trip) {
		Set<ItinerarySyncListener> listeners = new HashSet<>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onCancelledTripAdded(trip);
		}
	}

	private void onSyncFailed(SyncError error) {
		Set<ItinerarySyncListener> listeners = new HashSet<>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onSyncFailure(error);
		}
	}

	@VisibleForTesting
	public void onSyncFinished(Collection<Trip> trips) {
		Set<ItinerarySyncListener> listeners = new HashSet<>(mSyncListeners);
		syncFinishObservable.onNext(getImmutableItinCardDatas());
		for (ItinerarySyncListener listener : listeners) {
			listener.onSyncFinished(trips);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Data syncing
	//
	// Syncing is implemented as an operation queue.  Operations are added
	// to the queue, then sorted in priority order.  There is a sync looper
	// which executes each in order.
	//
	// There are a few advantages to this setup:
	//
	// 1. It allows anyone to enqueue operations while a sync is in progress.
	//    No matter where you are in the sync, new operations can be added
	//    safely and will get executed.
	//
	// 2. It allows for flexible updates.  If you just need to do a deep
	//    refresh of a single trip, that's possible using the same code that
	//    refreshes all data.
	//
	// 3. It makes updating routes easier.  For example, getting cached
	//    details in the user list update can still get images/flight status
	//    updates without wonky sync code.

	// !!!!!
	//
	// SUPER IMPORTANT, READ THIS OR I WILL HURT YOU:
	// The order of operations in this enum determines the priorities of each item;
	// the looper will pick the highest order operation to execute next.
	//
	// !!!!!!
	private enum Operation {
		LOAD_FROM_DISK,
		// Loads saved trips from disk, if we're just starting up
		REAUTH_FACEBOOK_USER,
		// Autologin for facebook users
		REFRESH_USER,
		// If logged in, refreshes the trip list of the user
		GATHER_TRIPS,
		// Enqueues all trips for later operation

		// Refresh ancillary parts of a trip; these are higher priority so that they're
		// completed after each trip is refreshed (for lazy loading purposes)
		REFRESH_TRIP_FLIGHT_STATUS,
		// Refreshes trip statuses on trip
		PUBLISH_TRIP_UPDATE,
		// Publishes that we've updated a trip

		// Trip refresh operations
		REFRESH_ALL_TRIPS,
		DEEP_REFRESH_TRIP,
		REFRESH_TRIP,

		FETCH_SHARED_ITIN,
		// Fetches the shared itin data
		REMOVE_ITIN,
		// Removes the selected itin.

		DEDUPLICATE_TRIPS,
		// Remove shared trip if it matches one associated to a user

		SAVE_TO_DISK,
		// Saves state of ItineraryManager to disk

		GENERATE_ITIN_CARDS,
		// Generates itin card data for use
	}

	private class Task implements Comparable<Task> {
		Operation mOp;

		Trip mTrip;

		String mTripNumber;

		Task(Operation op) {
			this(op, null, null);
		}

		Task(Operation op, Trip trip) {
			this(op, trip, null);
		}

		Task(Operation op, String tripNumber) {
			this(op, null, tripNumber);
		}

		Task(Operation op, Trip trip, String tripNumber) {
			mOp = op;
			mTrip = trip;
			mTripNumber = tripNumber;
		}

		@Override
		public int compareTo(@NonNull Task another) {
			if (mOp != another.mOp) {
				return mOp.ordinal() - another.mOp.ordinal();
			}

			// We can safely assume that if the ops are the same, they will both have a Trip associated
			if (mTrip != null) {
				return mTrip.compareTo(another.mTrip);
			}

			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Task)) {
				return false;
			}

			return compareTo((Task) o) == 0;
		}

		@Override
		public String toString() {
			String str = "task " + mOp;

			String tripKey = mTripNumber;
			if (TextUtils.isEmpty(tripKey) && mTrip != null) {
				tripKey = mTrip.getItineraryKey();
			}
			if (!TextUtils.isEmpty(tripKey)) {
				str += " trip=" + tripKey;
			}

			return str;
		}
	}

	// Priority queue that doesn't allow duplicates to be added
	@SuppressWarnings("serial")
	private static class TaskPriorityQueue extends PriorityBlockingQueue<Task> {
		@Override
		public boolean add(Task o) {
			if (!contains(o)) {
				return super.add(o);
			}

			return false;
		}
	}

	/**
	 * Start a sync operation.
	 * <p/>
	 * If a sync is already in progress then calls to this are ignored.
	 *
	 * @return true if the sync started or is in progress, false if it never started
	 */
	@Override
	public boolean startSync(boolean forceRefresh) {
		return startSync(forceRefresh, true, true);
	}

	public boolean startSync(boolean forceRefresh, boolean load, boolean update) {
		if (!forceRefresh && DateTime.now().getMillis() < UPDATE_CUTOFF + mLastUpdateTime) {
			Log.d(LOGGING_TAG, "ItineraryManager sync started too soon since last one; ignoring.");
			return false;
		}
		else if (trips != null && trips.size() == 0 && !userStateManager.isUserAuthenticated()
			&& !hasFetchSharedInQueue()) {
			Log.d(LOGGING_TAG,
				"ItineraryManager sync called, but there are no guest nor shared trips and the user is not logged in, so"
					+
					" we're not starting a formal sync; but we will call onSyncFinished() with no results");
			onSyncFinished(trips.values());
			return false;
		}
		else if (isSyncing()) {
			Log.d(LOGGING_TAG, "Tried to start a sync while one is already in progress.");
			return true;
		}
		else {
			Log.i(LOGGING_TAG, "Starting an ItineraryManager sync...");

			// Add default sync operations
			if (load) {
				mSyncOpQueue.add(new Task(Operation.LOAD_FROM_DISK));
			}
			if (update) {
				mSyncOpQueue.add(new Task(Operation.REAUTH_FACEBOOK_USER));
				mSyncOpQueue.add(new Task(Operation.REFRESH_USER));
				mSyncOpQueue.add(new Task(Operation.GATHER_TRIPS));
				mSyncOpQueue.add(new Task(Operation.DEDUPLICATE_TRIPS));
				mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));
				mSyncOpQueue.add(new Task(Operation.GENERATE_ITIN_CARDS));
			}

			startSyncIfNotInProgress();

			return true;
		}
	}

	public boolean deepRefreshTrip(Trip trip) {
		return deepRefreshTrip(trip.getItineraryKey(), false);
	}

	@Override
	public boolean deepRefreshTrip(String key, boolean doSyncIfNotFound) {
		Trip trip = trips.get(key);

		if (trip == null) {
			if (doSyncIfNotFound) {
				Log.i(LOGGING_TAG, "Deep refreshing trip " + key + ", trying a full refresh just in case.");

				// We'll try to refresh the user to find the trip
				mSyncOpQueue.add(new Task(Operation.REFRESH_USER));

				// Refresh the trip via tripNumber; does not guarantee it will be found
				// by the time we get here (esp. if the user isn't logged in).
				mSyncOpQueue.add(new Task(Operation.DEEP_REFRESH_TRIP, key));
			}
			else {
				Log.w(LOGGING_TAG, "Tried to deep refresh a trip which doesn't exist.");
				return false;
			}
		}
		else {
			Log.i(LOGGING_TAG, "Deep refreshing trip " + key);

			mSyncOpQueue.add(new Task(Operation.DEEP_REFRESH_TRIP, trip));
		}

		// We're set to sync; add the rest of the ops and go
		mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));
		mSyncOpQueue.add(new Task(Operation.GENERATE_ITIN_CARDS));

		startSyncIfNotInProgress();

		return true;
	}

	public boolean fetchSharedItin(String shareableUrl) {
		Log.i(LOGGING_TAG, "Fetching SharedItin " + shareableUrl);

		mSyncOpQueue.add(new Task(Operation.LOAD_FROM_DISK));
		mSyncOpQueue.add(new Task(Operation.FETCH_SHARED_ITIN, shareableUrl));
		mSyncOpQueue.add(new Task(Operation.DEDUPLICATE_TRIPS));
		mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));
		mSyncOpQueue.add(new Task(Operation.GENERATE_ITIN_CARDS));

		startSyncIfNotInProgress();

		return true;
	}

	public boolean removeItin(String tripNumber) {
		Log.i(LOGGING_TAG, "Removing Itin num = " + tripNumber);
		mSyncOpQueue.add(new Task(Operation.REMOVE_ITIN, tripNumber));
		mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));
		mSyncOpQueue.add(new Task(Operation.GENERATE_ITIN_CARDS));

		startSyncIfNotInProgress();

		return true;
	}

	private void startSyncIfNotInProgress() {
		if (!isSyncing()) {
			Log.i(LOGGING_TAG, "Starting a sync...");

			Ui.getApplication(context).defaultTripComponents();
			mSyncTask = new SyncTask(Ui.getApplication(context).tripComponent().tripServices(),
				new ExpediaServices(context),
				ServicesUtil.generateAccountService(context));
			mSyncTask.execute();
		}
	}

	@Override
	public boolean isSyncing() {
		return mSyncTask != null && mSyncTask.getStatus() != AsyncTask.Status.FINISHED && !mSyncTask.finished();
	}

	class SyncTask extends AsyncTask<Void, ProgressUpdate, Collection<Trip>> {

		/*
		 * Implementation note - we regularly check if the sync has been
		 * cancelled (after every service call).  If it has been cancelled,
		 * then we exit out quickly.  If you add any service calls, also
		 * check afterwards (since the service calls are where the app
		 * will get hung up during a cancel).
		 */
		private ExpediaServices mServices;
		private TripsServicesInterface tripsServices;
		private AccountService accountService;

		// Used for determining whether to publish an "added" or "update" when we refresh a guest trip
		private Set<String> mGuestTripsNotYetLoaded = new HashSet<>();

		// Earlier versions of AsyncTask do not tell you when they are cancelled correctly.
		// This will let us know when the AsyncTask has fully completed its mission (even
		// if it was cancelled).
		private boolean mFinished = false;

		// These variables are used for stat tracking
		private Map<Operation, Integer> mOpCount = new HashMap<>();
		private int tripsRefreshed = 0;
		private int mTripRefreshFailures = 0;
		private int tripsAdded = 0;
		private int tripsRemoved = 0;
		private int mFlightsUpdated = 0;

		SyncTask(TripsServicesInterface tripsServices, ExpediaServices legacyExpediaServices, AccountService accountService) {
			mServices = legacyExpediaServices;
			this.tripsServices = tripsServices;
			this.accountService = accountService;

			for (Operation op : Operation.values()) {
				mOpCount.put(op, 0);
			}
		}

		private class CurrentTime implements TimeSource {

			@Override
			public long now() {
				return DateTime.now().getMillis();
			}
		}

		@Override
		protected void onPreExecute() {
			if (trips == null) {
				Log.i(LOGGING_TAG,
					"Sync called with trips == null. Loading trips from disk and generating itin cards before other operations in queue.");
				TaskPriorityQueue queueWithLoadFromDiskFirst = new TaskPriorityQueue();
				queueWithLoadFromDiskFirst.add(new Task(Operation.LOAD_FROM_DISK));
				queueWithLoadFromDiskFirst.add(new Task(Operation.GENERATE_ITIN_CARDS));
				queueWithLoadFromDiskFirst.addAll(mSyncOpQueue);

				mSyncOpQueue.clear();
				mSyncOpQueue.addAll(queueWithLoadFromDiskFirst);
			}
		}

		@Override
		protected Collection<Trip> doInBackground(Void... params) {

			while (!mSyncOpQueue.isEmpty()) {
				Task nextTask = mSyncOpQueue.remove();

				switch (nextTask.mOp) {
				case LOAD_FROM_DISK:
					loadStateFromDisk();
					break;
				case REAUTH_FACEBOOK_USER:
					reAuthenticateFacebookUser();
					break;
				case REFRESH_USER:
					refreshUserList();
					break;
				case GATHER_TRIPS:
					gatherTrips();
					break;
				case REFRESH_TRIP_FLIGHT_STATUS:
					updateFlightStatuses(nextTask.mTrip);
					break;
				case PUBLISH_TRIP_UPDATE:
					publishTripUpdate(nextTask.mTrip);
					break;
				case REFRESH_ALL_TRIPS:
					refreshAllTrips(new CurrentTime(), trips);
					break;
				case DEEP_REFRESH_TRIP:
					Trip trip = nextTask.mTrip;
					if (trip == null && !TextUtils.isEmpty(nextTask.mTripNumber)) {
						// Try to retrieve a trip here
						trip = trips.get(nextTask.mTripNumber);

						if (trip == null) {
							Log.w(LOGGING_TAG, "Could not deep refresh trip # " + nextTask.mTripNumber
								+ "; it was not loaded as a guest trip nor user trip");
						}
					}

					if (trip != null) {
						refreshTrip(trip, true);
					}
					break;
				case REFRESH_TRIP:
					refreshTrip(nextTask.mTrip, false);
					break;
				case DEDUPLICATE_TRIPS:
					deduplicateTrips();
					break;
				case FETCH_SHARED_ITIN:
					downloadSharedItinTrip(nextTask.mTripNumber);
					break;
				case REMOVE_ITIN:
					removeTrip(nextTask.mTripNumber);
					break;
				case SAVE_TO_DISK:
					saveStateToDisk();
					break;
				case GENERATE_ITIN_CARDS:
					generateItinCards();
					break;
				}

				// Update stats
				mOpCount.put(nextTask.mOp, mOpCount.get(nextTask.mOp) + 1);

				// After each task, check if we've been cancelled
				if (isCancelled()) {
					return null;
				}
			}

			// If we get down to here, we can assume that the operation queue is finished
			// and we return a list of the existing Trips.
			return trips.values();
		}

		@Override
		protected void onProgressUpdate(ProgressUpdate... values) {
			super.onProgressUpdate(values);

			ProgressUpdate update = values[0];

			switch (update.mType) {
			case ADDED:
				onTripAdded(update.mTrip);
				break;
			case UPDATED:
				onTripUpdated(update.mTrip);
				break;
			case UPDATE_FAILED:
				onTripUpdateFailed(update.mTrip);
				break;
			case FAILED_FETCHING_GUEST_ITINERARY:
				onTripFailedFetchingGuestItinerary();
				break;
			case FAILED_FETCHING_REGISTERED_USER_ITINERARY:
				onTripFailedFetchingRegisteredUserItinerary();
				break;
			case REMOVED:
				onTripRemoved(update.mTrip);
				break;
			case SYNC_ERROR:
				onSyncFailed(update.mError);
				break;
			case USER_ADDED_COMPLETED_TRIP:
				onCompletedTripAdded(update.mTrip);
				break;
			case USER_ADDED_CANCELLED_TRIP:
				onCancelledTripAdded(update.mTrip);
				break;
			}
		}

		@Override
		protected void onPostExecute(Collection<Trip> trips) {
			super.onPostExecute(trips);

			onSyncFinished(trips);
			logStats();

			mFinished = true;
			if (userStateManager.isUserAuthenticated() || (trips != null && trips.size() > 0)) {
				// Only remember the last update time if there was something actually updated;
				// either the user was logged in (but had no trips) or there are guest trips
				// present.  Also, don't bother doing this w/ quick sync as we'll just be
				// starting a new sync immediately (which should trigger this)
				mLastUpdateTime = DateTime.now().getMillis();
			}
		}

		@Override
		protected void onCancelled() {
			// Currently, the only reason we are canceled is if
			// the user signs out mid-update.  So continue
			// the signout in that case.
			doClearData();

			onSyncFailed(SyncError.CANCELLED);

			onSyncFinished(null);

			logStats();

			mFinished = true;
		}

		// Should be called in addition to cancel(boolean), in order
		// to cancel the update mid-download
		void cancelDownloads() {
			mServices.onCancel();
		}

		boolean finished() {
			return mFinished;
		}

		private void logStats() {
			Log.d(LOGGING_TAG, "Sync Finished; stats below.");
			for (Operation op : Operation.values()) {
				Log.d(LOGGING_TAG, op.name() + ": " + mOpCount.get(op));
			}

			Log.i(LOGGING_TAG,
				"# Trips=" + (trips == null ? 0 : trips.size()) + "; # Added=" + tripsAdded + "; # Removed="
					+ tripsRemoved);
			Log.i(LOGGING_TAG, "# Refreshed=" + tripsRefreshed + "; # Failed Refresh=" + mTripRefreshFailures);
			Log.i(LOGGING_TAG, "# Flights Updated=" + mFlightsUpdated);
		}

		//////////////////////////////////////////////////////////////////////
		// Operations

		private void updateFlightStatuses(Trip trip) {
			long now = DateTime.now().getMillis();

			for (TripComponent tripComponent : trip.getTripComponents(true)) {
				if (tripComponent.getType() == Type.FLIGHT) {
					TripFlight tripFlight = (TripFlight) tripComponent;
					FlightTrip flightTrip = tripFlight.getFlightTrip();
					for (int i = 0; i < flightTrip.getLegCount(); i++) {
						FlightLeg fl = flightTrip.getLeg(i);

						for (Flight segment : fl.getSegments()) {
							long takeOff = segment.getOriginWaypoint().getMostRelevantDateTime().getMillis();
							long landing = segment.getArrivalWaypoint().getMostRelevantDateTime().getMillis();
							long timeToTakeOff = takeOff - now;
							long timeSinceLastUpdate = now - segment.mLastUpdated;
							if (segment.mFlightHistoryId == -1) {
								// we have never got data from FS, so segment.mLastUpdated is unreliable at best
								timeSinceLastUpdate = Long.MAX_VALUE;
							}

							// Logic for whether to update; this could be compacted, but I've left it
							// somewhat unwound so that it can actually be understood.
							boolean update = false;
							String status = segment.mStatusCode;
							if (!status.equals(Flight.STATUS_CANCELLED) && !status.equals(Flight.STATUS_DIVERTED)) {
								// only worth updating if we haven't already hit a final state (Cancelled, Diverted)
								// we will potentially check after LANDED as we get updated arrival info for a little while after landing
								if (timeToTakeOff > 0) {
									if ((timeToTakeOff < TWELVE_HOURS && timeSinceLastUpdate > FIVE_MINUTES)
										|| (timeToTakeOff < TWENTY_FOUR_HOURS && timeSinceLastUpdate > ONE_HOUR)
										|| (timeToTakeOff < SEVENTY_TWO_HOURS && timeSinceLastUpdate > TWELVE_HOURS)) {
										update = true;
									}
								}
								else if (now < landing && timeSinceLastUpdate > FIVE_MINUTES) {
									update = true;
								}
								else if (now > landing) {
									if (now < (landing + SEVEN_DAYS)
										&& timeSinceLastUpdate > (now - (landing + ONE_HOUR))
										&& timeSinceLastUpdate > FIVE_MINUTES) {
										// flight should have landed some time in the last seven days
										// AND the last update was less than 1 hour after the flight should have landed (or did land)
										// AND the last update was more than 5 minutes ago
										update = true;
									}
								}
							}

							if (update) {
								Flight updatedFlight = mServices.getUpdatedFlight(segment);

								if (isCancelled()) {
									return;
								}

								segment.updateFrom(updatedFlight);

								mFlightsUpdated++;
							}
						}
					}
				}
			}
		}

		private void publishTripUpdate(Trip trip) {
			// We only consider a guest trip added once it has some meaningful info
			if (trip.isGuest() && mGuestTripsNotYetLoaded.contains(trip.getTripNumber())) {
				publishProgress(new ProgressUpdate(ProgressUpdate.Type.ADDED, trip));
				tripsAdded++;
			}
			else {
				publishProgress(new ProgressUpdate(ProgressUpdate.Type.UPDATED, trip));
			}

			// POSSIBLE TODO: Only call tripUpated() when it's actually changed
		}

		void reAuthenticateFacebookUser() {
			accountService.facebookReauth(context)
					.blockingSubscribe(new DisposableObserver<FacebookLinkResponse>() {
						@Override
						public void onComplete() {
						}

						@Override
						public void onError(Throwable e) {
							Log.w("FB: Auto-Login failed, " + e.getMessage());
						}

						@Override
						public void onNext(FacebookLinkResponse response) {
							if (response == null) {
								Log.w("FB: Auto-Login failed, null response");
							}
							else if (response.isSuccess()) {
								Log.i("FB: Auto-Login succeeded");
							}
							else {
								Log.w("FB: Auto-Login failed, " + response.tlError);
							}
						}
					});
		}

		void refreshAllTrips(TimeSource timeSource, Map<String, Trip> trips) {
			final Map<String, Trip> readMap = Collections.unmodifiableMap(new HashMap(trips));
			final List<Observable<JSONObject>> observables = new ArrayList<>();
			for (Trip trip : readMap.values()) {
				if (timeSource.now() - REFRESH_TRIP_CUTOFF > trip.getLastCachedUpdateMillis()) {
					if (trip.isShared() && trip.hasExpired(CUTOFF_HOURS)) {
						Log.w(LOGGING_TAG, "REFRESH_ALL_TRIPS: Removing a shared trip because it is completed and past the cutoff.  tripNum=" + trip.getItineraryKey());

						Trip removeTrip = trips.remove(trip.getItineraryKey());
						publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, removeTrip));

						tripsRemoved++;
						continue;
					}
					if (trip.isShared()) {
						Log.i(LOGGING_TAG, "REFRESH_ALL_TRIPS: Adding observable for shared trip.  tripNum=" + trip.getItineraryKey());
						observables.add(tripsServices.getSharedTripDetailsObservable(trip.getShareInfo().getSharableDetailsApiUrl()).onErrorReturn(onErrorReturnNull()));
					}
					else if (trip.isGuest()) {
						Log.i(LOGGING_TAG, "REFRESH_ALL_TRIPS: Adding observable for guest trip.  tripNum=" + trip.getItineraryKey());
						observables.add(tripsServices.getGuestTripObservable(trip.getTripNumber(), trip.getGuestEmailAddress(), false).onErrorReturn(onErrorReturnNull()));
					}
					else {
						Log.i(LOGGING_TAG, "REFRESH_ALL_TRIPS: Adding observable for user trip.  tripNum=" + trip.getItineraryKey());
						observables.add(tripsServices.getTripDetailsObservable(trip.getTripId(), false).onErrorReturn(onErrorReturnNull()));
					}
				}
			}
			waitAndParseDetailResponses(observables, trips, new HandleTripResponse());
		}

		@NonNull
		private Function<Throwable, JSONObject> onErrorReturnNull() {
			return new Function<Throwable, JSONObject>() {
				@Override
				public JSONObject apply(Throwable throwable) {
					try {
						String jsonString = ((HttpException) throwable).response().errorBody().string();
						return new JSONObject(jsonString);
					}
					catch (Exception e) {
						return null;
					}
				}
			};
		}

		void waitAndParseDetailResponses(List<Observable<JSONObject>> observables, final Map<String, Trip> trips, final IHandleTripResponse handleTripResponse) {
			Observable.zip(observables, new Function<Object[], List<JSONObject>>() {
					@Override
					public List<JSONObject> apply(Object[] objects) throws Exception {
						ArrayList<JSONObject> responseList = new ArrayList<>();
						for (Object arg : objects) {
							responseList.add((JSONObject) arg);
						}
						Log.i(LOGGING_TAG,
							"REFRESH_ALL_TRIPS: Number of responses received in zip " + responseList.size());
						return responseList;
					}
				}).flatMap(new Function<List<JSONObject>, Observable<JSONObject>>() {
					@Override
					public Observable<JSONObject> apply(List<JSONObject> jsonObjects) {
						return Observable.fromIterable(jsonObjects);
					}
				}).blockingSubscribe(new Observer<JSONObject>() {
					@Override
					public void onComplete() {
					}

					@Override
					public void onError(Throwable e) {
						Log.e(LOGGING_TAG, "REFRESH_ALL_TRIPS: Error observable");
						e.printStackTrace();
					}

					@Override
					public void onSubscribe(Disposable d) {
					}

					@Override
					public void onNext(JSONObject jsonObject) {
						TripDetailsResponse response = (new TripDetailsResponseHandler()).handleJson(jsonObject);
						if ((response == null) || (response.getTrip() == null)) {
							Log.i(LOGGING_TAG, "REFRESH_ALL_TRIPS: Response is null");
							handleTripResponse.refreshTripResponseNull(new Trip());
						}
						else {
							Trip updatedTrip = response.getTrip();
							//getItineraryKey() handles both user and shared trips
							String itineraryKey = updatedTrip.getItineraryKey();
							if (trips.containsKey(itineraryKey)) {
								Trip trip = trips.get(itineraryKey);
								if (response.hasErrors()) {
									Log.i(LOGGING_TAG, "REFRESH_ALL_TRIPS: Response has errors");
									handleTripResponse.refreshTripResponseHasErrors(trip, response);
								}
								else {
									Log.i(LOGGING_TAG, "REFRESH_ALL_TRIPS: Response is a success");
									handleTripResponse.refreshTripResponseSuccess(trip, false, response);
									writeTripJsonResponseToFile(trip, jsonObject);
								}
							}
						}
					}
			});
		}

		private void refreshTrip(Trip trip, boolean deepRefresh) {
			// It's possible for a trip to be removed during refresh (if it ends up being canceled
			// during the refresh).  If it's been somehow queued for multiple refreshes (e.g.,
			// deep refresh called during a sync) then we want to skip trying to refresh it twice.
			if (!trips.containsKey(trip.getItineraryKey())) {
				return;
			}

			// Only update if we are outside the cutoff
			long now = DateTime.now().getMillis();
			if (now - REFRESH_TRIP_CUTOFF > trip.getLastCachedUpdateMillis() || deepRefresh) {
				// Limit the user to one deep refresh per DEEP_REFRESH_RATE_LIMIT. Use cache refresh if user attempts to
				// deep refresh within the limit.
				if (now - trip.getLastFullUpdateMillis() < DEEP_REFRESH_RATE_LIMIT) {
					deepRefresh = false;
				}

				if (trip.isShared() && trip.hasExpired(CUTOFF_HOURS)) {
					Log.w(LOGGING_TAG,
						"Removing a shared trip because it is completed and past the cutoff.  tripNum="
							+ trip.getItineraryKey());

					Trip removeTrip = trips.remove(trip.getItineraryKey());
					publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, removeTrip));

					tripsRemoved++;
					return;
				}

				TripDetailsResponse response;
				response = getTripDetailsResponse(trip, deepRefresh);

				HandleTripResponse handleTripResponse = new HandleTripResponse();
				if (response == null) {
					handleTripResponse.refreshTripResponseNull(trip);
				}
				else if (response.hasErrors()) {
					handleTripResponse.refreshTripResponseHasErrors(trip, response);
				}
				else {
					handleTripResponse.refreshTripResponseSuccess(trip, deepRefresh, response);
				}
			}
		}

		class HandleTripResponse implements IHandleTripResponse {
			@Override
			public void refreshTripResponseNull(@NotNull Trip trip) {
				publishProgress(new ProgressUpdate(ProgressUpdate.Type.UPDATE_FAILED, trip));
				mTripRefreshFailures++;
			}

			@Override
			public void refreshTripResponseHasErrors(@NotNull Trip trip, @NotNull TripDetailsResponse response) {
				Log.w(LOGGING_TAG, "Error updating trip " + trip.getItineraryKey() + ": " + response.gatherErrorMessage(context));

				// If it's a guest trip, and we've never retrieved info on it, it may be invalid.
				// As such, we should remove it (but don't remove a trip if it's ever been loaded
				// or it's not a guest trip).
				if (trip.isGuest() && trip.getLevelOfDetail() == LevelOfDetail.NONE) {
					if (response.getErrors().size() > 0) {
						Log.w(LOGGING_TAG, "Tried to load guest trip, but failed, so we're removing it.  Email="
							+ trip.getGuestEmailAddress() + " itinKey="
							+ trip.getItineraryKey());
						trips.remove(trip.getItineraryKey());
						ServerError.ErrorCode errorCode = response.getErrors().get(0).getErrorCode();
						if (errorCode == ServerError.ErrorCode.NOT_AUTHENTICATED) {
							publishProgress(new ProgressUpdate(ProgressUpdate.Type.FAILED_FETCHING_REGISTERED_USER_ITINERARY, trip));
						}
						else {
							publishProgress(new ProgressUpdate(ProgressUpdate.Type.FAILED_FETCHING_GUEST_ITINERARY, trip));
						}
					}
				}
				mTripRefreshFailures++;
			}

			@Override
			public void refreshTripResponseSuccess(@NotNull Trip trip, boolean deepRefresh, @NotNull TripDetailsResponse response) {
				Trip updatedTrip = response.getTrip();

				BookingStatus bookingStatus = updatedTrip.getBookingStatus();
				if (bookingStatus == BookingStatus.SAVED && trip.getLevelOfDetail() == LevelOfDetail.NONE
					&& trip.getLastCachedUpdateMillis() == 0) {
					// Normally we'd filter this out; but there is a special case wherein a guest trip is
					// still in a SAVED state right after booking (when we'd normally add it).  So we give
					// any guest trip a one-refresh; if we see that it's already been tried once, we let it
					// die a normal death
					Log.w(LOGGING_TAG, "Would have removed guest trip, but it is SAVED and has never been updated.");

					trip.markUpdated(false, new CurrentTime());
				}
				else if (BookingStatus.filterOut(updatedTrip.getBookingStatus())) {
					Log.w(LOGGING_TAG, "Removing a trip because it's being filtered by booking status.  tripNum="
						+ updatedTrip.getItineraryKey() + " status=" + bookingStatus);
					removeTrip(updatedTrip.getItineraryKey());
				}
				else {
					// Update trip
					trip.updateFrom(updatedTrip);
					trip.markUpdated(deepRefresh, new CurrentTime());

					tripsRefreshed++;

					if (trip.getAirAttach() != null && !trip.isShared()) {
						Db.getTripBucket().setAirAttach(trip.getAirAttach());
					}

					if (!(trip.getLevelOfDetail() == LevelOfDetail.SUMMARY_FALLBACK)) {
						mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP_FLIGHT_STATUS, trip));
						mSyncOpQueue.add(new Task(Operation.PUBLISH_TRIP_UPDATE, trip));
					}
				}
			}
		}

		TripDetailsResponse getTripDetailsResponse(Trip trip, boolean deepRefresh) {
			JSONObject json;
			if (trip.isShared()) {
				json = tripsServices.getSharedTripDetails(trip.getShareInfo().getSharableDetailsApiUrl());
			}
			else if (trip.isGuest()) {
				json = tripsServices.getGuestTrip(trip.getTripNumber(), trip.getGuestEmailAddress(), !deepRefresh);
			}
			else {
				json = tripsServices.getTripDetails(trip.getTripId(), !deepRefresh);
			}
			TripDetailsResponse response = (new TripDetailsResponseHandler()).handleJson(json);
			if (json != null && response != null && !response.hasErrors() && response.getTrip() != null) {
				response.getTrip().setGuestEmailAddress(trip.getGuestEmailAddress());
				response.getTrip().setIsShared(trip.isShared());
				writeTripJsonResponseToFile(response.getTrip(), json);
			}
			return response;
		}

		void writeTripJsonResponseToFile(Trip trip, JSONObject json) {
			if (trip.isGuest()) {
				try {
					JSONObject itin = json.optJSONObject("responseData");
					if (itin != null) {
						itin.put("isGuest", true);
					}
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			if (trip.isShared()) {
				try {
					JSONObject itin = json.optJSONObject("responseData");
					if (itin != null) {
						itin.put("isShared", true);
					}
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
				tripsJsonFileUtils.writeToFile(trip.getShareInfo().getSharableDetailsUrl(), json.toString());
			}
			else {
				tripsJsonFileUtils.writeToFile(trip.getTripId(), json.toString());
			}
		}

		void deleteTripJsonFromFile(Trip trip) {
			if (trip.isShared()) {
				tripsJsonFileUtils.deleteFile(trip.getShareInfo().getSharableDetailsUrl());
			}
			else {
				tripsJsonFileUtils.deleteFile(trip.getTripId());
			}
		}

		// If the user is logged in, retrieve a listing of current trips for logged in user
		private void refreshUserList() {
			if (!userStateManager.isUserAuthenticated()) {
				Log.d(LOGGING_TAG, "User is not logged in, not refreshing user list.");
			}
			else {
				// We only want to get the first N cached details if it's been more than
				// REFRESH_TRIP_CUTOFF since the last refresh.  If we've refreshed more
				// recently, then we only want to update individual trips as is necessary
				// (so that the summary call goes out quickly).
				boolean getCachedDetails = DateTime.now().getMillis() - REFRESH_TRIP_CUTOFF > mLastUpdateTime;

				Log.d(LOGGING_TAG, "User is logged in, refreshing the user list.  Using cached details call: "
					+ getCachedDetails);

				TripResponse response = mServices.getTrips();
				trackTripRefreshCallMade();

				if (isCancelled()) {
					return;
				}

				if (response == null || response.hasErrors()) {
					String errorMessage = null;
					if (response != null && response.hasErrors()) {
						errorMessage = response.gatherErrorMessage(context);
						for (ServerError serverError : response.getErrors()) {
							ServerError.ErrorCode errorCode = serverError.getErrorCode();
							if (errorCode == ServerError.ErrorCode.NOT_AUTHENTICATED) {
								logForcedLogoutToCrashlytics();
								userStateManager.signOut();
								break;
							}
						}
						Log.w(LOGGING_TAG, "Error updating trips: " + response.gatherErrorMessage(context));
					}
					publishProgress(new ProgressUpdate(SyncError.USER_LIST_REFRESH_FAILURE));
					if (errorMessage == null || errorMessage.isEmpty()) {
						errorMessage = "Trip details response is null or has errors";
					}
					TripsTracking.trackItinTripRefreshCallFailure(errorMessage);
				}
				else {
					Set<String> currentTrips = new HashSet<>(trips.keySet());

					for (Trip trip : response.getTrips()) {
						if (BookingStatus.filterOut(trip.getBookingStatus())) {
							continue;
						}

						String tripKey = trip.getItineraryKey();

						LevelOfDetail lod = trip.getLevelOfDetail();
						boolean hasFullDetails = lod == LevelOfDetail.FULL || lod == LevelOfDetail.SUMMARY_FALLBACK;
						if (!trips.containsKey(tripKey)) {
							trips.put(tripKey, trip);

							publishProgress(new ProgressUpdate(ProgressUpdate.Type.ADDED, trip));

							tripsAdded++;
						}
						else if (hasFullDetails) {
							trips.get(tripKey).updateFrom(trip);
						}

						if (hasFullDetails) {
							// If we have full details, mark this as recently updated so we don't
							// refresh it below
							trip.markUpdated(false, new CurrentTime());

							tripsRefreshed++;
						}

						if (trip.getAirAttach() != null && !trip.isShared()) {
							Db.getTripBucket().setAirAttach(trip.getAirAttach());
						}

						currentTrips.remove(tripKey);
					}

					// Remove all trips that were not returned by the server (not including guest trips or imported shared trips)
					for (String tripNumber : currentTrips) {
						Trip trip = trips.get(tripNumber);
						if (!trip.isGuest() && !trip.isShared()) {
							trip = trips.remove(tripNumber);
							publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, trip));
							tripsRemoved++;
						}
					}
					trackTripRefreshCallSuccess();
				}
			}
		}

		void trackTripRefreshCallSuccess() {
			if (Features.Companion.getAll().getTripsApiCallSuccess().enabled()) {
				TripsTracking.trackItinTripRefreshCallSuccess();
			}
		}

		void trackTripRefreshCallMade() {
			if (Features.Companion.getAll().getTripsApiCallMade().enabled()) {
				TripsTracking.trackItinTripRefreshCallMade();
			}
		}

		// Add all trips to be updated, even ones that may not need to be refreshed
		// (so we can see if any of the ancillary data needs to be refreshed).
		private void gatherTrips() {
			Log.i(LOGGING_TAG, "Gathering " + trips.values().size() + " trips...");

			Log.i(LOGGING_TAG, "====REFRESH_ALL_TRIPS====");
			mSyncOpQueue.add(new Task(Operation.REFRESH_ALL_TRIPS));
		}

		private void deduplicateTrips() {
			Map<String, String> sharedTripsMap = new HashMap<>();
			Set<String> sharedTripsToRemove = new HashSet<>();

			// Collect all of the shared trips
			for (Trip trip : trips.values()) {
				if (trip.isShared()) {
					sharedTripsMap.put(trip.getTripComponents().get(0).getUniqueId(), trip.getItineraryKey());
				}
			}

			// Check each "regular" trip and see if it matches one of the shared trips
			for (Trip trip : trips.values()) {
				if (!trip.isShared()) {
					for (TripComponent tc : trip.getTripComponents()) {
						if (sharedTripsMap.keySet().contains(tc.getUniqueId())) {
							sharedTripsToRemove.add(sharedTripsMap.get(tc.getUniqueId()));
						}
					}
				}
			}

			// Remove the shared trips
			Trip trip;
			for (String key : sharedTripsToRemove) {
				Log.i(LOGGING_TAG, "Removing duplicate shared itin key=" + key);
				trip = trips.remove(key);
				publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, trip));
				tripsRemoved++;
			}
		}

		private void removeTrip(String tripNumber) {
			Trip trip = trips.get(tripNumber);
			if (trip == null) {
				Log.w(LOGGING_TAG, "Tried to remove a tripNumber that doesn't exist: " + tripNumber);
			}
			else {
				Log.i(LOGGING_TAG, "Removing trip with # " + tripNumber);

				publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, trip));
				// Delete notifications if any.
				deletePendingNotification(trip);
				trips.remove(tripNumber);
				tripsRemoved++;

				deleteTripJsonFromFile(trip);
			}
		}

		private void downloadSharedItinTrip(String shareableUrl) {
			Log.i(LOGGING_TAG, "Creating shared itin placeholder " + shareableUrl);
			// Create a placeholder trip for this shared itin so that the ItineraryManager knows to
			// notify the UI to show loading indicator.
			Trip trip = new Trip();
			trip.setIsShared(true);
			trip.getShareInfo().setSharableDetailsUrl(shareableUrl);
			trips.put(shareableUrl, trip);

			Log.i(LOGGING_TAG, "Fetching shared itin " + shareableUrl);
			JSONObject json = tripsServices.getSharedTripDetails(trip.getShareInfo().getSharableDetailsApiUrl());
			TripDetailsResponse response = (new TripDetailsResponseHandler()).handleJson(json);
			if (json != null && response != null && !response.hasErrors()) {
				writeTripJsonResponseToFile(trip, json);
			}

			if (isCancelled()) {
				return;
			}

			if (response == null || response.hasErrors()) {
				if (response != null && response.hasErrors()) {
					Log.w(LOGGING_TAG, "Error fetching shared itin : " + response.gatherErrorMessage(context));
				}
				removeItin(shareableUrl);
			}
			else {
				Trip sharedTrip = response.getTrip();
				sharedTrip.setIsShared(true);

				// Stuff the URL in the parent trip for later retrieval in case the sharee wants to become a sharer.
				// This response does not contain any shareable url, so, we gotta save it for later on our own.
				sharedTrip.getShareInfo().setSharableDetailsUrl(shareableUrl);

				if (sharedTrip.hasExpired(CUTOFF_HOURS)) {
					publishProgress(new ProgressUpdate(ProgressUpdate.Type.USER_ADDED_COMPLETED_TRIP, sharedTrip));
					// Remove placeholder for loading
					removeItin(shareableUrl);
					return;
				}
				if (sharedTrip.getBookingStatus() == BookingStatus.CANCELLED) {
					publishProgress(new ProgressUpdate(ProgressUpdate.Type.USER_ADDED_CANCELLED_TRIP, sharedTrip));
					// Remove placeholder for loading
					removeItin(shareableUrl);
					return;
				}

				LevelOfDetail lod = sharedTrip.getLevelOfDetail();
				boolean hasFullDetails = lod == LevelOfDetail.FULL || lod == LevelOfDetail.SUMMARY_FALLBACK;
				if (!trips.containsKey(shareableUrl)) {
					trips.put(shareableUrl, sharedTrip);

					publishProgress(new ProgressUpdate(ProgressUpdate.Type.ADDED, sharedTrip));

					tripsAdded++;
				}
				else if (hasFullDetails) {
					trips.get(shareableUrl).updateFrom(sharedTrip);
				}

				if (hasFullDetails) {
					// If we have full details, mark this as recently updated so we don't
					// refresh it below
					sharedTrip.markUpdated(false, new CurrentTime());

					tripsRefreshed++;

					mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP_FLIGHT_STATUS, sharedTrip));
				}

				if (trip.getAirAttach() != null && !trip.isShared()) {
					Db.getTripBucket().setAirAttach(trip.getAirAttach());
				}

				// Note: In the future, we may be getting these parameters from the URL. Currently, we do not, thus we just
				// send the generic "AppShare" event any time that we import a shared itin. Ideally, the URL will contain
				// some more info pertaining to tracking and we'd send something like "AppShare.Facebook".
				HashMap<String, String> deepLinkArg = new HashMap<>();
				deepLinkArg.put("brandcid", "AppShare");
				OmnitureTracking.storeDeepLinkParams(deepLinkArg);
			}
		}

	}

	private static class ProgressUpdate {
		enum Type {
			ADDED,
			UPDATED,
			UPDATE_FAILED,
			FAILED_FETCHING_GUEST_ITINERARY,
			FAILED_FETCHING_REGISTERED_USER_ITINERARY,
			REMOVED,
			SYNC_ERROR,
			USER_ADDED_COMPLETED_TRIP,
			USER_ADDED_CANCELLED_TRIP,
		}

		public Type mType;
		Trip mTrip;

		SyncError mError;

		ProgressUpdate(Type type, Trip trip) {
			mType = type;
			mTrip = trip;
		}

		ProgressUpdate(SyncError error) {
			mType = Type.SYNC_ERROR;
			mError = error;
		}
	}

	private void deletePendingNotification(Trip trip) {
		List<TripComponent> components = trip.getTripComponents(true);
		if (components == null) {
			return;
		}
		for (TripComponent tc : components) {
			String itinId = tc.getUniqueId();
			notificationManager.deleteAll(itinId);
		}
	}

	private boolean hasFetchSharedInQueue() {
		for (Task task : mSyncOpQueue) {
			if (task.mOp == Operation.FETCH_SHARED_ITIN) {
				return true;
			}
		}
		return false;
	}

	private void logForcedLogoutToCrashlytics() {
		User user = userStateManager.getUserSource().getUser();

		if (user != null) {
			String tuidString = user.getTuidString();
			String expediaId = user.getExpediaUserId();
			Throwable exception = new Throwable("Forced logout on itinerary refresh - " + "UserTUID: " + tuidString + " - ExpediaUserId: " + expediaId);
			Crashlytics.logException(exception);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable
	//
	// Please don't actually try to serialize this object; this is mostly for
	// ease of being able to internally save/reproduce this manager.

}
