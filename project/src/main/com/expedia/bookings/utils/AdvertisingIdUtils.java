package com.expedia.bookings.utils;

import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mobiata.android.Log;

public class AdvertisingIdUtils {

	private static final String TAG = "TrackingUtils";

	private static String sIDFA;
	private static boolean sIsAdTrackingLimited = true;

	// Can't be run on the main thread
	public static void loadIDFA(final Context context) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
					if (resultCode == 0) {
						AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(context);
						sIDFA = info.getId();
						sIsAdTrackingLimited = info.isLimitAdTrackingEnabled();
					}
					else {
						sIDFA = null;
						sIsAdTrackingLimited = false;
					}
				}
				catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}).start();
	}

	// Only return AdvertisingId if ad tracking is allowed
	public static String getIDFA() {
		return !sIsAdTrackingLimited ? sIDFA : null;
	}
}
