package com.mobiata.android.util;

import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mobiata.android.Log;

public class AdvertisingIdUtils {

	private static final String TAG = "TrackingUtils";

	private static String sIDFA;
	private static boolean sIsAdTrackingLimited = true;

	// Can't be run on the main thread
	public static void loadIDFA(final Context context, final OnIDFALoaded callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
					if (resultCode == 0) {
						AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(context);
						sIDFA = info.getId();
						sIsAdTrackingLimited = info.isLimitAdTrackingEnabled();
						if (callback != null) {
							callback.onIDFALoaded(sIDFA);
						}
					}
					else {
						sIDFA = null;
						sIsAdTrackingLimited = false;
						if (callback != null) {
							callback.onIDFAFailed();
						}
					}
				}
				catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}).start();
	}

	public interface OnIDFALoaded {
		public void onIDFALoaded(String idfa);
		public void onIDFAFailed();
	}

	// Only return AdvertisingId if ad tracking is allowed
	public static String getIDFA() {
		return !sIsAdTrackingLimited ? sIDFA : null;
	}
}
