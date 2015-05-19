package com.expedia.bookings.test.robolectric;

import com.expedia.bookings.activity.ExpediaBookingApp;

public class TestExpediaBookingApp extends ExpediaBookingApp {
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public boolean isRobolectric() {
		return true;
	}
}
