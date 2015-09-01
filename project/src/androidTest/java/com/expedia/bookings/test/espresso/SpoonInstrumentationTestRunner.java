package com.expedia.bookings.test.espresso;

import android.app.Application;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.os.PowerManager;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.utils.ExpediaNetUtils;

import android.support.test.runner.AndroidJUnitRunner;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.FULL_WAKE_LOCK;
import static android.os.PowerManager.ON_AFTER_RELEASE;

public class SpoonInstrumentationTestRunner extends AndroidJUnitRunner {

	@Override
	public void onCreate(Bundle args) {
		ExpediaBookingApp.setIsInstrumentation(true);
		ExpediaNetUtils.setFake(true, true);
		super.onCreate(args);
	}

	@Override
	public void onStart() {
		runOnMainSync(new Runnable() {
			@Override
			public void run() {
				Application app = (Application) getTargetContext().getApplicationContext();
				String simpleName = SpoonInstrumentationTestRunner.class.getSimpleName();

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

		super.onStart();
	}
}
