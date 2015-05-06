package com.expedia.bookings.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.tracking.AdTracker;
import com.mobiata.android.Log;

public class LocaleChangeReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("LocaleChangeReceiver: Locale changed!");

		// Clear out saved flight route data
		Db.deleteCachedFlightRoutes(context);

		//Update Locale
		AdTracker.updatePOS();
	}
}
