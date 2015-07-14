package com.expedia.bookings.utils;

import android.app.Activity;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.mobileapptracker.MobileAppTracker;

public class TuneUtils {

	public static MobileAppTracker mobileAppTracker = null;
	private static boolean initialized = false;

	public static void init(ExpediaBookingApp app) {
		initialized = true;

		String advertiserID = app.getString(R.string.tune_sdk_app_advertiser_id);
		String conversionKey = app.getString(R.string.tune_sdk_app_conversion_key);

		mobileAppTracker = MobileAppTracker.init(app, advertiserID, conversionKey);

		mobileAppTracker.setDebugMode(BuildConfig.DEBUG);
		mobileAppTracker.setAllowDuplicates(BuildConfig.DEBUG);

	}

	public static void startTune(Activity activity) {
		if (initialized) {
			// Get source of open for app re-engagement
			mobileAppTracker.setReferralSources(activity);
			// MAT will not function unless the measureSession call is included
			mobileAppTracker.measureSession();
		}
	}

}
