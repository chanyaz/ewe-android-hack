package com.expedia.bookings.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.expedia.bookings.tracking.AdTracker;

public class PosChangedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		AdTracker.initialize(context);
	}
}