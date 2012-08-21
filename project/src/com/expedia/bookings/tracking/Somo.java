package com.expedia.bookings.tracking;

import android.content.Context;

import com.mobiata.android.Log;
import com.somo.apptimiser.SomoApptimiserTracker;
import com.somo.apptimiser.SomoTracker;

public class Somo {
	private static SomoTracker mSomoTracker;
	private static boolean mEnabled = false;

	public static void initialize(Context context, int userId, int applicationId, boolean enabled) {
		mSomoTracker = SomoApptimiserTracker.getTracker(context, userId, applicationId);
		mEnabled = enabled;

		Log.i("Somo tracking initialized (enabled: " + String.valueOf(enabled) + ")");
	}

	public static void trackFirstLaunch() {
		if (mEnabled) {
			mSomoTracker.track(SomoTracker.EVENT_OPEN);
			mSomoTracker.submit();

			Log.i("Submitted Somo first launch event");
		}
	}

	public static void trackLaunch() {
		if (mEnabled) {
			mSomoTracker.track(SomoTracker.EVENT_OPEN);
			mSomoTracker.submit();

			Log.i("Submitted Somo launch event");
		}
	}

	public static void trackLogin() {
		if (mEnabled) {
			mSomoTracker.track(SomoTracker.EVENT_SIGN_UP);
			mSomoTracker.submit();

			Log.i("Submitted Somo login event");
		}
	}

	public static void trackBooking(String currency, double totalPrice, int duration, int daysRemaining) {
		if (mEnabled) {
			String info = String.format("Currency: %s - Total price: %f - Duration: %d - Days remaining: %d", currency,
					totalPrice, duration, daysRemaining);

			mSomoTracker.track(SomoTracker.EVENT_SALE, null, null, info);
			mSomoTracker.submit();

			Log.i("Submitted Somo booking event");
		}
	}
}