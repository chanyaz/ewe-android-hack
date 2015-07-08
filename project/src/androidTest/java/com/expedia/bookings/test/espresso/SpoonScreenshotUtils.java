package com.expedia.bookings.test.espresso;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.Instrumentation;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;

import android.support.test.runner.lifecycle.Stage;
import com.mobiata.android.Log;
import com.squareup.spoon.Spoon;

public class SpoonScreenshotUtils {

	public static void screenshot(String tag, Instrumentation instrumentation) throws Throwable {
		Activity a = getCurrentActivity();
		if (a != null) {
			Spoon.screenshot(a, instrumentation, tag);
		}
		else {
			Log.e("SpoonScreenshot", "No activity to take screenshot of", new Throwable());
		}
	}

	public static void screenshot(String tag, Instrumentation instrumentation, StackTraceElement testClass) throws Throwable {
		Activity a = getCurrentActivity();
		if (a != null) {
			Spoon.screenshot(a, instrumentation, tag, testClass);
		}
		else {
			Log.e("SpoonScreenshot", "No activity to take screenshot of", new Throwable());
		}
	}

	//Helper method to get current activity for spoon screenshot
	public static Activity getCurrentActivity() throws Throwable {
		Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
		instrumentation.waitForIdleSync();
		final AtomicReference<Activity> activity = new AtomicReference<Activity>();
		activity.set(null);
		instrumentation.runOnMainSync(new Runnable() {
			@Override
			public void run() {
				Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
				if (activities.size() == 1) {
					activity.set(((Activity) activities.toArray()[0]));
				}
				else {
					throw new RuntimeException("Expected exactly 1 activitiy, got: " + activities.size());
				}
			}
		});
		return activity.get();
	}

	public static boolean hasActiveActivity() throws Throwable {
		Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
		instrumentation.waitForIdleSync();

		final AtomicReference<Boolean> hasActivity = new AtomicReference<>();
		hasActivity.set(false);

		instrumentation.runOnMainSync(new Runnable() {
			@Override
			public void run() {
				Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
				if (activities.size() > 0) {
					hasActivity.set(true);
				}
			}
		});
		return hasActivity.get();
	}
}
