package com.expedia.bookings.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

public class LocaleChangeReceiver extends BroadcastReceiver {

	public static final String KEY_LOCALE_CHANGED = "com.expedia.bookings.locale_changed";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("Locale changed!");
		SettingUtils.save(context, KEY_LOCALE_CHANGED, true);
	}
}
