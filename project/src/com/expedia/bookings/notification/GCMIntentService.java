package com.expedia.bookings.notification;

import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncAdapter;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent;
import com.google.android.gcm.GCMBaseIntentService;
import com.mobiata.android.Log;

public class GCMIntentService extends GCMBaseIntentService {

	public GCMIntentService() {
		super(PushNotificationUtils.SENDER_ID);
		Log.d("GCM GCMIntentService constructor");
	}

	@Override
	public void onRegistered(Context context, String regId) {
		Log.d("GCM onRegistered regId:" + regId);
		GCMRegistrationKeeper.getInstance(context).setRegistrationId(context, regId);

		//ItineraryManager.startSync needs to happen on the UI thread, hence the Handler magic
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				ItineraryManager.getInstance().startPushNotificationSync();
			}
		});
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		Log.d("GCM onUnregistered regId:" + regId);
		GCMRegistrationKeeper.getInstance(context).setRegistrationId(context, "");
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d("GCM onMessage intent:" + intent);
		printItent(intent);

		Bundle extras = intent.getExtras();
		if (extras != null) {
			if (extras.containsKey("data") && extras.containsKey("message") && extras.containsKey("from")) {
				try {
					JSONObject data = new JSONObject(extras.getString("data"));
					JSONObject message = new JSONObject(extras.getString("message"));

					//Used for omniture tracking  - should always be an int string
					final String type = data.optString("t");

					//Flight history id
					String flightHistoryId = data.optString("fhid");
					final int fhid = Integer.parseInt(flightHistoryId);

					//The key to determine which string to use
					final String locKey = message.getString("loc-key");

					//The arguments for the locKey string
					JSONArray locArgs = message.getJSONArray("loc-args");
					final String[] locArgsStrings = new String[locArgs.length()];
					for (int i = 0; i < locArgs.length(); i++) {
						locArgsStrings[i] = locArgs.getString(i);
					}

					//If our itins arent synced yet, we cant show pretty notifications, so if we are syncing, we wait
					if (!ItineraryManager.getInstance().isSyncing()) {
						Log.d("GCM onMessage - generating the notification right away.");
						generateNotification(fhid, locKey, locArgsStrings, type);
					}
					else {
						Log.d("GCM onMessage - Waiting for the ItinManager to finish syncing...");
						ItineraryManager.getInstance().addSyncListener(new ItinerarySyncAdapter() {
							@Override
							public void onSyncFinished(Collection<Trip> trips) {
								Log.d("GCM onMessage - ItinManager finished syncing, building notification now.");
								ItineraryManager.getInstance().removeSyncListener(this);
								generateNotification(fhid, locKey, locArgsStrings, type);
							}
						});
					}

				}
				catch (Exception ex) {
					Log.e("GCM - Exception parsing bundle", ex);
				}
			}
			else {
				Log.e("GCM - Missing Required Extras");
			}
		}
		else {
			Log.e("GCM - No Extras Bundle");
		}
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		Log.d("GCM onError arg1:" + arg1);

	}

	@Override
	public boolean onRecoverableError(Context context, String errorId) {
		Log.d("GCM onRecoverableError errorId:" + errorId);
		return false;
	}

	private void printItent(Intent intent) {
		Log.d("GCM printItent");
		for (String key : intent.getExtras().keySet()) {
			Log.d("GCM key:" + key + " value:" + intent.getExtras().getString(key));
		}
	}

	private void generateNotification(final int fhid, final String locKey, final String[] locArgs, final String type) {
		//We should find the flight in itin manager (using fhid) and do a deep refresh. and to find the correct uniqueid for the itin in question
		TripComponent component = ItineraryManager.getInstance().getTripComponentFromFlightHistoryId(fhid);
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
									Log.d("GCM: Our deep refreshing trip has updated, so we can now generate the actual notification");
									notify(trip);
								}
							}

							@Override
							public void onTripUpdateFailed(Trip trip) {
								// We failed to refresh, which is sad, but there's nothing we can do
								if (notificationTrip.equals(trip)) {
									Log.d("GCM: Our deep refreshing trip has failed to update so we are giving up and notifying with old data");
									notify(trip);
								}
							}

							private void notify(Trip trip) {
								PushNotificationUtils.generateNotification(GCMIntentService.this, fhid, locKey,
										locArgs, type);
								ItineraryManager.getInstance().removeSyncListener(this);
							}
						});

						ItineraryManager.getInstance().deepRefreshTrip(notificationTrip.getItineraryKey(), true);
						Log.d("GCM: Started deep refresh, waiting for sync completion...");
					}
				});
			}
			else {
				Log.w("GCM: Generating push notification but unable to find parentTrip for fhid=" + fhid + " type=" + type
						+ "component=" + component.toJson().toString());
				PushNotificationUtils.generateNotification(GCMIntentService.this, fhid, locKey, locArgs, type);
			}
		}
		else {
			Log.w("GCM: Generating push notification, but can't find the tripComponent, thus no deepRefresh called fhid="
					+ fhid + " type=" + type);
			PushNotificationUtils.generateNotification(GCMIntentService.this, fhid, locKey, locArgs, type);
		}

	}

}
