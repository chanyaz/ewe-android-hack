package com.squareup.spoon;

import java.io.File;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Build;
import android.util.Log;

/**
 * Spork is an enhanced Spoon. Use Spork instead of Spoon if you want better screenshots on
 * API 18 and newer. It falls back on the regular Spoon code when on devices with API 17 or older.
 */
public class Spork {

	private static final String TAG = "Spork";

	public static void screenshot(Activity activity, Instrumentation instrumentation, String tag) {
		if (Build.VERSION.SDK_INT < 18) {
			Spoon.screenshot(activity, tag);
		}
		else {
			if (!ExtendableSpoon.TAG_VALIDATION.matcher(tag).matches()) {
				throw new IllegalArgumentException("Tag must match " + ExtendableSpoon.TAG_VALIDATION.pattern() + "");
			}
			try {
				File screenshotDirectory = ExtendableSpoon.obtainScreenshotDirectory(activity);
				String screenshotName = System.currentTimeMillis() + ExtendableSpoon.NAME_SEPARATOR + tag + ExtendableSpoon.EXTENSION;
				SporkCompatJellyBeanMR2.takeScreenshot(instrumentation, new File(screenshotDirectory, screenshotName));
				Log.d(TAG, "Captured screenshot '" + tag + "'.");
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to capture screenshot.", e);
			}
		}
	}

	//overloaded method with extra class name and method name parameters to get screenshot on failure
	public static void screenshot(Activity activity, Instrumentation instrumentation, String tag, String className, String methodName) {
		if (Build.VERSION.SDK_INT < 18) {
			ExtendableSpoon.screenshot(activity, tag, className, methodName);
		}
		else {
			if (!ExtendableSpoon.TAG_VALIDATION.matcher(tag).matches()) {
				throw new IllegalArgumentException("Tag must match " + ExtendableSpoon.TAG_VALIDATION.pattern() + "");
			}
			try {
				File screenshotDirectory = ExtendableSpoon.obtainScreenshotDirectory(activity, className, methodName);
				String screenshotName = System.currentTimeMillis() + ExtendableSpoon.NAME_SEPARATOR + tag + ExtendableSpoon.EXTENSION;
				SporkCompatJellyBeanMR2.takeScreenshot(instrumentation, new File(screenshotDirectory, screenshotName));
				Log.d(TAG, "Captured screenshot '" + tag + "'.");
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to capture screenshot.", e);
			}
		}
	}
}
