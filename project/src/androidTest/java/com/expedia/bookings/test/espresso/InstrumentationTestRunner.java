package com.expedia.bookings.test.espresso;

import android.app.Application;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnitRunner;

import com.linkedin.android.testbutler.TestButler;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.test.BuildConfig;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.FULL_WAKE_LOCK;
import static android.os.PowerManager.ON_AFTER_RELEASE;

public class InstrumentationTestRunner extends AndroidJUnitRunner {

	@Override
	public void onCreate(Bundle args) {
		if (!BuildConfig.IS_SCREENSHOT_BUILD) {
			ExpediaBookingApp.setIsInstrumentation(true);
		}
		super.onCreate(args);
	}

	@Override
	public void onStart() {
		runOnMainSync(new Runnable() {
			@Override
			public void run() {
				Application app = (Application) getTargetContext().getApplicationContext();
				String simpleName = InstrumentationTestRunner.class.getSimpleName();

				// Unlock the device so that the tests can input keystrokes.
				((KeyguardManager) app.getSystemService(KEYGUARD_SERVICE))
					.newKeyguardLock(simpleName)
					.disableKeyguard();

				// Wake up the screen.
				((PowerManager) app.getSystemService(POWER_SERVICE))
					.newWakeLock(FULL_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP | ON_AFTER_RELEASE, simpleName)
					.acquire();
			}
		});

		try {
			TestButler.setup(InstrumentationRegistry.getTargetContext());
			TestButler.setLocationMode(Settings.Secure.LOCATION_MODE_OFF);
			TestButler.setWifiState(false);

		}
		catch (IllegalStateException e) {
			//do not fail the tests if test butler apk is missing
		}
		super.onStart();
	}

	@Override
	public void finish(int resultCode, Bundle results) {
		try {
			TestButler.teardown(InstrumentationRegistry.getTargetContext());
		}
		catch (IllegalStateException e) {
			//do not fail the tests if test butler apk is missing
		}

		super.finish(resultCode, results);
	}

}
