package com.expedia.bookings.test.robolectric;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowTelephonyManager;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.expedia.bookings.activity.ExpediaBookingApp;

import static org.robolectric.Shadows.shadowOf;

public class TestExpediaBookingApp extends ExpediaBookingApp {

	@Override
	public void onCreate() {
		TelephonyManager telephonyManager = (TelephonyManager) RuntimeEnvironment.application
			.getSystemService(Context.TELEPHONY_SERVICE);
		ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
		shadowTelephonyManager.setNetworkOperatorName("Test Operator");

		setIsRobolectric(true);
		super.onCreate();
	}
}
