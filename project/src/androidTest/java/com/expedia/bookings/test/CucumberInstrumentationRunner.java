package com.expedia.bookings.test;

import android.app.Application;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.test.runner.MonitoringInstrumentation;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.test.espresso.InstrumentationTestRunner;

import cucumber.api.android.CucumberInstrumentationCore;

import static android.content.Context.POWER_SERVICE;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.FULL_WAKE_LOCK;
import static android.os.PowerManager.ON_AFTER_RELEASE;

public class CucumberInstrumentationRunner extends MonitoringInstrumentation {

	//keeping a separate runner for cucumber tests for now. Forking is not compatible with cucumber yet. WIP.
	private final CucumberInstrumentationCore mInstrumentationCore = new CucumberInstrumentationCore(this);

	@Override
	public void onCreate(Bundle arguments) {
		ExpediaBookingApp.setIsInstrumentation(true);
		super.onCreate(arguments);

		mInstrumentationCore.create(arguments);
		start();
	}

	@Override
	public void onStart() {
		runOnMainSync(new Runnable() {
			@Override
			public void run() {
				Application app = (Application) getTargetContext().getApplicationContext();
				String simpleName = InstrumentationTestRunner.class.getSimpleName();
				// Wake up the screen.
				((PowerManager) app.getSystemService(POWER_SERVICE))
					.newWakeLock(FULL_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP | ON_AFTER_RELEASE, simpleName)
					.acquire();
			}
		});
		super.onStart();

		waitForIdleSync();
		mInstrumentationCore.start();
	}
}
