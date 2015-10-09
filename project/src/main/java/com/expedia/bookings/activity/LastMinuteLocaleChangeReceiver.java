package com.expedia.bookings.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobiata.android.Log;

public class LastMinuteLocaleChangeReceiver extends BroadcastReceiver {
	public static final String ACTION_LOCALE_CHANGED = "com.expedia.bookings.action.lastminute_locale_changed";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("LastMinuteLocaleChangeReceiver: Locale changed!");
	}
}
