package com.expedia.bookings.tracking;

import android.content.Context;

import com.AdX.tag.AdXConnect;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

public class AdX {
	private static Context mContext;
	private static boolean mEnabled;
	private static int mLogLevel;

	public static void initialize(Context context, boolean enabled) {
		mContext = context;
		mEnabled = enabled;
		if (AndroidUtils.isRelease(mContext)) {
			mLogLevel = 0;
		}
		else {
			mLogLevel = 5;
		}

		Log.i("AdX tracking initialized (enabled: " + String.valueOf(enabled) + ")");
	}

	public static void trackFirstLaunch() {
		if (mEnabled) {
			AdXConnect.getAdXConnectInstance(mContext, false, mLogLevel);
			AdXConnect.getAdXConnectEventInstance(mContext, "FirstLaunch", "", "");
			Log.i("AdX first launch event");
		}
	}

	public static void trackLaunch() {
		if (mEnabled) {
			AdXConnect.getAdXConnectInstance(mContext, true, mLogLevel);
			AdXConnect.getAdXConnectEventInstance(mContext, "Launch", "", "");
			Log.i("AdX launch event");
		}
	}

	public static void trackLogin() {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Login", "", "");
			Log.i("AdX login event");
		}
	}

	public static void trackViewItinList() {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Itinerary", "", "");
			Log.i("AdX Itinerary event");
		}
	}

	public static void trackHotelBooking(String currency, double totalPrice) {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Booking", String.valueOf(totalPrice), currency);
			Log.i("AdX hotel booking event currency=" + currency + " total=" + totalPrice);
		}
	}
}
