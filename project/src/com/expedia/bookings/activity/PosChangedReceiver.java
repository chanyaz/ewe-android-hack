package com.expedia.bookings.activity;

import com.expedia.bookings.tracking.AdTracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PosChangedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		AdTracker.initialize(context);
	}
}