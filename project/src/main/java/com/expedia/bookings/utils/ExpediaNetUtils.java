package com.expedia.bookings.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ExpediaNetUtils {
	public static boolean sShouldFake = false;
	public static boolean sFakeIsOnline = false;

	public static void setFake(boolean shouldFake, boolean fakeValue) {
		sShouldFake = shouldFake;
		sFakeIsOnline = fakeValue;
	}

	public static boolean isOnline(Context context) {
		if (sShouldFake) {
			return sFakeIsOnline;
		}

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return (ni != null && ni.isAvailable() && ni.isConnected());
	}
}

