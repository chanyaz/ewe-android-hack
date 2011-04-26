package com.expedia.bookings.activity;

import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.hotellib.Params;

public class ExpediaBookingApp extends com.activeandroid.Application {
	@Override
	public void onCreate() {
		super.onCreate();

		Params params = Params.getInstance();
		params.mIsRelease = AndroidUtils.isRelease(this);
		Log.configureLogging("ExpediaBookings", !params.mIsRelease);
	}
}
