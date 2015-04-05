package com.expedia.bookings.test.ui.utils;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.Instrumentation;

import android.support.test.internal.runner.lifecycle.ActivityLifecycleMonitorRegistry;

import android.support.test.runner.lifecycle.Stage;
import com.android.support.test.deps.guava.collect.Iterables;
import com.mobiata.android.Log;
import com.squareup.spoon.Spoon;

/**
 * Created by dmadan on 7/3/14.
 */
public class SpoonScreenshotUtils {

	public static void screenshot(String tag, Instrumentation instrumentation) throws Throwable {
		Activity a = getCurrentActivity(instrumentation);
		if (a != null) {
			Spoon.screenshot(a, instrumentation, tag);
		}
		else {
			Log.e("SpoonScreenshot", "No activity to take screenshot of");
		}
	}

	public static void screenshot(String tag, Instrumentation instrumentation, StackTraceElement testClass) throws Throwable {
		Activity a = getCurrentActivity(instrumentation);
		if (a != null) {
			Spoon.screenshot(a, instrumentation, tag, testClass);
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

	public static boolean hasActiveActivity(Instrumentation instrumentation) throws Throwable {
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
