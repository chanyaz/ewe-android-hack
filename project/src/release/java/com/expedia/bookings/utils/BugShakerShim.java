package com.expedia.bookings.utils;

import android.app.Application;
import android.content.Context;

public class BugShakerShim {

	public static boolean isBugShakerEnabled(Context context) {
		return false;
	}

	public static void startNewBugShaker(Application application) {
		// Do nothing for release builds
	}

	public static void turnOff() {
		// Do nothing for release builds
	}
}
