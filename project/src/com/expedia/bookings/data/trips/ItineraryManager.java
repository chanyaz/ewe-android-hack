package com.expedia.bookings.data.trips;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;

import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.Trip.LevelOfDetail;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;
import com.mobiata.flightlib.data.Flight;

// Make sure to call init() before using in the app!
//
// In addition, make sure to call startSync() before manipulating data.
public class ItineraryManager implements JSONable {

	public static final String TRIP_REFRESH_BROADCAST = "com.expedia.bookings.data.trips.DEEP_REFRESH";
	public static final String TRIP_REFRESH_ARG_TRIP_ID = "tripId";

	private static final long UPDATE_CUTOFF = 1000 * 60; // At most once a minute

	private static final ItineraryManager sManager = new ItineraryManager();

	private ItineraryManager() {
		// Cannot be instantiated
	}

	public static ItineraryManager getInstance() {
		return sManager;
	}

	// Should be initialized from the Application so that this does not leak a component
	private Context mContext;

	// Don't try refreshing too often
	private long mLastUpdateTime;

	private Map<String, Trip> mTrips;

	// This is an in-memory representation of the trips.  It is not
	// saved, but rather reproduced from the trip list.  It updates
	// each time a sync occurs.
	//
	// It can be assumed that it is sorted at all times.
	private List<ItinCardData> mItinCardDatas = new ArrayList<ItinCardData>();

	// These are lists of all trip start and end times; unlike mTrips, they will be loaded at app startup, so you can use them to
	// determine whether you should launch in itin or not.
	private List<DateTime> mStartTimes = new ArrayList<DateTime>();
	private List<DateTime> mEndTimes = new ArrayList<DateTime>();

	/**
	 * Adds a guest trip to the itinerary list.
	 *
	 * Automatically starts to try to get info on the trip from the server.  If a sync is already
	 * in progress it will queue the guest trip for refresh; otherwise it will only refresh this
	 * single guest trip.
	 */
	public void addGuestTrip(String email, String tripNumber) {
		if (mTrips == null) {
			Log.w("ItineraryManager - Attempt to add guest trip, mTrips == null. Init");
			mTrips = new HashMap<String, Trip>();
		}

		Trip trip = new Trip(email, tripNumber);
		mTrips.put(tripNumber, trip);

		mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP, trip));
		mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));
		mSyncOpQueue.add(new Task(Operation.GENERATE_ITIN_CARDS));

		if (!isSyncing()) {
			mSyncTask = new SyncTask();
			mSyncTask.execute();
		}
	}

	public void removeGuestTrip(String tripNumber) {
		Trip trip = mTrips.get(tripNumber);

		if (trip == null) {
			Log.w("Tried to remove a guest tripNumber that doesn't exist: " + tripNumber);
		}
		else if (!trip.isGuest()) {
			Log.w("Tried to remove a non-guest trip, DENIED because only the ItinManager is allowed to do that: "
					+ tripNumber);
		}
		else {
			mTrips.remove(tripNumber);

			// Do not inform of removal if it was never valid (since we never informed of adding in the first place)
			if (trip.getLevelOfDetail() != LevelOfDetail.NONE) {
				onTripRemoved(trip);
				deletePendingNotification(trip);
			}
		}
	}

	/**
	 * Get a list of the current Trips.
	 *
	 * Be warned: these Trips will be updated from a sync
	 * operation.  If this behavior seems wrong, talk with
	 * DLew since he's open to the idea of changing this
	 * behavior.
	 */
	public Collection<Trip> getTrips() {
		if (mTrips != null) {
			return mTrips.values();
		}

		return null;
	}

	public List<ItinCardData> getItinCardData() {
		return mItinCardDatas;
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
		Log.i("Clearing all data from ItineraryManager...");

		// Delete the file, so it can't be reloaded later
		File file = mContext.getFileStreamPath(MANAGER_PATH);
		if (file.exists()) {
			file.delete();
		}

		mStartTimes.clear();
		mEndTimes.clear();

		mLastUpdateTime = 0;

		mItinCardDatas.clear();

		if (mTrips == null) {
			return;
		}

		Log.d("Informing the removal of " + mTrips.size() + " trips due to clearing of ItineraryManager...");

		for (Trip trip : mTrips.values()) {
			onTripRemoved(trip);
		}

		mTrips.clear();
	}

	//////////////////////////////////////////////////////////////////////////
	// Data

	private static final String MANAGER_PATH = "itin-manager.dat";

	private static final String MANAGER_START_END_TIMES_PATH = "itin-starts-and-ends.dat";

	/**
	 * Must be called before using ItineraryManager for the first time.
	 *
	 * I expect this to be called from the Application.  That way the
	 * context won't leak.
	 */
	public void init(Context context) {
		long start = System.nanoTime();

		mContext = context;

		loadStartAndEndTimes();

		Log.d("Initialized ItineraryManager in " + ((System.nanoTime() - start) / 1000000) + " ms");
	}

	private void save() {
		Log.i("Saving ItineraryManager data...");

		saveStartAndEndTimes();

		try {
			IoUtils.writeStringToFile(MANAGER_PATH, toJson().toString(), mContext);
		}
		catch (IOException e) {
			Log.w("Could not save ItineraryManager data", e);
		}
	}

	private void load() {
		if (mTrips == null) {
			File file = mContext.getFileStreamPath(MANAGER_PATH);
			if (file.exists()) {
				try {
					JSONObject obj = new JSONObject(IoUtils.readStringFromFile(MANAGER_PATH, mContext));
					fromJson(obj);
					Log.i("Loaded " + mTrips.size() + " trips from disk.");
				}
				catch (Exception e) {
					Log.w("Could not load ItineraryManager data, starting from scratch again...", e);
					file.delete();
				}
			}
		}

		if (mTrips == null) {
			mTrips = new HashMap<String, Trip>();

			Log.i("Starting a fresh set of itineraries.");
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Start times data

	public List<DateTime> getStartTimes() {
		return mStartTimes;
	}

	public List<DateTime> getEndTimes() {
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
					DateTime fakeEnd = new DateTime(0, 0);
					mEndTimes.add(fakeEnd);
				}
			}
		}

		try {
			// Save to disk
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONableList(obj, "startTimes", mStartTimes);
			JSONUtils.putJSONableList(obj, "endTimes", mEndTimes);
			IoUtils.writeStringToFile(MANAGER_START_END_TIMES_PATH, obj.toString(), mContext);
		}
		catch (Exception e) {
			Log.w("Could not save start and end times", e);
		}
	}

	private void loadStartAndEndTimes() {
		Log.d("Loading start and end times...");

		File file = mContext.getFileStreamPath(MANAGER_START_END_TIMES_PATH);
		if (file.exists()) {
			try {
				JSONObject obj = new JSONObject(IoUtils.readStringFromFile(MANAGER_START_END_TIMES_PATH, mContext));
				mStartTimes = JSONUtils.getJSONableList(obj, "startTimes", DateTime.class);
				mEndTimes = JSONUtils.getJSONableList(obj, "endTimes", DateTime.class);
			}
			catch (Exception e) {
				Log.w("Could not load start times", e);
				file.delete();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Itin card data

	private static final int CUTOFF_HOURS = 48;

	private void generateItinCardData() {
		mItinCardDatas.clear();

		Calendar pastCutoffCal = Calendar.getInstance();
		pastCutoffCal.add(Calendar.HOUR_OF_DAY, -CUTOFF_HOURS);
		for (Trip trip : mTrips.values()) {
			if (trip.getTripComponents() != null) {
				List<TripComponent> components = trip.getTripComponents(true);
				for (TripComponent comp : components) {
					List<ItinCardData> items = ItinCardDataFactory.generateCardData(comp);
					if (items != null) {
						for (ItinCardData item : items) {
							if (item.getEndDate() != null && item.getEndDate().getCalendar() != null
									&& item.getEndDate().getCalendar().compareTo(pastCutoffCal) >= 0) {
								mItinCardDatas.add(item);
							}
						}
					}
				}
			}
		}

		Collections.sort(mItinCardDatas, mItinCardDataComparator);
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
		return date.getMillisFromEpoch() + date.getTzOffsetMillis();
	}

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
		 *
		 * Note: Guest trips will not have this called right when you add them
		 * (because they have no meaningful info at that point, and may not
		 * even be a valid trip).
		 */
		public void onTripAdded(Trip trip);

		/**
		 * Each Trip that is updated during a sync gets its own callback
		 * so that you can update the UI before the entire sync process
		 * is complete.
		 *
		 * Not all Trips may get an updated trip call (e.g., a trip doesn't
		 * need an update because it was just updated a few minutes ago).
		 */
		public void onTripUpdated(Trip trip);

		/**
		 * Notification when a trip failed to update
		 *
		 * This can be particularly useful to know when a guest trip that
		 * was added can't be updated at all.
		 *
		 * POSSIBLE TODO: info on why the update failed?
		 */
		public void onTripUpdateFailed(Trip trip);

		/**
		 * Notification for when a Trip has been removed, either automatically
		 * from a logged in user account or manually for guest trips.
		 */
		public void onTripRemoved(Trip trip);

		/**
		 * Notification if sync itself has a failure.  There can be multiple
		 * failures during the sync process.  onSyncFinished() will still
		 * be called at the end.
		 */
		public void onSyncFailure(SyncError error);

		/**
		 * Once the sync process is done it returns the list of Trips as
		 * it thinks exists.  Returns all trips currently in the ItineraryManager.
		 */
		public void onSyncFinished(Collection<Trip> trips);
	}

	// Makes it so you don't have to implement everything from the interface
	public static class ItinerarySyncAdapter implements ItinerarySyncListener {
		public void onTripAdded(Trip trip) {
		}

		public void onTripUpdated(Trip trip) {
		}

		public void onTripUpdateFailed(Trip trip) {
		}

		public void onTripRemoved(Trip trip) {
		}

		public void onSyncFailure(SyncError error) {
		}

		public void onSyncFinished(Collection<Trip> trips) {
		}
	}

	private Set<ItinerarySyncListener> mSyncListeners = new HashSet<ItineraryManager.ItinerarySyncListener>();

	public void addSyncListener(ItinerarySyncListener listener) {
		mSyncListeners.add(listener);
	}

	public void removeSyncListener(ItinerarySyncListener listener) {
		mSyncListeners.remove(listener);
	}

	private void onTripAdded(Trip trip) {
		Set<ItinerarySyncListener> listeners = new HashSet<ItineraryManager.ItinerarySyncListener>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onTripAdded(trip);
		}
	}

	private void onTripUpdated(Trip trip) {
		Set<ItinerarySyncListener> listeners = new HashSet<ItineraryManager.ItinerarySyncListener>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onTripUpdated(trip);
		}
	}

	private void onTripUpdateFailed(Trip trip) {
		Set<ItinerarySyncListener> listeners = new HashSet<ItineraryManager.ItinerarySyncListener>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onTripUpdateFailed(trip);
		}
	}

	private void onTripRemoved(Trip trip) {
		Set<ItinerarySyncListener> listeners = new HashSet<ItineraryManager.ItinerarySyncListener>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onTripRemoved(trip);
		}
	}

	private void onSyncFailed(SyncError error) {
		Set<ItinerarySyncListener> listeners = new HashSet<ItineraryManager.ItinerarySyncListener>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onSyncFailure(error);
		}
	}

	private void onSyncFinished(Collection<Trip> trips) {
		Set<ItinerarySyncListener> listeners = new HashSet<ItineraryManager.ItinerarySyncListener>(mSyncListeners);
		for (ItinerarySyncListener listener : listeners) {
			listener.onSyncFinished(trips);
		}

		// Only remember the last update time if there was something actually updated;
		// either the user was logged in (but had no trips) or there are guest trips
		// present.
		if (User.isLoggedIn(mContext) || (trips != null && trips.size() > 0)) {
			mLastUpdateTime = Calendar.getInstance().getTimeInMillis();
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

	// The order of operations in this enum determines the priorities of each item;
	// the looper will pick the highest order operation to execute next.
	private enum Operation {
		LOAD_FROM_DISK, // Loads saved trips from disk, if we're just starting up
		REFRESH_USER, // If logged in, refreshes the trip list of the user
		GATHER_TRIPS, // Enqueues all trips for later operation

		// Refresh ancillary parts of a trip; these are higher priority so that they're
		// completed after each trip is refreshed (for lazy loading purposes)
		REFRESH_TRIP_FLIGHT_STATUS, // Refreshes trip statuses on trip
		PUBLISH_TRIP_UPDATE, // Publishes that we've updated a trip

		// Refreshes trip
		DEEP_REFRESH_TRIP, // Refreshes a trip (deep)
		REFRESH_TRIP, // Refreshes a trip

		SAVE_TO_DISK, // Saves state of ItineraryManager to disk

		GENERATE_ITIN_CARDS, // Generates itin card data for use
	}

	private class Task implements Comparable<Task> {
		Operation mOp;

		Trip mTrip;

		public Task(Operation op) {
			this(op, null);
		}

		public Task(Operation op, Trip trip) {
			mOp = op;
			mTrip = trip;
		}

		@Override
		public int compareTo(Task another) {
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
	}

	// Priority queue that doesn't allow duplicates to be added
	@SuppressWarnings("serial")
	private static class TaskPriorityQueue extends PriorityQueue<Task> {
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

	private static final long REFRESH_TRIP_CUTOFF = 1000 * 60 * 15; // 15 minutes

	private static final long MINUTE = DateUtils.MINUTE_IN_MILLIS;
	private static final long HOUR = DateUtils.HOUR_IN_MILLIS;

	/**
	 * Start a sync operation.
	 *
	 * If a sync is already in progress then calls to this are ignored.
	 *
	 * @return true if the sync started or is in progress, false if it never started
	 */
	public boolean startSync(boolean forceRefresh) {
		if (!forceRefresh && Calendar.getInstance().getTimeInMillis() < UPDATE_CUTOFF + mLastUpdateTime) {
			Log.d("ItineraryManager sync started too soon since last one; ignoring.");
			return false;
		}
		else if (mTrips != null && mTrips.size() == 0 && !User.isLoggedIn(mContext)) {
			Log.d("ItineraryManager sync called, but there are no guest trips and the user is not logged in, so" +
					" we're not starting a formal sync; but we will call onSyncFinished() with no results");
			onSyncFinished(mTrips.values());
			return false;
		}
		else if (isSyncing()) {
			Log.d("Tried to start a sync while one is already in progress.");
			return true;
		}
		else {
			Log.i("Starting an ItineraryManager sync...");

			// Add default sync operations
			mSyncOpQueue.add(new Task(Operation.LOAD_FROM_DISK));
			mSyncOpQueue.add(new Task(Operation.REFRESH_USER));
			mSyncOpQueue.add(new Task(Operation.GATHER_TRIPS));
			mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));
			mSyncOpQueue.add(new Task(Operation.GENERATE_ITIN_CARDS));

			mSyncTask = new SyncTask();
			mSyncTask.execute();

			return true;
		}
	}

	private static final long DEEP_REFRESH_RATE_LIMIT = 1000 * 60 * 1;

	public boolean deepRefreshTrip(Trip trip) {
		trip = mTrips.get(trip.getTripNumber());

		if (trip == null) {
			Log.w("Tried to deep refresh a trip which doesn't exist.");
			return false;
		}
		else {
			mSyncOpQueue.add(new Task(Operation.DEEP_REFRESH_TRIP, trip));
			mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));
			mSyncOpQueue.add(new Task(Operation.GENERATE_ITIN_CARDS));

			if (!isSyncing()) {
				mSyncTask = new SyncTask();
				mSyncTask.execute();
			}
			return true;
		}
	}

	public static void broadcastTripRefresh(Context context, Trip trip) {
		Log.d("ItineraryManager - Broacasting TRIP_REFRESH");
		Intent intent = new Intent(TRIP_REFRESH_BROADCAST);
		intent.putExtra(TRIP_REFRESH_ARG_TRIP_ID, trip.getTripId());
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	public boolean isSyncing() {
		return mSyncTask != null && mSyncTask.getStatus() != AsyncTask.Status.FINISHED && !mSyncTask.finished();
	}

	private class SyncTask extends AsyncTask<Void, ProgressUpdate, Collection<Trip>> {

		/*
		 * Implementation note - we regularly check if the sync has been
		 * cancelled (after every service call).  If it has been cancelled,
		 * then we exit out quickly.  If you add any service calls, also
		 * check afterwards (since the service calls are where the app
		 * will get hung up during a cancel).
		 */
		private ExpediaServices mServices;

		// If we have not yet loaded itineraries from disk, then we skip whatever the system
		// requested and do a "quick sync" instead.  A quick sync only loads what was on the disk
		// before and then organizes the data.  It then calls a normal, full sync, which
		// will actually do a refresh in the background.
		private boolean mQuickSync = false;

		// Used for determining whether to publish an "added" or "update" when we refresh a guest trip
		private Set<String> mGuestTripsNotYetLoaded = new HashSet<String>();

		// Earlier versions of AsyncTask do not tell you when they are cancelled correctly.
		// This will let us know when the AsyncTask has fully completed its mission (even
		// if it was cancelled).
		private boolean mFinished = false;

		// These variables are used for stat tracking
		private Map<Operation, Integer> mOpCount = new HashMap<ItineraryManager.Operation, Integer>();
		private int mTripsRefreshed = 0;
		private int mTripRefreshFailures = 0;
		private int mTripsAdded = 0;
		private int mTripsRemoved = 0;
		private int mFlightsUpdated = 0;

		public SyncTask() {
			mServices = new ExpediaServices(mContext);

			for (Operation op : Operation.values()) {
				mOpCount.put(op, 0);
			}
		}

		@Override
		protected void onPreExecute() {
			mQuickSync = mTrips == null;
		}

		@Override
		protected Collection<Trip> doInBackground(Void... params) {
			while (!mSyncOpQueue.isEmpty()) {
				Task nextTask = mSyncOpQueue.remove();
				Operation op = nextTask.mOp;

				// If we're doing a quick sync (aka, just loading data from disk), skip most operations
				if (mQuickSync && !(op == Operation.LOAD_FROM_DISK || op == Operation.GENERATE_ITIN_CARDS)) {
					continue;
				}

				switch (nextTask.mOp) {
				case LOAD_FROM_DISK:
					load();
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
				case DEEP_REFRESH_TRIP:
					refreshTrip(nextTask.mTrip, true);
					break;
				case REFRESH_TRIP:
					refreshTrip(nextTask.mTrip, false);
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

			scheduleLocalNotifications(mTrips.values());

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
			case REMOVED:
				onTripRemoved(update.mTrip);
				break;
			case SYNC_ERROR:
				onSyncFailed(update.mError);
				break;
			}
		}

		@Override
		protected void onPostExecute(Collection<Trip> trips) {
			super.onPostExecute(trips);

			onSyncFinished(trips);

			logStats();

			if (mQuickSync) {
				mFinished = true;
				startSync(true);
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
		public void cancelDownloads() {
			mServices.onCancel();
		}

		public boolean finished() {
			return mFinished;
		}

		private void logStats() {
			Log.d("Sync Finished; stats below.");
			for (Operation op : Operation.values()) {
				Log.d(op.name() + ": " + mOpCount.get(op));
			}

			Log.i("# Trips=" + mTrips.size() + "; # Added=" + mTripsAdded + "; # Removed=" + mTripsRemoved);
			Log.i("# Refreshed=" + mTripsRefreshed + "; # Failed Refresh=" + mTripRefreshFailures);
			Log.i("# Flights Updated=" + mFlightsUpdated);
		}

		//////////////////////////////////////////////////////////////////////
		// Operations

		private void updateFlightStatuses(Trip trip) {
			long now = Calendar.getInstance().getTimeInMillis();

			for (TripComponent tripComponent : trip.getTripComponents(true)) {
				if (tripComponent.getType() == Type.FLIGHT) {
					TripFlight tripFlight = (TripFlight) tripComponent;
					FlightTrip flightTrip = tripFlight.getFlightTrip();
					for (int i = 0; i < flightTrip.getLegCount(); i++) {
						FlightLeg fl = flightTrip.getLeg(i);

						for (Flight segment : fl.getSegments()) {
							long takeOff = segment.mOrigin.getMostRelevantDateTime().getTimeInMillis();
							long landing = segment.getArrivalWaypoint().getMostRelevantDateTime().getTimeInMillis();
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
									if ((timeToTakeOff < HOUR * 12 && timeSinceLastUpdate > 5 * MINUTE)
											|| (timeToTakeOff < HOUR * 24 && timeSinceLastUpdate > HOUR)
											|| (timeToTakeOff < HOUR * 72 && timeSinceLastUpdate > 12 * HOUR)) {
										update = true;
									}
								}
								else if (now < landing && timeSinceLastUpdate > 5 * MINUTE) {
									update = true;
								}
								else if (now > landing) {
									if (now < (landing + (7 * DateUtils.DAY_IN_MILLIS))
											&& timeSinceLastUpdate > (now - (landing + DateUtils.HOUR_IN_MILLIS))
											&& timeSinceLastUpdate > 5 * MINUTE) {
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

		private void refreshTrip(Trip trip, boolean deepRefresh) {
			// It's possible for a trip to be removed during refresh (if it ends up being canceled
			// during the refresh).  If it's been somehow queued for multiple refreshes (e.g.,
			// deep refresh called during a sync) then we want to skip trying to refresh it twice.
			if (!mTrips.containsKey(trip.getTripNumber())) {
				return;
			}

			boolean gatherAncillaryData = true;

			// Only update if we are outside the cutoff
			long now = Calendar.getInstance().getTimeInMillis();
			if (now - REFRESH_TRIP_CUTOFF > trip.getLastCachedUpdateMillis() || deepRefresh) {
				// Limit the user to one deep refresh per DEEP_REFRESH_RATE_LIMIT. Use cache refresh if user attempts to
				// deep refresh within the limit.
				if (now - trip.getLastFullUpdateMillis() < DEEP_REFRESH_RATE_LIMIT) {
					deepRefresh = false;
				}

				TripDetailsResponse response = mServices.getTripDetails(trip, !deepRefresh);

				if (response == null || response.hasErrors()) {
					if (response != null && response.hasErrors()) {
						Log.w("Error updating trip " + trip.getTripNumber() + ": "
								+ response.gatherErrorMessage(mContext));

						// If it's a guest trip, and we've never retrieved info on it, it may be invalid.
						// As such, we should remove it (but don't remove a trip if it's ever been loaded
						// or it's not a guest trip).
						if (trip.isGuest() && trip.getLevelOfDetail() == LevelOfDetail.NONE) {
							for (ServerError error : response.getErrors()) {
								if (error.getErrorCode() == ServerError.ErrorCode.INVALID_INPUT) {
									mTrips.remove(trip.getTripNumber());
								}
							}
						}
					}

					publishProgress(new ProgressUpdate(ProgressUpdate.Type.UPDATE_FAILED, trip));

					gatherAncillaryData = false;

					mTripRefreshFailures++;
				}
				else {
					Trip updatedTrip = response.getTrip();

					if (BookingStatus.filterOut(updatedTrip.getBookingStatus())) {
						gatherAncillaryData = false;

						Trip removeTrip = mTrips.remove(updatedTrip.getTripNumber());
						publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, removeTrip));

						mTripsRemoved++;
					}
					else {
						// Update trip
						trip.updateFrom(updatedTrip);
						trip.markUpdated(deepRefresh);

						mTripsRefreshed++;
					}
				}
			}

			// We don't want to try to gather ancillary data if we don't have any data on the trips themselves
			if (trip.getLevelOfDetail() == LevelOfDetail.SUMMARY_FALLBACK) {
				gatherAncillaryData = false;
			}

			if (gatherAncillaryData) {
				mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP_FLIGHT_STATUS, trip));
				mSyncOpQueue.add(new Task(Operation.PUBLISH_TRIP_UPDATE, trip));
			}
		}

		// If the user is logged in, retrieve a listing of current trips for logged in user
		private void refreshUserList() {
			if (!User.isLoggedIn(mContext)) {
				Log.d("User is not logged in, not refreshing user list.");
			}
			else {
				// We only want to get the first N cached details if it's been more than
				// REFRESH_TRIP_CUTOFF since the last refresh.  If we've refreshed more
				// recently, then we only want to update individual trips as is necessary
				// (so that the summary call goes out quickly).
				boolean getCachedDetails = Calendar.getInstance().getTimeInMillis() - REFRESH_TRIP_CUTOFF > mLastUpdateTime;

				Log.d("User is logged in, refreshing the user list.  Using cached details call: " + getCachedDetails);

				TripResponse response = mServices.getTrips(getCachedDetails, 0);

				if (isCancelled()) {
					return;
				}

				if (response == null || response.hasErrors()) {
					if (response != null && response.hasErrors()) {
						Log.w("Error updating trips: " + response.gatherErrorMessage(mContext));
					}

					publishProgress(new ProgressUpdate(SyncError.USER_LIST_REFRESH_FAILURE));
				}
				else {
					Set<String> currentTrips = new HashSet<String>(mTrips.keySet());

					for (Trip trip : response.getTrips()) {
						if (BookingStatus.filterOut(trip.getBookingStatus())) {
							continue;
						}

						String tripNumber = trip.getTripNumber();

						LevelOfDetail lod = trip.getLevelOfDetail();
						boolean hasFullDetails = lod == LevelOfDetail.FULL || lod == LevelOfDetail.SUMMARY_FALLBACK;
						if (!mTrips.containsKey(tripNumber)) {
							mTrips.put(tripNumber, trip);

							publishProgress(new ProgressUpdate(ProgressUpdate.Type.ADDED, trip));

							mTripsAdded++;
						}
						else if (hasFullDetails) {
							mTrips.get(tripNumber).updateFrom(trip);
						}

						if (hasFullDetails) {
							// If we have full details, mark this as recently updated so we don't
							// refresh it below
							trip.markUpdated(false);

							mTripsRefreshed++;
						}

						currentTrips.remove(tripNumber);
					}

					// Remove all trips that were not returned by the server (not including guest trips)
					for (String tripNumber : currentTrips) {
						if (!mTrips.get(tripNumber).isGuest()) {
							Trip trip = mTrips.remove(tripNumber);
							publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, trip));
							mTripsRemoved++;
						}
					}
				}
			}
		}

		// Add all trips to be updated, even ones that may not need to be refreshed
		// (so we can see if any of the ancillary data needs to be refreshed).
		private void gatherTrips() {
			Log.i("Gathering " + mTrips.values().size() + " trips...");

			for (Trip trip : mTrips.values()) {
				mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP, trip));

				if (trip.isGuest() && trip.getLevelOfDetail() == LevelOfDetail.NONE) {
					mGuestTripsNotYetLoaded.add(trip.getTripNumber());
				}
			}
		}
	}

	private static class ProgressUpdate {
		public static enum Type {
			ADDED,
			UPDATED,
			UPDATE_FAILED,
			REMOVED,
			SYNC_ERROR,
		}

		public Type mType;
		public Trip mTrip;

		public SyncError mError;

		public ProgressUpdate(Type type, Trip trip) {
			mType = type;
			mTrip = trip;
		}

		public ProgressUpdate(SyncError error) {
			mType = Type.SYNC_ERROR;
			mError = error;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Local Notifications

	private void scheduleLocalNotifications(Collection<Trip> trips) {
		for (Trip trip : trips) {
			List<TripComponent> components = trip.getTripComponents(true);
			if (components == null) {
				continue;
			}
			for (TripComponent tc : components) {
				List<ItinCardData> list = ItinCardDataFactory.generateCardData(tc);
				if (list == null) {
					continue;
				}
				for (ItinCardData data : list) {
					ItinContentGenerator<? extends ItinCardData> generator = ItinContentGenerator.createGenerator(
							mContext, data);

					List<Notification> notifications = generator.generateNotifications();
					if (notifications == null) {
						continue;
					}

					for (Notification notification : notifications) {
						Notification existing = Notification.find(notification.getUniqueId());
						if (existing == null) {
							notification.save();
						}
						else {
							existing.updateFrom(notification);
							existing.save();
						}
					}
				}
			}
		}

		Notification.scheduleAll(mContext);
	}

	private void deletePendingNotification(Trip trip) {
		List<TripComponent> components = trip.getTripComponents(true);
		if (components == null) {
			return;
		}
		for (TripComponent tc : components) {
			List<ItinCardData> list = ItinCardDataFactory.generateCardData(tc);
			if (list == null) {
				continue;
			}
			for (ItinCardData data : list) {
				Notification notification = Notification.find(data.getId());
				if (notification != null) {
					notification.delete();
				}
			}
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
		mTrips = new HashMap<String, Trip>();
		List<Trip> trips = JSONUtils.getJSONableList(obj, "trips", Trip.class);
		for (Trip trip : trips) {
			mTrips.put(trip.getTripNumber(), trip);
		}

		return true;
	}
}
