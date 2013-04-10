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
			Log.i("AdX first launch event");
		}
	}

	public static void trackLaunch() {
		if (mEnabled) {
			AdXConnect.getAdXConnectInstance(mContext, true, mLogLevel);
			Log.i("AdX launch event");
		}
	}

}

