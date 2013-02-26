package com.expedia.bookings.data.trips;

import java.io.File;
import java.io.IOException;
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

import android.content.Context;
import android.os.AsyncTask;

import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;
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
		if (mTrips == null) {
			// Delete the file, so it can't be reloaded later
			File file = mContext.getFileStreamPath(MANAGER_PATH);
			file.delete();
			return;
		}

		// TODO: Handle when sync is in progress

		for (Trip trip : mTrips.values()) {
			onTripRemoved(trip);
		}

		mTrips.clear();

		save();
	}

	//////////////////////////////////////////////////////////////////////////
	// Data

	private static final String MANAGER_PATH = "itin-manager.dat";

	/**
	 * Must be called before using ItineraryManager for the first time.
	 * 
	 * I expect this to be called from the Application.  That way the
	 * context won't leak.
	 */
	public void init(Context context) {
		mContext = context;
	}

	private void save() {
		try {
			IoUtils.writeStringToFile(MANAGER_PATH, toJson().toString(), mContext);
		}
		catch (IOException e) {
			Log.w("Could not save ItineraryManager data", e);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Sync listener

	public enum SyncError {
		OFFLINE,
		USER_LIST_REFRESH_FAILURE,
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
		for (ItinerarySyncListener listener : mSyncListeners) {
			listener.onTripAdded(trip);
		}
	}

	private void onTripUpdated(Trip trip) {
		for (ItinerarySyncListener listener : mSyncListeners) {
			listener.onTripUpdated(trip);
		}
	}

	private void onTripUpdateFailed(Trip trip) {
		for (ItinerarySyncListener listener : mSyncListeners) {
			listener.onTripUpateFailed(trip);
		}
	}

	private void onTripRemoved(Trip trip) {
		for (ItinerarySyncListener listener : mSyncListeners) {
			listener.onTripRemoved(trip);
		}
	}

	private void onSyncFailure(SyncError error) {
		for (ItinerarySyncListener listener : mSyncListeners) {
			listener.onSyncFailure(error);
		}
	}

	private void onSyncFinished(Collection<Trip> trips) {
		for (ItinerarySyncListener listener : mSyncListeners) {
			listener.onSyncFinished(trips);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Data syncing

	private Queue<Trip> mTripSyncQueue = new PriorityQueue<Trip>();

	private AsyncTask<Void, ProgressUpdate, Collection<Trip>> mSyncTask;

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

			mSyncTask = new AsyncTask<Void, ProgressUpdate, Collection<Trip>>() {
				@Override
				protected Collection<Trip> doInBackground(Void... params) {
					// We first try to load the itin man data on sync
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

					// Check if we're online; quickly fail if not 
					if (!NetUtils.isOnline(mContext)) {
						publishProgress(new ProgressUpdate(SyncError.OFFLINE));
						save();
						return mTrips.values();
					}

					// If the user is logged in, retrieve a listing of current trips for logged in user
					if (User.isLoggedIn(mContext)) {
						ExpediaServices services = new ExpediaServices(mContext);
						TripResponse response = services.getTrips(false, 0);

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

					while (mTripSyncQueue.size() > 0) {
						Trip trip = mTripSyncQueue.poll();

						// Determine if we should sync or not
						long now = Calendar.getInstance().getTimeInMillis();
						if (now - UPDATE_TRIP_CACHED_CUTOFF < trip.getLastCachedUpdateMillis()) {
							Log.d("Not querying trip, recently updated: " + trip.getTripId());
							continue;
						}

						ExpediaServices services = new ExpediaServices(mContext);
						TripDetailsResponse response = services.getTripDetails(trip, true);

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

										BackgroundImageResponse imageResponse = services.getFlightsBackgroundImage(
												destinationCode, 0, 0);

										if (imageResponse != null) {
											tripFlight.setLegDestinationImageUrl(i, imageResponse.getImageUrl());
										}
										else {
											tripFlight.setLegDestinationImageUrl(i, "");
										}

										for (Flight segment : fl.getSegments()) {
											if (Math.abs(segment.mOrigin.getMostRelevantDateTime().getTimeInMillis()
													- now) <= (60 * 60 * 24 * 1000)) {
												segment.updateFrom(services.getUpdatedFlight(segment));
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
										BackgroundImageResponse imageResponse = services.getCarsBackgroundImage(tripCar
												.getCar().getCategory(), 0, 0);

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
						onSyncFailure(update.mError);
						break;
					}
				}

				@Override
				protected void onPostExecute(Collection<Trip> trips) {
					super.onPostExecute(trips);

					onSyncFinished(trips);
				}
			};

			mSyncTask.execute();
		}
		else {
			Log.i("Tried to start a sync while one is already in progress.");
		}
	}

	public boolean isSyncing() {
		return mSyncTask != null && mSyncTask.getStatus() != AsyncTask.Status.FINISHED;
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
