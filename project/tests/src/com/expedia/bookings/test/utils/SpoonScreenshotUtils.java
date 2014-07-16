package com.expedia.bookings.test.utils;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.Instrumentation;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.common.collect.Iterables;
import com.squareup.spoon.Spork;

/**
 * Created by dmadan on 7/3/14.
 */
public class SpoonScreenshotUtils {

	public static void screenshot(String tag, Instrumentation instrumentation) throws Throwable {
		Spork.screenshot(getCurrentActivity(instrumentation), instrumentation, tag);
	}

	public static void screenshot(String tag, Instrumentation instrumentation, String className, String methodName) throws Throwable {
		Spork.screenshot(getCurrentActivity(instrumentation), instrumentation, tag, className, methodName);
	}

	//Helper method to get current activity for spoon screenshot
	public static Activity getCurrentActivity(Instrumentation instrumentation) throws Throwable {
		instrumentation.waitForIdleSync();
		final AtomicReference<Activity> activity = new AtomicReference<Activity>();
		instrumentation.runOnMainSync(new Runnable() {
			@Override
			public void run() {
				Collection<Activity> activites = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
				activity.set(Iterables.getOnlyElement(activites));
			}
		});
		return activity.get();
	}
}