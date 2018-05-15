package com.expedia.bookings.notification;

import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncAdapter;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.Log;

public class GCMIntentService extends IntentService {

	public GCMIntentService() {
		super("GCMIntentService: " + PushNotificationUtils.SENDER_ID);
		Log.d("GCM GCMIntentService constructor");
	}

	private static final String LOGGING_TAG = "GCMIntentService";

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(LOGGING_TAG, "GCM onMessage intent:" + intent);
		StrUtils.printIntent(intent);

		Bundle extras = intent.getExtras();
		if (extras != null) {
			if (extras.containsKey("data") && extras.containsKey("message") && extras.containsKey("from")) {
				try {
					JSONObject data = new JSONObject(extras.getString("data"));
					JSONObject message = new JSONObject(extras.getString("message"));

					//Used for omniture tracking  - should always be an int string
					final String type = data.optString("t");

					//Flight history id
					final int fhid;
					final String flightHistoryId = data.optString("fhid");
					if (!TextUtils.isEmpty(flightHistoryId)) {
						fhid = Integer.parseInt(flightHistoryId);
					}
					else {
						// If the fhid is empty, set it to 0. Likely a desktop notification,
						// but if it isn't we are still not introducing incorrect behavior.
						fhid = 0;
					}

					//The key to determine which string to use
					final String locKey = message.getString("loc-key");

					//The title
					final String titleKey = message.optString("title-loc-key");

					//The arguments for the locKey string
					JSONArray locArgs = message.getJSONArray("loc-args");
					final String[] locArgsStrings = new String[locArgs.length()];
					for (int i = 0; i < locArgs.length(); i++) {
						locArgsStrings[i] = locArgs.getString(i);
					}
					final String notificationID = data.optString("nid");

					//If our itins arent synced yet, we cant show pretty notifications, so if we are syncing, we wait
					if (!ItineraryManager.getInstance().isSyncing()) {
						Log.d(LOGGING_TAG, "GCM onMessage - not syncing, starting sync.");
						ItineraryManager.getInstance().startSync(true);
						ItineraryManager.getInstance().addSyncListener(makeSyncListener(fhid, locKey, locArgsStrings, type, titleKey, notificationID));
					}
					else {
						Log.d(LOGGING_TAG, "GCM onMessage - Waiting for the ItinManager to finish syncing...");
						ItineraryManager.getInstance().addSyncListener(makeSyncListener(fhid, locKey, locArgsStrings, type, titleKey, notificationID));
					}
				}
				catch (Exception ex) {
					Log.e(LOGGING_TAG, "GCM - Exception parsing bundle", ex);
				}
			}
			else {
				StringBuilder errorBuilder = new StringBuilder("GCM - Missing Required Extras from fields: ");
				if (!extras.containsKey("data")) {
					errorBuilder.append("data, ");
				}
				if (!extras.containsKey("message")) {
					errorBuilder.append("message, ");
				}
				if (!extras.containsKey("from")) {
					errorBuilder.append("from");
				}

				Log.e(LOGGING_TAG, errorBuilder.toString());
			}
		}
		else {
			Log.e(LOGGING_TAG, "GCM - No Extras Bundle");
		}
	}

	private void generateNotification(final int fhid, final String locKey, final String[] locArgs, final String type, final String titleKey, final String nID) {
		//We should find the flight in itin manager (using fhid) and do a deep refresh. and to find the correct uniqueid for the itin in question
		TripFlight component = ItineraryManager.getInstance().getTripComponentFromFlightHistoryId(fhid);
		if (component != null) {

			//ItineraryManager.startSync needs to happen on the UI thread, hence the Handler magic
			final Trip notificationTrip = component.getParentTrip();
			if (notificationTrip != null) {
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						ItineraryManager.getInstance().addSyncListener(new ItinerarySyncAdapter() {
							@Override
							public void onTripUpdated(Trip trip) {
								//After the refresh completes we should show the notification
								if (notificationTrip.equals(trip)) {
									Log.d(LOGGING_TAG, "GCM: Our deep refreshing trip has updated, so we can now generate the actual notification");
									notify(trip);
								}
							}

							@Override
							public void onTripUpdateFailed(Trip trip) {
								// We failed to refresh, which is sad, but there's nothing we can do
								if (notificationTrip.equals(trip)) {
									Log.d(LOGGING_TAG, "GCM: Our deep refreshing trip has failed to update so we are giving up and notifying with old data");
									notify(trip);
								}
							}

							private void notify(Trip trip) {
								PushNotificationUtils.generateNotification(GCMIntentService.this, fhid, locKey,
									locArgs, titleKey, nID);
								ItineraryManager.getInstance().removeSyncListener(this);
							}
						});

						ItineraryManager.getInstance().deepRefreshTrip(notificationTrip.getItineraryKey(), true);
						Log.d(LOGGING_TAG, "GCM: Started deep refresh, waiting for sync completion...");
					}
				});
			}
			else {
				Log.w(LOGGING_TAG, "GCM: Generating push notification but unable to find parentTrip for fhid=" + fhid + " type=" + type
					+ "component=" + component.toJson().toString());
				PushNotificationUtils.generateNotification(GCMIntentService.this, fhid, locKey, locArgs, titleKey, nID);
			}
		}
		else {
			Log.w(LOGGING_TAG, "GCM: Generating push notification, but can't find the tripComponent, thus no deepRefresh called fhid="
				+ fhid + " type=" + type);
			PushNotificationUtils.generateNotification(GCMIntentService.this, fhid, locKey, locArgs, titleKey, nID);
		}

	}

	public ItineraryManager.ItinerarySyncAdapter makeSyncListener(final int fhid, final String locKey, final String[] locArgs, final String type, final String titleKey, final String nID) {
		return new ItineraryManager.ItinerarySyncAdapter() {
			@Override
			public void onSyncFinished(Collection<Trip> trips) {
				Log.d(LOGGING_TAG, "GCM onMessage - ItinManager finished syncing, building notification now.");
				ItineraryManager.getInstance().removeSyncListener(this);
				generateNotification(fhid, locKey, locArgs, type, titleKey, nID);
			}

			@Override
			public void onSyncFailure(ItineraryManager.SyncError error) {
				Log.d(LOGGING_TAG, "GCM onMessage - ItinManager failed syncing, building notification now.");
				ItineraryManager.getInstance().removeSyncListener(this);
				generateNotification(fhid, locKey, locArgs, type, titleKey, nID);
			}
		};
	}
}
