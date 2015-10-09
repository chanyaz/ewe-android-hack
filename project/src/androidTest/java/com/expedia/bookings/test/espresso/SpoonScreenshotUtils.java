package com.expedia.bookings.test.espresso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import com.squareup.spoon.Spoon;

public class SpoonScreenshotUtils {

	public static void screenshot(String tag, Instrumentation instrumentation) throws Throwable {
		Activity a = getCurrentActivity();
		Spoon.screenshot(a, instrumentation, tag);
	}

	public static void screenshot(String tag, Instrumentation instrumentation, StackTraceElement testClass)
		throws Throwable {
		List<Activity> activities = getResumedActivities();
		for (int i = 0; i < activities.size(); i++) {
			Spoon.screenshot(activities.get(i), instrumentation, tag + "_" + i, testClass);
		}
	}

	//Helper method to get current activity for spoon screenshot
	public static Activity getCurrentActivity() throws Throwable {
		List<Activity> activities = getResumedActivities();

		if (activities.size() == 1) {
			return activities.get(0);
		}
		else {
			throw new RuntimeException("Expected exactly 1 activitiy, got: " + activities.size());
		}
	}

	private static List<Activity> getResumedActivities() throws Throwable {
		Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
		final AtomicReference<List<Activity>> ref = new AtomicReference<>();
		ref.set(new ArrayList<Activity>());

		instrumentation.waitForIdleSync();
		instrumentation.runOnMainSync(new Runnable() {
			@Override
			public void run() {
				Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
					.getActivitiesInStage(Stage.RESUMED);
				List<Activity> activities = new ArrayList<>(resumedActivities);
				ref.set(activities);
			}
		});

		return ref.get();
	}

	public static boolean hasActiveActivity() throws Throwable {
		List<Activity> activities = getResumedActivities();
		return activities.size() > 0;
	}
}
