package com.expedia.bookings.tracking;

import android.content.Context;

import com.amobee.agency.tracking.AmobeeReceiver;
import com.amobee.agency.tracking.AmobeeReceiver.Goal;
import com.mobiata.android.Log;

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

		Log.i("Amobee tracking initialized (enabled: " + String.valueOf(enabled) + ")");
	}

	public static void trackFirstLaunch() {
		if (mEnabled) {
			AmobeeReceiver.amobeeTracking(Goal.FIRST_LAUNCH, mContext, mAppId);

			Log.i("Submitted Amobee first launch event");
		}
	}

	public static void trackLaunch() {
		if (mEnabled) {
			AmobeeReceiver.amobeeTracking(Goal.LAUNCH, mContext, mAppId);

			Log.i("Submitted Amobee launch event");
		}
	}

	public static void trackLogin() {
		if (mEnabled) {
			AmobeeReceiver.amobeeTracking(Goal.LOGIN, mContext, mAppId);

			Log.i("Submitted Amobee login event");
		}
	}

	public static void trackBooking(String currency, double totalPrice, int duration, int daysRemaining) {
		if (mEnabled) {
			AmobeeReceiver
					.amobeeTracking(Goal.CUSTOM0, mContext, mAppId, currency, totalPrice, duration, daysRemaining);

			Log.i("Submitted Amobee booking event");
		}
	}

	public static void trackCheckout(String currency, double flightBookingValue,
			int numberOfDaysInAdvanceOfFirstFlight, String destinationAirportCode) {
		if (mEnabled) {
			// f1395 set destination airport code as alpha2 which is 2nd element in array, apparently
			AmobeeReceiver.amobeeTracking(Goal.CUSTOM1, mContext, mAppId, flightBookingValue, destinationAirportCode,
					currency, numberOfDaysInAdvanceOfFirstFlight);

			Log.i("Submitted Amobee checkout event");
		}
	}
}
