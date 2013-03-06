package com.expedia.bookings.data.trips;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightStatsFlightResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.Trip.LevelOfDetail;
import com.expedia.bookings.server.ExpediaServices;
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

	private static final ItineraryManager sManager = new ItineraryManager();

	private ItineraryManager() {
		// Cannot be instantiated
	}

	public static ItineraryManager getInstance() {
		return sManager;
	}

	// Should be initialized from the Application so that this does not leak a component
	private Context mContext;

	private Map<String, Trip> mTrips;

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
		Trip trip = new Trip(email, tripNumber);
		mTrips.put(tripNumber, trip);

		mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP, trip));
		mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));

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
				}
				catch (Exception e) {
					Log.w("Could not load ItineraryManager data, starting from scratch again...", e);
					file.delete();
				}
			}
		}

		if (mTrips == null) {
			mTrips = new HashMap<String, Trip>();
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
		Log.d("Syncing/saving start times...");

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
		REFRESH_TRIP_IMAGES, // Refreshes images related to a trip
		REFRESH_TRIP_FLIGHT_STATUS, // Refreshes trip statuses on trip
		PUBLISH_TRIP_UPDATE, // Publishes that we've updated a trip

		// Refreshes trip
		DEEP_REFRESH_TRIP, // Refreshes a trip (deep)
		REFRESH_TRIP, // Refreshes a trip

		SAVE_TO_DISK, // Saves state of ItineraryManager to disk
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

	// TODO: Figure out better values for this
	private static final long UPDATE_TRIP_CACHED_CUTOFF = 1000 * 60 * 60 * 24; // 1 day

	private static final long MINUTE = DateUtils.MINUTE_IN_MILLIS;
	private static final long HOUR = DateUtils.HOUR_IN_MILLIS;

	/**
	 * Start a sync operation.
	 * 
	 * If a sync is already in progress then calls to this are ignored.
	 */
	public void startSync() {
		if (!isSyncing()) {
			// Add default sync operations
			mSyncOpQueue.add(new Task(Operation.LOAD_FROM_DISK));
			mSyncOpQueue.add(new Task(Operation.REFRESH_USER));
			mSyncOpQueue.add(new Task(Operation.GATHER_TRIPS));
			mSyncOpQueue.add(new Task(Operation.SAVE_TO_DISK));

			mSyncTask = new SyncTask();
			mSyncTask.execute();
		}
		else {
			Log.i("Tried to start a sync while one is already in progress.");
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
		return mSyncTask != null && mSyncTask.getStatus() != AsyncTask.Status.FINISHED;
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

		// Used for determining whether to publish an "added" or "update" when we refresh a guest trip
		private Set<String> mGuestTripsNotYetLoaded = new HashSet<String>();

		// These variables are used for stat tracking
		private Map<Operation, Integer> mOpCount = new HashMap<ItineraryManager.Operation, Integer>();
		private int mRefreshedTrips = 0;
		private int mFailedTripRefreshes = 0;
		private int mImagesGrabbed = 0;
		private int mFlightsUpdated = 0;

		public SyncTask() {
			mServices = new ExpediaServices(mContext);

			for (Operation op : Operation.values()) {
				mOpCount.put(op, 0);
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
				case REFRESH_USER:
					refreshUserList();
					break;
				case GATHER_TRIPS:
					gatherTrips();
					break;
				case REFRESH_TRIP_IMAGES:
					updateTripImages(nextTask.mTrip);
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
		}

		@SuppressLint("NewApi")
		@Override
		protected void onCancelled(Collection<Trip> result) {
			super.onCancelled(result);

			// Currently, the only reason we are canceled is if
			// the user signs out mid-update.  So continue
			// the signout in that case.
			doClearData();

			onSyncFailed(SyncError.CANCELLED);

			onSyncFinished(null);

			logStats();
		}

		// Should be called in addition to cancel(boolean), in order
		// to cancel the update mid-download
		public void cancelDownloads() {
			mServices.onCancel();
		}

		private void logStats() {
			Log.i("Sync Finished; stats below.");
			for (Operation op : Operation.values()) {
				Log.i(op.name() + ": " + mOpCount.get(op));
			}

			Log.i("Refreshed trips=" + mRefreshedTrips + " failed trip refreshes=" + mFailedTripRefreshes
					+ " image urls grabbed=" + mImagesGrabbed + " flightstats updates=" + mFlightsUpdated);
		}

		//////////////////////////////////////////////////////////////////////
		// Operations

		private void updateTripImages(Trip trip) {
			// Look for images.  For now, do not update if we already have images (they will remain static)
			for (TripComponent tripComponent : trip.getTripComponents()) {
				if (tripComponent.getType().equals(TripComponent.Type.FLIGHT)) {
					TripFlight tripFlight = (TripFlight) tripComponent;
					FlightTrip flightTrip = tripFlight.getFlightTrip();
					for (int i = 0; i < flightTrip.getLegCount(); i++) {
						if (tripFlight.getLegDestinationImageUrl(i) == null) {
							BackgroundImageResponse imageResponse = mServices.getFlightsBackgroundImage(
									flightTrip.getLeg(i).getLastWaypoint().mAirportCode, 0, 0);

							if (isCancelled()) {
								return;
							}

							if (imageResponse != null) {
								tripFlight.setLegDestinationImageUrl(i, imageResponse.getImageUrl());

								mImagesGrabbed++;
							}
							else {
								tripFlight.setLegDestinationImageUrl(i, "");
							}
						}
					}
				}
				else if (tripComponent.getType().equals(TripComponent.Type.CAR)) {
					TripCar tripCar = (TripCar) tripComponent;
					Car.Category category = tripCar.getCar().getCategory();

					if (category != null && TextUtils.isEmpty(tripCar.getCarCategoryImageUrl())) {
						BackgroundImageResponse imageResponse = mServices.getCarsBackgroundImage(tripCar
								.getCar().getCategory(), 0, 0);

						if (isCancelled()) {
							return;
						}

						if (imageResponse != null) {
							tripCar.setCarCategoryImageUrl(imageResponse.getImageUrl());

							mImagesGrabbed++;
						}
					}
				}
			}
		}

		private void updateFlightStatuses(Trip trip) {
			long now = Calendar.getInstance().getTimeInMillis();

			for (TripComponent tripComponent : trip.getTripComponents()) {
				if (tripComponent.getType().equals(TripComponent.Type.FLIGHT)) {
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
								if (now < (landing + (7 * DateUtils.DAY_IN_MILLIS)) && timeSinceLastUpdate > (now - landing)) {
									update = true;
								}
								else {
									segment.mStatusCode = Flight.STATUS_LANDED;
								}
							}

							if (update) {
								FlightStatsFlightResponse updatedFlightResponse = mServices.getUpdatedFlight(segment);

								if (isCancelled()) {
									return;
								}

								if (updatedFlightResponse != null) {
									segment.updateFrom(updatedFlightResponse.getFlight());
								}

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
			}
			else {
				publishProgress(new ProgressUpdate(ProgressUpdate.Type.UPDATED, trip));
			}

			// POSSIBLE TODO: Only call tripUpated() when it's actually changed
		}

		private void refreshTrip(Trip trip, boolean deepRefresh) {
			boolean gatherAncillaryData = true;

			// Only update if we are outside the cutoff
			long now = Calendar.getInstance().getTimeInMillis();
			if (now - UPDATE_TRIP_CACHED_CUTOFF > trip.getLastCachedUpdateMillis() || deepRefresh) {
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

						for (ServerError error : response.getErrors()) {
							if (error.getErrorCode() == ServerError.ErrorCode.INVALID_INPUT) {
								mTrips.remove(trip.getTripNumber());
							}
						}
					}

					publishProgress(new ProgressUpdate(ProgressUpdate.Type.UPDATE_FAILED, trip));

					gatherAncillaryData = false;

					mFailedTripRefreshes++;
				}
				else {
					Trip updatedTrip = response.getTrip();

					// Update trip
					trip.updateFrom(updatedTrip);
					trip.markUpdated(deepRefresh);

					mRefreshedTrips++;
				}
			}

			if (gatherAncillaryData) {
				mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP_IMAGES, trip));
				mSyncOpQueue.add(new Task(Operation.REFRESH_TRIP_FLIGHT_STATUS, trip));
				mSyncOpQueue.add(new Task(Operation.PUBLISH_TRIP_UPDATE, trip));
			}
		}

		// If the user is logged in, retrieve a listing of current trips for logged in user
		private void refreshUserList() {
			if (User.isLoggedIn(mContext)) {
				// First, determine if we've ever loaded trips for this user; if not, then we
				// should do a cached call for the first 5 detailed trips (for speedz).
				boolean getCachedDetails = true;
				for (Trip trip : mTrips.values()) {
					if (!trip.isGuest()) {
						getCachedDetails = false;
						break;
					}
				}

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
						String tripNumber = trip.getTripNumber();

						boolean hasFullDetails = trip.getLevelOfDetail() == LevelOfDetail.FULL;
						if (!mTrips.containsKey(tripNumber)) {
							mTrips.put(tripNumber, trip);

							publishProgress(new ProgressUpdate(ProgressUpdate.Type.ADDED, trip));
						}
						else if (hasFullDetails) {
							mTrips.get(tripNumber).updateFrom(trip);
						}

						if (hasFullDetails) {
							// If we have full details, mark this as recently updated so we don't
							// refresh it below
							trip.markUpdated(false);

							mRefreshedTrips++;
						}

						currentTrips.remove(tripNumber);
					}

					// Remove all trips that were not returned by the server (not including guest trips)
					for (String tripNumber : currentTrips) {
						if (!mTrips.get(tripNumber).isGuest()) {
							Trip trip = mTrips.remove(tripNumber);
							publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, trip));
						}
					}
				}
			}
		}

		// Add all trips to be updated, even ones that may not need to be refreshed
		// (so we can see if any of the ancillary data needs to be refreshed).
		private void gatherTrips() {
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
