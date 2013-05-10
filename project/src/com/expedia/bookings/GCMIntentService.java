package com.expedia.bookings;

import org.json.JSONArray;
import org.json.JSONObject;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.utils.Ui;
import com.google.android.gcm.GCMBaseIntentService;
import com.mobiata.android.Log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class GCMIntentService extends GCMBaseIntentService {

	private static String sGCMRegistrationId = "";
	public static final String SENDER_ID = "895052546820";

	public GCMIntentService() {
		super(SENDER_ID);
		Log.d("GCM GCMIntentService constructor");
	}

	@Override
	public void onRegistered(Context context, String regId) {
		Log.d("GCM onRegistered regId:" + regId);
		setRegistrationId(regId);
		sGCMRegistrationId = regId;

	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.d("GCM onUnregistered arg1:" + arg1);

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
					String type = data.optString("t");
					//Flight history id
					String fhid = data.optString("fhid");

					//The key to determine which string to use
					String locKey = message.getString("loc-key");
					String locStr = getLocStr(locKey);

					//The arguments for the locKey string
					JSONArray locArgs = message.getJSONArray("loc-args");
					String[] locArgsStrings = new String[locArgs.length()];
					for (int i = 0; i < locArgs.length(); i++) {
						locArgsStrings[i] = locArgs.getString(i);
					}

					showTestNotification(fhid + "_" + locArgs);

					//String formattedMessage = String.format(locStr, locArgsStrings);

					//We should find the flight in itin manager (using fhid) and do a deep refresh. and to find the correct uniqueid for the itin in question

					//After the refresh completes we should show the notification

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

	private void showTestNotification(String uniqueId) {
		long triggerTimeMillis = System.currentTimeMillis();

		Notification notification = new Notification(uniqueId, triggerTimeMillis);
		notification.setNotificationType(NotificationType.FLIGHT_CHECK_IN);
		notification.setFlags(Notification.FLAG_PUSH);

		String title = "We got a push notification";
		notification.setTicker(title);
		notification.setTitle(title);

		String body = "Test body, test body, test body";
		notification.setBody(body);

		notification.setImage(Notification.ImageType.DESTINATION, R.drawable.bg_itin_placeholder_flight,
				"MSP");

		notification.save();
		notification.scheduleNotification(this);
	}

	private String getLocStr(String locKey) {
		return "CAT_MIGGINS";
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

	public static void setRegistrationId(String regId) {
		if (!sGCMRegistrationId.equals(regId)) {
			//Send empty list to server for old regId

			sGCMRegistrationId = regId;

			//We should now tell the server about our current flights
		}
	}

	public static String getRegistrationId() {
		return sGCMRegistrationId;
	}

}
