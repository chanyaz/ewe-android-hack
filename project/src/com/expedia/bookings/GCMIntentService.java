package com.expedia.bookings;

import org.json.JSONArray;
import org.json.JSONObject;

import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.google.android.gcm.GCMBaseIntentService;
import com.mobiata.android.Log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class GCMIntentService extends GCMBaseIntentService {

	public GCMIntentService() {
		super(PushNotificationUtils.SENDER_ID);
		Log.d("GCM GCMIntentService constructor");
	}

	@Override
	public void onRegistered(Context context, String regId) {
		Log.d("GCM onRegistered regId:" + regId);
		PushNotificationUtils.setRegistrationId(this, regId);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.d("GCM onUnregistered arg1:" + arg1);
		//TODO: Maybe do a callback or something
		PushNotificationUtils.setRegistrationId(this, "");
	}

	@Override
	protected void onMessage(Context arg0, Intent intent) {
		Log.d("GCM onMessage intent:" + intent);
		printItent(intent);

		Bundle extras = intent.getExtras();
		if (extras != null) {
			if (extras.containsKey("data") && extras.containsKey("message") && extras.containsKey("from")) {
				try {
					JSONObject data = new JSONObject(extras.getString("data"));
					JSONObject message = new JSONObject(extras.getString("message"));

					//Used for omniture tracking
					@SuppressWarnings("unused")
					String type = data.optString("t");
					//Flight history id
					String flightHistoryId = data.optString("fhid");
					int fhid = Integer.parseInt(flightHistoryId);

					//The key to determine which string to use
					String locKey = message.getString("loc-key");
					String locStr = PushNotificationUtils.getLocStringForKey(locKey);

					//The arguments for the locKey string
					JSONArray locArgs = message.getJSONArray("loc-args");
					String[] locArgsStrings = new String[locArgs.length()];
					for (int i = 0; i < locArgs.length(); i++) {
						locArgsStrings[i] = locArgs.getString(i);
					}

					//We should find the flight in itin manager (using fhid) and do a deep refresh. and to find the correct uniqueid for the itin in question
					TripComponent component = ItineraryManager.getInstance().getTripComponentFromFlightHistoryId(fhid);
					if (component != null) {

						//TODO: Wait  until the deep refresh is complete before scheduling the notification
						ItineraryManager.getInstance().deepRefreshTrip(component.getParentTrip());
					}

					//After the refresh completes we should show the notification
					PushNotificationUtils.generateNotification(this, fhid, locStr, locArgsStrings);

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

	@SuppressLint("NewApi")
	private void printItent(Intent intent) {
		Log.d("GCM printItent");
		for (String key : intent.getExtras().keySet()) {
			Log.d("GCM key:" + key + " value:" + intent.getExtras().getString(key, "FAIL"));
		}
	}

}
