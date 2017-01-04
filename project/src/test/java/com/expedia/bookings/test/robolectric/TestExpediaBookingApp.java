package com.expedia.bookings.test.robolectric;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowTelephonyManager;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.dagger.AppComponent;
import com.expedia.bookings.dagger.AppModule;
import com.expedia.bookings.dagger.DaggerTestAppComponent;
import com.expedia.bookings.dagger.TestCryptoModule;

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

	@Override
	protected AppComponent createAppComponent() {
		return DaggerTestAppComponent.builder()
			.appModule(new AppModule(RuntimeEnvironment.application))
			.testCryptoModule(new TestCryptoModule())
			.build();
	}
}
