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
import android.os.AsyncTask;

import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.Trip.LevelOfDetail;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;

// Make sure to call init() before using in the app!
//
// In addition, make sure to call startSync() before manipulating data.
public class ItineraryManager implements JSONable {

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
	 */
	public void addGuestTrip(String email, String tripId, boolean startSyncIfNotInProgress) {
		Trip trip = new Trip(email, tripId);
		mTrips.put(tripId, trip);

		if (isSyncing()) {
			mTripSyncQueue.add(trip);
		}
		else if (startSyncIfNotInProgress) {
			startSync();
		}
	}

	public void removeGuestTrip(String tripId) {
		Trip trip = mTrips.get(tripId);

		if (trip == null) {
			Log.w("Tried to remove a guest tripId that doesn't exist: " + tripId);
		}
		else if (!trip.isGuest()) {
			Log.w("Tried to remove a non-guest trip, DENIED because only the ItinManager is allowed to do that: "
					+ tripId);
		}
		else {
			mTrips.remove(tripId);

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
	 * Indicates that the user has logged out.  We remove
	 * ALL trips (even guest trips) in this situation.
	 */
	public void onSignOut() {
		if (isSyncing()) {
			// If we're syncing, cancel the sync (then let the canceled task
			// do the sign out once it's finished).
			mSyncTask.cancel(true);
			mSyncTask.cancelDownloads();
		}
		else {
			doSignOut();
		}
	}

	private void doSignOut() {
		if (mTrips == null) {
			// Delete the file, so it can't be reloaded later
			File file = mContext.getFileStreamPath(MANAGER_PATH);
			file.delete();
			return;
		}

		for (Trip trip : mTrips.values()) {
			onTripRemoved(trip);
		}

		mTrips.clear();

		save();
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
		public void onTripUpateFailed(Trip trip);

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
			listener.onTripUpateFailed(trip);
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

	private Queue<Trip> mTripSyncQueue = new PriorityQueue<Trip>();

	private SyncTask mSyncTask;

	// TODO: Figure out better values for this
	private static final long UPDATE_TRIP_CACHED_CUTOFF = 1000 * 60 * 60 * 24; // 1 day

	/**
	 * Start a sync operation.
	 * 
	 * If a sync is already in progress then calls to this are ignored.
	 */
	public void startSync() {
		if (!isSyncing()) {
			mTripSyncQueue.clear();
			mSyncTask = new SyncTask();
			mSyncTask.execute();
		}
		else {
			Log.i("Tried to start a sync while one is already in progress.");
		}
	}

	public boolean isSyncing() {
		return mSyncTask != null && mSyncTask.getStatus() != AsyncTask.Status.FINISHED;
	}

	private class SyncTask extends AsyncTask<Void, ProgressUpdate, Collection<Trip>> {

		private ExpediaServices mServices;

		public SyncTask() {
			mServices = new ExpediaServices(mContext);
		}

		/*
		 * Implementation note - we regularly check if the sync has been 
		 * cancelled (after every service call).  If it has been cancelled,
		 * then we exit out quickly.  If you add any service calls, also
		 * check afterwards (since the service calls are where the app
		 * will get hung up during a cancel).
		 */
		@Override
		protected Collection<Trip> doInBackground(Void... params) {
			// We first try to load the itin man data on sync
			load();

			if (isCancelled()) {
				return null;
			}

			// Check if we're online; quickly fail if not 
			if (!NetUtils.isOnline(mContext)) {
				publishProgress(new ProgressUpdate(SyncError.OFFLINE));
				save();
				return mTrips.values();
			}

			// If the user is logged in, retrieve a listing of current trips for logged in user
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
					return null;
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
						String tripId = trip.getTripId();

						boolean hasFullDetails = trip.getLevelOfDetail() == LevelOfDetail.FULL;
						if (!mTrips.containsKey(tripId)) {
							mTrips.put(tripId, trip);

							publishProgress(new ProgressUpdate(ProgressUpdate.Type.ADDED, trip));
						}
						else if (hasFullDetails) {
							mTrips.get(tripId).updateFrom(trip);
						}

						// If we have full details, mark this as recently updated so we don't
						// refresh it below
						if (hasFullDetails) {
							trip.markUpdated(false);
						}

						currentTrips.remove(tripId);
					}

					// Remove all trips that were not returned by the server (not including guest trips)
					for (String tripId : currentTrips) {
						if (!mTrips.get(tripId).isGuest()) {
							Trip trip = mTrips.remove(tripId);
							publishProgress(new ProgressUpdate(ProgressUpdate.Type.REMOVED, trip));
						}
					}
				}
			}

			// Now that we have set of fresh trips, refresh each one
			mTripSyncQueue.addAll(mTrips.values());

			Log.i("Updating " + mTripSyncQueue.size() + " trips...");

			while (mTripSyncQueue.size() > 0) {
				Trip trip = mTripSyncQueue.poll();

				// Determine if we should sync or not
				long now = Calendar.getInstance().getTimeInMillis();
				if (now - UPDATE_TRIP_CACHED_CUTOFF < trip.getLastCachedUpdateMillis()) {
					Log.d("Not querying trip, recently updated: " + trip.getTripId());
					continue;
				}

				TripDetailsResponse response = mServices.getTripDetails(trip, true);

				if (isCancelled()) {
					return null;
				}

				if (response == null || response.hasErrors()) {
					if (response != null && response.hasErrors()) {
						Log.w("Error updating trip " + trip.getTripId() + ": "
								+ response.gatherErrorMessage(mContext));
					}

					publishProgress(new ProgressUpdate(ProgressUpdate.Type.UPDATE_FAILED, trip));
				}
				else {
					LevelOfDetail initialLevelOfDetail = trip.getLevelOfDetail();

					Trip updatedTrip = response.getTrip();

					// Look for images
					TripFlight tripFlight;
					FlightTrip flightTrip;
					TripCar tripCar;
					Waypoint waypoint;
					String destinationCode;

					for (TripComponent tripComponent : updatedTrip.getTripComponents()) {
						if (tripComponent.getType().equals(TripComponent.Type.FLIGHT)) {
							tripFlight = (TripFlight) tripComponent;
							flightTrip = tripFlight.getFlightTrip();
							for (int i = 0; i < flightTrip.getLegCount(); i++) {
								FlightLeg fl = flightTrip.getLeg(i);
								waypoint = fl.getLastWaypoint();
								destinationCode = waypoint.mAirportCode;

								BackgroundImageResponse imageResponse = mServices.getFlightsBackgroundImage(
										destinationCode, 0, 0);

								if (isCancelled()) {
									return null;
								}

								if (imageResponse != null) {
									tripFlight.setLegDestinationImageUrl(i, imageResponse.getImageUrl());
								}
								else {
									tripFlight.setLegDestinationImageUrl(i, "");
								}

								for (Flight segment : fl.getSegments()) {
									if (Math.abs(segment.mOrigin.getMostRelevantDateTime().getTimeInMillis()
											- now) <= (60 * 60 * 24 * 1000)) {
										segment.updateFrom(mServices.getUpdatedFlight(segment));

										if (isCancelled()) {
											return null;
										}
									}
									else if (segment.getArrivalWaypoint().getMostRelevantDateTime()
											.getTimeInMillis() < now) {
										segment.mStatusCode = Flight.STATUS_LANDED;
									}
								}
							}
						}
						else if (tripComponent.getType().equals(TripComponent.Type.CAR)) {
							tripCar = (TripCar) tripComponent;
							Car.Category category = tripCar.getCar().getCategory();

							if (category != null) {
								BackgroundImageResponse imageResponse = mServices.getCarsBackgroundImage(tripCar
										.getCar().getCategory(), 0, 0);

								if (isCancelled()) {
									return null;
								}

								if (imageResponse != null) {
									tripCar.setCarCategoryImageUrl(imageResponse.getImageUrl());
								}
							}
						}
					}

					// Update trip
					trip.updateFrom(updatedTrip);
					trip.markUpdated(false);

					// We only consider a guest trip added once it has some meaningful info
					if (initialLevelOfDetail == LevelOfDetail.NONE && trip.isGuest()) {
						publishProgress(new ProgressUpdate(ProgressUpdate.Type.ADDED, trip));
					}

					publishProgress(new ProgressUpdate(ProgressUpdate.Type.UPDATED, trip));

					// POSSIBLE TODO: Only call tripUpated() when it's actually changed
				}
			}

			save();

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
		}

		@SuppressLint("NewApi")
		@Override
		protected void onCancelled(Collection<Trip> result) {
			super.onCancelled(result);

			// Currently, the only reason we are canceled is if
			// the user signs out mid-update.  So continue
			// the signout in that case.
			doSignOut();

			onSyncFailed(SyncError.CANCELLED);

			onSyncFinished(null);
		}

		// Should be called in addition to cancel(boolean), in order
		// to cancel the update mid-download
		public void cancelDownloads() {
			mServices.onCancel();
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
			mTrips.put(trip.getTripId(), trip);
		}

		return true;
	}
}
