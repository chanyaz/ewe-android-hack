package com.expedia.bookings.data.trips;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
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
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager;
import com.expedia.bookings.featureconfig.SatelliteFeatureConstants;
import com.expedia.bookings.itin.utils.NotificationScheduler;
import com.expedia.bookings.notification.GCMRegistrationKeeper;
import com.expedia.bookings.notification.NotificationManager;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.TripDetailsResponseHandler;
import com.expedia.bookings.services.TripsServicesInterface;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.tracking.TimeSource;
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
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

/**
 * This singleton keeps all of our itinerary data together.  It loads, syncs and stores all itin data.
 * <p/>
 * Make sure to call init() before using in the app!  In addition, make sure to call startSync()
 * before manipulating data.
 * <p/>
 * An explanation about how the syncs happen: in order to allow syncs to be modified mid-execution, we implemented
 * a priority Operation queue which re-orders a series of instructions to perform a sync.
 * <p/>
 * There are essentially four steps to how Operations worked:
 * <p/>
 * 1. Initial load.  This can be safely called whenever.
 * 2. Refresh/load trips.  These steps load data from all sources.
 * 3. Load ancillary data about trips.  For example, flight stats data about trips.  Any calls beyond
 * our normal refresh should go here.  Also, anyone who loads trip data in #2 should make sure to call
 * these ancillary data calls.
 * 4. Post-processing operations; these assume that all of the data in the itins have been loaded.  These
 * operations include saving the loaded data to disk, generating data for the app to consume, and
 * registering loaded data with notifications.
 * <p/>
 * For more information, check out the Operation enum comments.
 */
public class ItineraryManager implements JSONable {

	private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
	private static final long FIVE_MINUTES = TimeUnit.MINUTES.toMillis(5);

	private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
	private static final long TWELVE_HOURS = TimeUnit.HOURS.toMillis(12);
	private static final long TWENTY_FOUR_HOURS = TimeUnit.HOURS.toMillis(24);
	private static final long SEVENTY_TWO_HOURS = TimeUnit.HOURS.toMillis(72);

	private static final long SEVEN_DAYS = TimeUnit.DAYS.toMillis(7);

	private static final long UPDATE_CUTOFF = ONE_MINUTE; // At most once a minute
	private static final long REFRESH_TRIP_CUTOFF = TimeUnit.MINUTES.toMillis(15);
	private static final long DEEP_REFRESH_RATE_LIMIT = ONE_MINUTE;

	private static final String LOGGING_TAG = "ItineraryManager";

	private static final ItineraryManager sManager = new ItineraryManager();

	private ItineraryManager() {
		// Cannot be instantiated
	}

	public static ItineraryManager getInstance() {
		return sManager;
	}

	// Should be initialized from the Application so that this does not leak a component
	private Context mContext;
	private UserStateManager userStateManager;
	private NotificationManager notificationManager;

	// Don't try refreshing too often
	private long mLastUpdateTime;

	private Map<String, Trip> mTrips;

	// This is an in-memory representation of the trips.  It is not
	// saved, but rather reproduced from the trip list.  It updates
	// each time a sync occurs.
	//
	// It can be assumed that it is sorted at all times.
	private final List<ItinCardData> mItinCardDatas = new ArrayList<>();

	// These are lists of all trip start and end times; unlike mTrips, they will be loaded at app startup, so you can use them to
	// determine whether you should launch in itin or not.
	private List<DateTime> mStartTimes = new ArrayList<>();
	private List<DateTime> mEndTimes = new ArrayList<>();

	private final PublishSubject<List<ItinCardData>> syncFinishObserverable = PublishSubject.create();

	/**
	 * Adds a guest trip to the itinerary list.
	 * <p/>
	 * Automatically starts to try to get info on the trip from the server.  If a sync is already
	 * in progress it will queue the guest trip for refresh; otherwise it will only refresh this
	 * single guest trip.
	 */
	public void addGuestTrip(String email, String tripNumber) {
		Log.i(LOGGING_TAG, "Adding guest trip, email=" + email + " tripNum=" + tripNumber);

		if (mTrips == null) {
			Log.w(LOGGING_TAG, "ItineraryManager - Attempt to add guest trip, mTrips == null. Init");
			mTrips = new HashMap<>();
		}

		Trip trip = new Trip(email, tripNumber);
		mTrips.put(tripNumber, trip);

		mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP, trip));
		mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));
		mSyncOpQueue.add(new Task(Operation.GENERATE_ITIN_CARDS));

		startSyncIfNotInProgress();
	}

	/**
	 * Get a list of the current Trips.
	 * <p/>
	 * Be warned: these Trips will be updated from a sync
	 * operation.  If this behavior seems wrong, talk with
	 * DLew since he's open to the idea of changing this
	 * behavior.
	 */
	public Collection<Trip> getTrips() {
		if (mTrips != null) {
			return mTrips.values();
		}

		return Collections.emptyList();
	}

	public List<ItinCardData> getItinCardData() {
		return mItinCardDatas;
	}

	public void addItinCard(ItinCardData itinCardData) {
		mItinCardDatas.add(itinCardData);
	}

	public List<ItinCardData> getImmutableItinCardDatas() {
		return Collections.unmodifiableList(new ArrayList(mItinCardDatas));
	}

	public String getItinIdByTripNumber(String tripNumber) {
		synchronized (mItinCardDatas) {
			if (!TextUtils.isEmpty(tripNumber)) {
				for (int i = 0; i < mItinCardDatas.size(); i++) {
					if (tripNumber.equals(mItinCardDatas.get(i).getTripNumber())) {
						return mItinCardDatas.get(i).getId();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get a TripComponent object from a flightHistoryId
	 * This is useful for push notifications which provide us with a flightHistoryId as
	 * the only identifier
	 * <p/>
	 * Note: We are only searching the mItinCardDatas collection, so only itins displayed
	 * in the itin list will be searched
	 *
	 * @param fhid - flightHistoryId from flightstats
	 * @return TripComponent containing the flight with the matching historyId or null
	 */
	public TripFlight getTripComponentFromFlightHistoryId(int fhid) {

		synchronized (mItinCardDatas) {
			ItinCardDataFlight fData = null;
			for (ItinCardData data : mItinCardDatas) {
				if (data instanceof ItinCardDataFlight) {
					fData = (ItinCardDataFlight) data;
					FlightLeg flightLeg = fData.getFlightLeg();
					for (Flight segment : flightLeg.getSegments()) {
						if (segment.mFlightHistoryId == fhid) {
							return (TripFlight) fData.getTripComponent();
						}
					}
				}
			}
			if (SettingUtils.get(mContext,
				mContext.getString(R.string.preference_push_notification_any_flight), false) && fData != null) {
				return (TripFlight) fData.getTripComponent();
			}
		}
		return null;
	}

	/**
	 * Get a ItinCardData object from a flightHistoryId
	 * This is useful for push notifications which provide us with a flightHistoryId as
	 * the only identifier
	 * <p/>
	 * Note: We are only searching the mItinCardDatas collection, so only itins displayed
	 * in the itin list will be searched
	 *
	 * @param fhid - flightHistoryId from flightstats
	 * @return ItinCardData containing the flight with the matching historyId or null
	 */
	public ItinCardData getItinCardDataFromFlightHistoryId(int fhid) {
		TripFlight tripFlight = getTripComponentFromFlightHistoryId(fhid);
		if (tripFlight != null) {
			synchronized (mItinCardDatas) {
				for (ItinCardData data : mItinCardDatas) {
					if (data.getTripComponent() == tripFlight) {
						return data;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get an ItinCardData object from all known itins given a known data.getId()
	 *
	 * @return first ItinCardData found matching the passed id or null
	 */
	public ItinCardData getItinCardDataFromItinId(String itinId) {

		synchronized (mItinCardDatas) {
			for (ItinCardData data : mItinCardDatas) {
				if (data.getId().equals(itinId) && data.hasDetailData()) {
					return data;
				}
			}
		}

		return null;
	}

	/**
	 * Clear all data from the itinerary manager.  Used on sign out or
	 * when private data is cleared.
	 */
	public void clear() {
		if (isSyncing()) {
			// If we're syncing, cancel the sync (then let the canceled task
			// do the sign out once it's finished).
			mSyncTask.cancel(true);
			mSyncTask.cancelDownloads();
		}
		else {
			doClearData();
		}
	}

	private void doClearData() {
		Log.i(LOGGING_TAG, "Clearing all data from ItineraryManager...");

		// Delete the file, so it can't be reloaded later
		File file = mContext.getFileStreamPath(MANAGER_PATH);
		if (file.exists()) {
			//noinspection ResultOfMethodCallIgnored
			file.delete();
		}

		mStartTimes.clear();
		mEndTimes.clear();
		deleteStartAndEndTimes();

		mLastUpdateTime = 0;

		synchronized (mItinCardDatas) {
			mItinCardDatas.clear();
		}

		if (mTrips == null) {
			return;
		}

		Log.d(LOGGING_TAG, "Informing the removal of " + mTrips.size()
			+ " trips due to clearing of ItineraryManager...");

		for (Trip trip : mTrips.values()) {
			onTripRemoved(trip);
		}

		notificationManager.deleteAll();

		mTrips.clear();

		// As we have no trips, we unregister all of our push notifications
		PushNotificationUtils.unRegister(mContext,
			GCMRegistrationKeeper.getInstance(mContext).getRegistrationId(mContext));
		PushNotificationUtils.clearPayloadMap();
	}

	//////////////////////////////////////////////////////////////////////////
	// Data

	private static final String MANAGER_PATH = "itin-manager.dat";

	private static final String MANAGER_START_END_TIMES_PATH = "itin-starts-and-ends.dat";

	/**
	 * Must be called before using ItineraryManager for the first time.
	 * <p/>
	 * I expect this to be called from the Application.  That way the
	 * context won't leak.
	 */
	public void init(Context context) {
		long start = System.nanoTime();

		mContext = context;
		userStateManager = Ui.getApplication(context).appComponent().userStateManager();
		notificationManager = Ui.getApplication(context).appComponent().notificationManager();
		NotificationScheduler notificationScheduler = Ui.getApplication(context).appComponent().notificationScheduler();
		if (!ExpediaBookingApp.isAutomation()) {
			notificationScheduler.subscribeToListener(syncFinishObserverable);
		}
		loadStartAndEndTimes();

		Log.d(LOGGING_TAG, "Initialized ItineraryManager in " + ((System.nanoTime() - start) / 1000000) + " ms");
	}

	private void save() {
		Log.i(LOGGING_TAG, "Saving ItineraryManager data...");

		saveStartAndEndTimes();

		try {
			IoUtils.writeStringToFile(MANAGER_PATH, toJson().toString(), mContext);
		}
		catch (IOException e) {
			Log.w(LOGGING_TAG, "Could not save ItineraryManager data", e);
		}
	}

	private void load() {
		if (mTrips == null) {
			File file = mContext.getFileStreamPath(MANAGER_PATH);
			if (file.exists()) {
				try {
					JSONObject obj = new JSONObject(IoUtils.readStringFromFile(MANAGER_PATH, mContext));
					fromJson(obj);
					Log.i(LOGGING_TAG, "Loaded " + mTrips.size() + " trips from disk.");
				}
				catch (Exception e) {
					Log.w(LOGGING_TAG, "Could not load ItineraryManager data, starting from scratch again...", e);
					//noinspection ResultOfMethodCallIgnored
					file.delete();
				}
			}
		}

		if (mTrips == null) {
			mTrips = new HashMap<>();

			Log.i(LOGGING_TAG, "Starting a fresh set of itineraries.");
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Start times data

	private List<DateTime> getStartTimes() {
		return mStartTimes;
	}

	private List<DateTime> getEndTimes() {
		return mEndTimes;
	}

	private void saveStartAndEndTimes() {
		// Sync start times whenever we save to disk
		mStartTimes.clear();
		mEndTimes.clear();
		for (Trip trip : mTrips.values()) {
			DateTime startDate = trip.getStartDate();
			DateTime endDate = trip.getEndDate();
			if (startDate != null) {
				mStartTimes.add(startDate);
				if (endDate != null) {
					mEndTimes.add(endDate);
				}
				else {
					//We want a valid date object even if it is bunk
					DateTime fakeEnd = new DateTime(0);
					mEndTimes.add(fakeEnd);
				}
			}
		}

		if (mStartTimes.size() <= 0 && mEndTimes.size() <= 0) {
			deleteStartAndEndTimes();
		}
		else {
			try {
				// Save to disk
				JSONObject obj = new JSONObject();
				JodaUtils.putDateTimeListInJson(obj, "startDateTimes", mStartTimes);
				JodaUtils.putDateTimeListInJson(obj, "endDateTimes", mEndTimes);
				IoUtils.writeStringToFile(MANAGER_START_END_TIMES_PATH, obj.toString(), mContext);
			}
			catch (Exception e) {
				Log.w(LOGGING_TAG, "Could not save start and end times", e);
			}
		}
	}

	private void loadStartAndEndTimes() {
		Log.d(LOGGING_TAG, "Loading start and end times...");

		File file = mContext.getFileStreamPath(MANAGER_START_END_TIMES_PATH);
		if (file.exists()) {
			try {
				JSONObject obj = new JSONObject(IoUtils.readStringFromFile(MANAGER_START_END_TIMES_PATH, mContext));
				mStartTimes = JodaUtils.getDateTimeListFromJsonBackCompat(obj, "startDateTimes", "startTimes");
				mEndTimes = JodaUtils.getDateTimeListFromJsonBackCompat(obj, "endDateTimes", "endTimes");
			}
			catch (Exception e) {
				Log.w(LOGGING_TAG, "Could not load start times", e);
				//noinspection ResultOfMethodCallIgnored
				file.delete();
			}
		}
	}

	private void deleteStartAndEndTimes() {
		File file = mContext.getFileStreamPath(MANAGER_START_END_TIMES_PATH);
		if (file.exists()) {
			//noinspection ResultOfMethodCallIgnored
			file.delete();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Itin card data

	private static final int CUTOFF_HOURS = 48;

	private void generateItinCardData() {
		synchronized (mItinCardDatas) {
			mItinCardDatas.clear();

			DateTime pastCutOffDateTime = DateTime.now().minusHours(CUTOFF_HOURS);
			for (Trip trip : mTrips.values()) {
				if (trip.getTripComponents() != null) {
					List<TripComponent> components = trip.getTripComponents(true);
					for (TripComponent comp : components) {
						List<ItinCardData> items = ItinCardDataFactory.generateCardData(comp);
						if (items != null) {
							for (ItinCardData item : items) {
								DateTime endDate = item.getEndDate();
								if (endDate != null && endDate.isAfter(pastCutOffDateTime)) {
									mItinCardDatas.add(item);
								}
							}
						}
					}
				}
			}

			Collections.sort(mItinCardDatas, mItinCardDataComparator);
		}
	}

	private Comparator<ItinCardData> mItinCardDataComparator = new Comparator<ItinCardData>() {
		@Override
		public int compare(ItinCardData dataOne, ItinCardData dataTwo) {
			// Sort by:
			// 1. "checkInDate" (but ignoring the time)
			// 2. Type (flight < car < activity < hotel < cruise)
			// 3. "checkInDate" (including time)
			// 4. Unique ID

			long startMillis1 = getStartMillisUtc(dataOne);
			long startMillis2 = getStartMillisUtc(dataTwo);

			int startDate1 = Integer.parseInt(SORT_DATE_FORMATTER.format(startMillis1));
			int startDate2 = Integer.parseInt(SORT_DATE_FORMATTER.format(startMillis2));

			// 1
			int comparison = startDate1 - startDate2;
			if (comparison != 0) {
				return comparison;
			}

			// 2
			comparison = dataOne.getTripComponentType().ordinal() - dataTwo.getTripComponentType().ordinal();
			if (comparison != 0) {
				return comparison;
			}

			// 3
			long millisComp = startMillis1 - startMillis2;
			if (millisComp > 0) {
				return 1;
			}
			else if (millisComp < 0) {
				return -1;
			}

			// 4
			comparison = dataOne.getId().compareTo(dataTwo.getId());

			return comparison;
		}
	};

	private long getStartMillisUtc(ItinCardData data) {
		DateTime date = data.getStartDate();
		if (date == null) {
			return 0;
		}
		return date.withZoneRetainFields(DateTimeZone.UTC).getMillis();
	}

	@SuppressLint("SimpleDateFormat")
	private static final DateFormat SORT_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");

	static {
		// Try to format in UTC for comparison purposes
		TimeZone tz = TimeZone.getTimeZone("UTC");
		if (tz != null) {
			SORT_DATE_FORMATTER.setTimeZone(tz);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Sync listener

	public enum SyncError {
		OFFLINE,
		USER_LIST_REFRESH_FAILURE,
		CANCELLED,
	}

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

	private Set<ItinerarySyncListener> mSyncListeners = new HashSet<>();

	public void addSyncListener(ItinerarySyncListener listener) {
		mSyncListeners.add(listener);
	}

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
		syncFinishObserverable.onNext(getImmutableItinCardDatas());
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

	private Queue<Task> mSyncOpQueue = new TaskPriorityQueue();

	private SyncTask mSyncTask;

	/**
	 * Start a sync operation.
	 * <p/>
	 * If a sync is already in progress then calls to this are ignored.
	 *
	 * @return true if the sync started or is in progress, false if it never started
	 */
	public boolean startSync(boolean forceRefresh) {
		return startSync(forceRefresh, true, true);
	}

	public boolean startSync(boolean forceRefresh, boolean load, boolean update) {
		if (!forceRefresh && DateTime.now().getMillis() < UPDATE_CUTOFF + mLastUpdateTime) {
			Log.d(LOGGING_TAG, "ItineraryManager sync started too soon since last one; ignoring.");
			return false;
		}
		else if (mTrips != null && mTrips.size() == 0 && !userStateManager.isUserAuthenticated()
			&& !hasFetchSharedInQueue()) {
			Log.d(LOGGING_TAG,
				"ItineraryManager sync called, but there are no guest nor shared trips and the user is not logged in, so"
					+
					" we're not starting a formal sync; but we will call onSyncFinished() with no results");
			onSyncFinished(mTrips.values());
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

	public boolean deepRefreshTrip(String key, boolean doSyncIfNotFound) {
		Trip trip = mTrips.get(key);

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

			Ui.getApplication(mContext).defaultTripComponents();
			mSyncTask = new SyncTask(Ui.getApplication(mContext).tripComponent().tripServices(),
				new ExpediaServices(mContext));
			mSyncTask.execute();
		}
	}

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

		// Used for determining whether to publish an "added" or "update" when we refresh a guest trip
		private Set<String> mGuestTripsNotYetLoaded = new HashSet<>();

		// Earlier versions of AsyncTask do not tell you when they are cancelled correctly.
		// This will let us know when the AsyncTask has fully completed its mission (even
		// if it was cancelled).
		private boolean mFinished = false;

		// These variables are used for stat tracking
		private Map<Operation, Integer> mOpCount = new HashMap<>();
		private int mTripsRefreshed = 0;
		private int mTripRefreshFailures = 0;
		private int mTripsAdded = 0;
		private int mTripsRemoved = 0;
		private int mFlightsUpdated = 0;

		SyncTask(TripsServicesInterface tripsServices, ExpediaServices legacyExpediaServices) {
			mServices = legacyExpediaServices;
			this.tripsServices = tripsServices;

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
			if (mTrips == null) {
				Log.i(LOGGING_TAG,
					"Sync called with mTrips == null. Loading trips from disk and generating itin cards before other operations in queue.");
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
					load();
					break;
				case REAUTH_FACEBOOK_USER:
					reauthFacebookUser();
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
					refreshAllTrips(new CurrentTime(), mTrips);
					break;
				case DEEP_REFRESH_TRIP:
					Trip trip = nextTask.mTrip;
					if (trip == null && !TextUtils.isEmpty(nextTask.mTripNumber)) {
						// Try to retrieve a trip here
						trip = mTrips.get(nextTask.mTripNumber);

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
					save();
					break;
				case GENERATE_ITIN_CARDS:
					generateItinCardData();
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
			return mTrips.values();
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
				"# Trips=" + (mTrips == null ? 0 : mTrips.size()) + "; # Added=" + mTripsAdded + "; # Removed="
					+ mTripsRemoved);
			Log.i(LOGGING_TAG, "# Refreshed=" + mTripsRefreshed + "; # Failed Refresh=" + mTripRefreshFailures);
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
				mTripsAdded++;
			}
			else {
				publishProgress(new ProgressUpdate(ProgressUpdate.Type.UPDATED, trip));
			}

			// POSSIBLE TODO: Only call tripUpated() when it's actually changed
		}

		private void reauthFacebookUser() {
			ServicesUtil.generateAccountService(mContext)
				.facebookReauth(mContext).doOnNext(new Consumer<FacebookLinkResponse>() {
				@Override
				public void accept(FacebookLinkResponse linkResponse) {
					if (linkResponse != null
						&& linkResponse.isSuccess()) {
						Log.w(LOGGING_TAG, "FB: Autologin success");
					}
				}
			});
		}

		void refreshAllTrips(TimeSource timeSource, Map<String, Trip> trips) {
			final List<Observable<JSONObject>> observables = new ArrayList<>();
			for (Trip trip : trips.values()) {
				if (timeSource.now() - REFRESH_TRIP_CUTOFF > trip.getLastCachedUpdateMillis()) {
					if (trip.isShared() && trip.hasExpired(CUTOFF_HOURS)) {
						Log.w(LOGGING_TAG, "REFRESH_ALL_TRIPS: Removing a shared trip because it is completed and past the cutoff.  tripNum=" + trip.getItineraryKey());

						Trip removeTrip = trips.remove(trip.getItineraryKey());
						publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, removeTrip));

						mTripsRemoved++;
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
			waitAndParseDetailResponses(observables, trips);
		}

		@NonNull
		private Function<Throwable, JSONObject> onErrorReturnNull() {
			return new Function<Throwable, JSONObject>() {
				@Override
				public JSONObject apply(Throwable throwable) {
					return null;
				}
			};
		}

		private void waitAndParseDetailResponses(List<Observable<JSONObject>> observables, final Map<String, Trip> trips) {
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
						if (response == null) {
							Log.i(LOGGING_TAG, "REFRESH_ALL_TRIPS: Response is null");
							refreshTripResponseNull(new Trip());
						}
						else {
							Trip updatedTrip = response.getTrip();
							String itineraryKey = updatedTrip.getItineraryKey();
							if (trips.containsKey(itineraryKey)) {
								Trip trip = trips.get(itineraryKey);
								if (response.hasErrors()) {
									Log.i(LOGGING_TAG, "REFRESH_ALL_TRIPS: Response has errors");
									refreshTripResponseHasErrors(trip, response);
								}
								else {
									Log.i(LOGGING_TAG, "REFRESH_ALL_TRIPS: Response is a success");
									refreshTripResponseSuccess(trip, false, response);
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
			if (!mTrips.containsKey(trip.getItineraryKey())) {
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

					Trip removeTrip = mTrips.remove(trip.getItineraryKey());
					publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, removeTrip));

					mTripsRemoved++;
					return;
				}

				TripDetailsResponse response;
				response = getTripDetailsResponse(trip, deepRefresh);

				if (response == null) {
					refreshTripResponseNull(trip);
				}
				else if (response.hasErrors()) {
					refreshTripResponseHasErrors(trip, response);
				}
				else {
					refreshTripResponseSuccess(trip, deepRefresh, response);
				}
			}
		}

		private void refreshTripResponseNull(Trip trip) {
			publishProgress(new ProgressUpdate(ProgressUpdate.Type.UPDATE_FAILED, trip));
			mTripRefreshFailures++;
		}

		private void refreshTripResponseHasErrors(Trip trip, TripDetailsResponse response) {
			Log.w(LOGGING_TAG, "Error updating trip " + trip.getItineraryKey() + ": " + response.gatherErrorMessage(mContext));

			// If it's a guest trip, and we've never retrieved info on it, it may be invalid.
			// As such, we should remove it (but don't remove a trip if it's ever been loaded
			// or it's not a guest trip).
			if (trip.isGuest() && trip.getLevelOfDetail() == LevelOfDetail.NONE) {
				if (response.getErrors().size() > 0) {
					Log.w(LOGGING_TAG, "Tried to load guest trip, but failed, so we're removing it.  Email="
						+ trip.getGuestEmailAddress() + " itinKey="
						+ trip.getItineraryKey());
					mTrips.remove(trip.getItineraryKey());
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

		private void refreshTripResponseSuccess(Trip trip, boolean deepRefresh, TripDetailsResponse response) {
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

				mTripsRefreshed++;

				if (trip.getAirAttach() != null && !trip.isShared()) {
					Db.getTripBucket().setAirAttach(trip.getAirAttach());
				}

				if (!(trip.getLevelOfDetail() == LevelOfDetail.SUMMARY_FALLBACK)) {
					mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP_FLIGHT_STATUS, trip));
					mSyncOpQueue.add(new Task(Operation.PUBLISH_TRIP_UPDATE, trip));
				}
			}
		}

		TripDetailsResponse getTripDetailsResponse(Trip trip, boolean deepRefresh) {
			TripDetailsResponse response;

			if (featureFlagForRetrofitServiceEnabled()) {
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
				response = (new TripDetailsResponseHandler()).handleJson(json);
			}
			else {
				if (trip.isShared()) {
					response = mServices.getSharedItin(trip.getShareInfo().getSharableDetailsApiUrl());
				}
				else {
					response = mServices.getTripDetails(trip, !deepRefresh);
				}
			}
			return response;
		}

		private boolean featureFlagForRetrofitServiceEnabled() {
			return SatelliteFeatureConfigManager
				.isFeatureEnabled(mContext, SatelliteFeatureConstants.ITINERARY_MANAGER_USE_RETROFIT_TRIP_DETAILS);
		}

		private boolean featureFlagForAsynchronousRefreshEnabled() {
			return SatelliteFeatureConfigManager.isFeatureEnabled(mContext,
				SatelliteFeatureConstants.ITINERARY_MANAGER_MAKE_ASYNCHRONOUS_CALLS_REFRESH_TRIPS);
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

				TripResponse response = mServices.getTrips(getCachedDetails);
				OmnitureTracking.trackItinTripRefreshCallMade();

				if (isCancelled()) {
					return;
				}

				if (response == null || response.hasErrors()) {
					String errorMessage = "Trip details response is null";
					if (response != null && response.hasErrors()) {
						errorMessage = response.gatherErrorMessage(mContext);
						for (ServerError serverError : response.getErrors()) {
							ServerError.ErrorCode errorCode = serverError.getErrorCode();
							if (errorCode == ServerError.ErrorCode.NOT_AUTHENTICATED) {
								logForcedLogoutToCrashlytics();
								userStateManager.signOut();
								break;
							}
						}
						Log.w(LOGGING_TAG, "Error updating trips: " + response.gatherErrorMessage(mContext));
					}
					publishProgress(new ProgressUpdate(SyncError.USER_LIST_REFRESH_FAILURE));
					OmnitureTracking.trackItinTripRefreshCallFailure(errorMessage);
				}
				else {
					Set<String> currentTrips = new HashSet<>(mTrips.keySet());

					boolean tripHasFlight = false;
					for (Trip trip : response.getTrips()) {
						if (BookingStatus.filterOut(trip.getBookingStatus())) {
							continue;
						}

						String tripKey = trip.getItineraryKey();

						LevelOfDetail lod = trip.getLevelOfDetail();
						boolean hasFullDetails = lod == LevelOfDetail.FULL || lod == LevelOfDetail.SUMMARY_FALLBACK;
						if (!mTrips.containsKey(tripKey)) {
							mTrips.put(tripKey, trip);

							publishProgress(new ProgressUpdate(ProgressUpdate.Type.ADDED, trip));

							mTripsAdded++;
						}
						else if (hasFullDetails) {
							mTrips.get(tripKey).updateFrom(trip);
						}

						if (hasFullDetails) {
							// If we have full details, mark this as recently updated so we don't
							// refresh it below
							trip.markUpdated(false, new CurrentTime());

							mTripsRefreshed++;
						}

						if (trip.getAirAttach() != null && !trip.isShared()) {
							Db.getTripBucket().setAirAttach(trip.getAirAttach());
						}

						currentTrips.remove(tripKey);
						if (trip.hasFlight()) {
							tripHasFlight = true;
						}
					}

					// Remove all trips that were not returned by the server (not including guest trips or imported shared trips)
					for (String tripNumber : currentTrips) {
						Trip trip = mTrips.get(tripNumber);
						if (!trip.isGuest() && !trip.isShared()) {
							trip = mTrips.remove(tripNumber);
							publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, trip));
							mTripsRemoved++;
						}
					}
					OmnitureTracking.trackItinTripRefreshCallSuccess(tripHasFlight);
				}
			}
		}

		// Add all trips to be updated, even ones that may not need to be refreshed
		// (so we can see if any of the ancillary data needs to be refreshed).
		private void gatherTrips() {
			Log.i(LOGGING_TAG, "Gathering " + mTrips.values().size() + " trips...");

			if (featureFlagForAsynchronousRefreshEnabled()) {
				Log.i(LOGGING_TAG, "====REFRESH_ALL_TRIPS====");
				mSyncOpQueue.add(new Task(Operation.REFRESH_ALL_TRIPS));
			}
			else {
				for (Trip trip : mTrips.values()) {
					mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP, trip));

					if (trip.isGuest() && trip.getLevelOfDetail() == LevelOfDetail.NONE) {
						mGuestTripsNotYetLoaded.add(trip.getTripNumber());
					}
				}
			}
		}

		private void deduplicateTrips() {
			Map<String, String> sharedTripsMap = new HashMap<>();
			Set<String> sharedTripsToRemove = new HashSet<>();

			// Collect all of the shared trips
			for (Trip trip : mTrips.values()) {
				if (trip.isShared()) {
					sharedTripsMap.put(trip.getTripComponents().get(0).getUniqueId(), trip.getItineraryKey());
				}
			}

			// Check each "regular" trip and see if it matches one of the shared trips
			for (Trip trip : mTrips.values()) {
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
				trip = mTrips.remove(key);
				publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, trip));
				mTripsRemoved++;
			}
		}

		private void removeTrip(String tripNumber) {
			Trip trip = mTrips.get(tripNumber);
			if (trip == null) {
				Log.w(LOGGING_TAG, "Tried to remove a tripNumber that doesn't exist: " + tripNumber);
			}
			else {
				Log.i(LOGGING_TAG, "Removing trip with # " + tripNumber);

				publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, trip));
				// Delete notifications if any.
				deletePendingNotification(trip);
				mTrips.remove(tripNumber);
				mTripsRemoved++;
			}
		}

		private void downloadSharedItinTrip(String shareableUrl) {
			Log.i(LOGGING_TAG, "Creating shared itin placeholder " + shareableUrl);
			// Create a placeholder trip for this shared itin so that the ItineraryManager knows to
			// notify the UI to show loading indicator.
			Trip trip = new Trip();
			trip.setIsShared(true);
			trip.getShareInfo().setSharableDetailsUrl(shareableUrl);
			mTrips.put(shareableUrl, trip);

			Log.i(LOGGING_TAG, "Fetching shared itin " + shareableUrl);
			// We need to replace the /m with /api to get the json response.
			String shareableAPIUrl = ItinShareInfo.convertSharableUrlToApiUrl(shareableUrl);
			TripDetailsResponse response;
			if (featureFlagForRetrofitServiceEnabled()) {
				JSONObject json = tripsServices.getSharedTripDetails(trip.getShareInfo().getSharableDetailsApiUrl());
				response = (new TripDetailsResponseHandler()).handleJson(json);
			}
			else {
				response = mServices.getSharedItin(shareableAPIUrl);
			}

			if (isCancelled()) {
				return;
			}

			if (response == null || response.hasErrors()) {
				if (response != null && response.hasErrors()) {
					Log.w(LOGGING_TAG, "Error fetching shared itin : " + response.gatherErrorMessage(mContext));
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
				if (!mTrips.containsKey(shareableUrl)) {
					mTrips.put(shareableUrl, sharedTrip);

					publishProgress(new ProgressUpdate(ProgressUpdate.Type.ADDED, sharedTrip));

					mTripsAdded++;
				}
				else if (hasFullDetails) {
					mTrips.get(shareableUrl).updateFrom(sharedTrip);
				}

				if (hasFullDetails) {
					// If we have full details, mark this as recently updated so we don't
					// refresh it below
					sharedTrip.markUpdated(false, new CurrentTime());

					mTripsRefreshed++;

					mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP_FLIGHT_STATUS, sharedTrip));
				}

				if (trip.getAirAttach() != null && !trip.isShared()) {
					Db.getTripBucket().setAirAttach(trip.getAirAttach());
				}

				// Note: In the future, we may be getting these parameters from the URL. Currently, we do not, thus we just
				// send the generic "AppShare" event any time that we import a shared itin. Ideally, the URL will contain
				// some more info pertaining to tracking and we'd send something like "AppShare.Facebook".
				OmnitureTracking.setDeepLinkTrackingParams("brandcid", "AppShare");
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

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();

			JSONUtils.putJSONableList(obj, "trips", mTrips.values());

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mTrips = new HashMap<>();
		List<Trip> trips = JSONUtils.getJSONableList(obj, "trips", Trip.class);
		for (Trip trip : trips) {
			mTrips.put(trip.getItineraryKey(), trip);
		}

		return true;
	}

	public static boolean haveTimelyItinItem() {
		List<DateTime> startTimes = sManager.getStartTimes();
		List<DateTime> endTimes = sManager.getEndTimes();
		return hasUpcomingOrInProgressTrip(startTimes, endTimes);
	}

	@VisibleForTesting
	static boolean hasUpcomingOrInProgressTrip(List<DateTime> startTimes, List<DateTime> endTimes) {
		if (startTimes != null && endTimes != null && startTimes.size() == endTimes.size()) {
			DateTime now = DateTime.now();
			DateTime oneWeekFromNow = now.plusWeeks(1);
			for (int i = 0; i < startTimes.size(); i++) {
				DateTime start = startTimes.get(i);
				DateTime end = endTimes.get(i);
				if (now.isBefore(end) && oneWeekFromNow.isAfter(start)) {
					return true;
				}
			}
		}
		return false;
	}

}
