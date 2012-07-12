package com.expedia.bookings.tracking;

import android.content.Context;

import com.amobee.agency.tracking.AmobeeReceiver;
import com.amobee.agency.tracking.AmobeeReceiver.Goal;

public class Amobee {
	private static Context mContext;
	private static String mAppId;
	private static boolean mEnabled = false;

	private Amobee() {
	}

	public static void initialize(Context context, String appId, boolean enabled) {
		mContext = context.getApplicationContext();
		mAppId = appId;

		mEnabled = enabled && mContext != null && mAppId != null;
	}

	public static void trackFirstLaunch() {
		if (mEnabled) {
			AmobeeReceiver.amobeeTracking(Goal.FIRST_LAUNCH, mContext, mAppId);
		}
	}

	public static void trackLaunch() {
		if (mEnabled) {
			AmobeeReceiver.amobeeTracking(Goal.LAUNCH, mContext, mAppId);
		}
	}

	public static void trackLogin() {
		if (mEnabled) {
			AmobeeReceiver.amobeeTracking(Goal.LOGIN, mContext, mAppId);
		}
	}

	public static void trackBooking(String currency, double totalPrice, int duration, int daysRemaining) {
		if (mEnabled) {
			Object params = new Object[] { currency, totalPrice, duration, daysRemaining };
			AmobeeReceiver.amobeeTracking(Goal.CUSTOM0, mContext, mAppId, params);
		}
	}
}