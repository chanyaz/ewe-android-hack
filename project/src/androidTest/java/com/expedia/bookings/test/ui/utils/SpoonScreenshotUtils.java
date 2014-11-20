package com.expedia.bookings.test.ui.utils;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.Instrumentation;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.common.collect.Iterables;
import com.mobiata.android.Log;
import com.squareup.spoon.Spork;

/**
 * Created by dmadan on 7/3/14.
 */
public class SpoonScreenshotUtils {

	public static void screenshot(String tag, Instrumentation instrumentation) throws Throwable {
		Activity a = getCurrentActivity(instrumentation);
		if (a != null) {
			Spork.screenshot(a, instrumentation, tag);
		}
		else {
			Log.e("SpoonScreenshot", "No activity to take screenshot of");
		}
	}

	public static void screenshot(String tag, Instrumentation instrumentation, String className, String methodName) throws Throwable {
		Activity a = getCurrentActivity(instrumentation);
		if (a != null) {
			Spork.screenshot(a, instrumentation, tag, className, methodName);
		}
		else {
			Log.e("SpoonScreenshot", "No activity to take screenshot of");
		}
	}

	//Helper method to get current activity for spoon screenshot
	public static Activity getCurrentActivity(Instrumentation instrumentation) throws Throwable {
		instrumentation.waitForIdleSync();
		final AtomicReference<Activity> activity = new AtomicReference<Activity>();
		activity.set(null);
		instrumentation.runOnMainSync(new Runnable() {
			@Override
			public void run() {
				Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
				if (activities.size() > 0) {
					activity.set(Iterables.getOnlyElement(activities));
				}
			}
		});
		return activity.get();
	}
}
