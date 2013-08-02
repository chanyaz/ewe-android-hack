package com.expedia.bookings.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.Log;

public class LocaleChangeReceiver extends BroadcastReceiver {
	public static final String ACTION_LOCALE_CHANGED = "com.expedia.bookings.action.locale_changed";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("Locale changed!");

		// Clear out saved flight route data
		Db.deleteCachedFlightRoutes(context);

		// Reset the app every time if the locale changes - we don't want to deal with it
		if (ExpediaBookingApp.IS_VSC) {
			NavUtils.goToVSC(context);
		}
		else {
			NavUtils.goToLaunchScreen(context);
		}
	}
}
