package com.expedia.bookings.activity;

import com.mobiata.android.Log;

public class ExpediaBookingApp extends com.activeandroid.Application {
	@Override
	public void onCreate() {
		super.onCreate();

		Log.configureLogging("ExpediaBookings", true);
	}
}
