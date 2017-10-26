package com.expedia.account.test.utils;

import android.app.Application;
import android.app.KeyguardManager;
import android.os.PowerManager;
import android.support.test.runner.AndroidJUnitRunner;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.FULL_WAKE_LOCK;
import static android.os.PowerManager.ON_AFTER_RELEASE;

public class InstrumentationTestRunner extends AndroidJUnitRunner {
	private PowerManager.WakeLock wakeLock;

	@Override
	public void onStart() {
		runOnMainSync(new Runnable() {
			@Override
			public void run() {
				Application app = (Application) getTargetContext().getApplicationContext();
				String name = InstrumentationTestRunner.class.getSimpleName();

				// Unlock the device so that the tests can input keystrokes.
				KeyguardManager keyguard = (KeyguardManager) app.getSystemService(KEYGUARD_SERVICE);
				keyguard.newKeyguardLock(name).disableKeyguard();

				// Wake up the screen.
				PowerManager power = (PowerManager) app.getSystemService(POWER_SERVICE);
				wakeLock = power.newWakeLock(FULL_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP | ON_AFTER_RELEASE, name);
				wakeLock.acquire();
			}
		});

		super.onStart();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		wakeLock.release();
	}
}