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

import com.expedia.bookings.data.User;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;

// Make sure to call init() before using in the app!
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

	/**
	 * Get a list of the current Trips.
	 * 
	 * Be warned: these Trips will be updated from a sync
	 * operation.  If this behavior seems wrong, talk with
	 * DLew since he's open to the idea of changing this
	 * behavior.
	 */
	public Collection<Trip> getTrips() {
		return mTrips.values();
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

		mTrips = null;

		File file = context.getFileStreamPath(MANAGER_PATH);
		if (file.exists()) {
			try {
				JSONObject obj = new JSONObject(IoUtils.readStringFromFile(MANAGER_PATH, context));
				fromJson(obj);
			}
			catch (Exception e) {
				Log.w("Could not load ItineraryManager data, starting from scratch again...", e);
				file.delete();
			}
		}

		if (mTrips == null) {
			mTrips = new HashMap<String, Trip>();
		}
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

	public interface ItinerarySyncListener {
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

	private void onTripUpdated(Trip trip) {
		for (ItinerarySyncListener listener : mSyncListeners) {
			listener.onTripUpdated(trip);
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

	private AsyncTask<Void, Trip, Collection<Trip>> mSyncTask;

	// TODO: Figure out better values for this
	private static final long UPDATE_TRIP_QUICK_CUTOFF = 1000 * 60 * 60 * 24; // 1 day

	/**
	 * Start a sync operation.
	 * 
	 * If a sync is already in progress then calls to this are ignored.
	 */
	public void startSync() {
		if (!isSyncing()) {
			mTripSyncQueue.clear();

			mSyncTask = new AsyncTask<Void, Trip, Collection<Trip>>() {
				@Override
				protected Collection<Trip> doInBackground(Void... params) {
					// If the user is logged in, retrieve a listing of current trips for logged in user
					if (User.isLoggedIn(mContext)) {
						ExpediaServices services = new ExpediaServices(mContext);
						TripResponse response = services.getTrips(0);

						// TODO: ERROR HANDLING

						if (response != null && !response.hasErrors()) {
							for (Trip trip : response.getTrips()) {

								// TODO: Determine if we got full details and update if possible
								if (!mTrips.containsKey(trip.getTripId())) {
									mTrips.put(trip.getTripId(), trip);
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
						if (now - UPDATE_TRIP_QUICK_CUTOFF < trip.getLastQuickUpdateMillis()) {
							continue;
						}

						// TODO: Figure out algorithm for when to do a cached update vs. full update (assumes cached atm)

						ExpediaServices services = new ExpediaServices(mContext);
						TripDetailsResponse response = services.getTripDetails(trip);

						// TODO: HANDLE ERROR SITUATIONS

						if (response != null && !response.hasErrors()) {
							Trip updatedTrip = response.getTrip();
							trip.updateFrom(updatedTrip, false);

							publishProgress(trip);

							// POSSIBLE TODO: Only call tripUpated() when it's actually changed
						}
					}

					save();

					return mTrips.values();
				}

				@Override
				protected void onProgressUpdate(Trip... values) {
					super.onProgressUpdate(values);

					onTripUpdated(values[0]);
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
