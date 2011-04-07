package com.expedia.bookings.activity;

import com.mobiata.android.Log;

import android.app.Application;

public class ExpediaBookingApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		Log.configureLogging("ExpediaBookings", true);
	}
}
