package com.expedia.bookings.test.robolectric;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowTelephonyManager;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.dagger.DaggerTestAppComponent;
import com.expedia.bookings.dagger.DaggerTestFlightComponent;
import com.expedia.bookings.dagger.TestAppModule;

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
		defaultFlightComponents();
		defaultTripComponents();
		defaultHotelComponents();
		defaultTravelerComponent();
		defaultRailComponents();
		defaultPackageComponents();
		defaultLXComponents();
		defaultLaunchComponents();
	}

	@Override
	protected void defaultAppComponents() {
		setAppComponent(DaggerTestAppComponent.builder()
			.appModule(new TestAppModule(this))
			.build());
	}

	@Override
	public void defaultFlightComponents() {
		setFlightComponent(DaggerTestFlightComponent.builder()
			.appComponent(appComponent())
			.build());
	}
}
