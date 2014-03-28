package com.expedia.bookings.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobiata.android.Log;

public class TravelocityLocaleChangeReceiver extends BroadcastReceiver {
	public static final String ACTION_LOCALE_CHANGED = "com.expedia.bookings.action.travelocity_locale_changed";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("TravelocityLocaleChangeReceiver: Locale changed!");
	}
}
