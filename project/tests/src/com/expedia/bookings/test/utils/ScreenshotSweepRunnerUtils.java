package com.expedia.bookings.test.utils;

import ErrorsAndExceptions.OutOfPOSException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

public class ScreenshotSweepRunnerUtils {
	private static String TAG = ScreenshotSweepRunnerUtils.class.getName();

	public static void run(ScreenshotMethodInterface runner, Resources res) {
		try {
			runner.execute();
		}
		catch (OutOfPOSException e) {
			Log.e(TAG, "POSHappyPath out of POSs. Throwing exception", e);
			throw e;
		}
		catch (RuntimeException r) {
			Log.e(TAG, "RuntimeException", r);
			throw r;
		}
		catch (Exception e) {
			Configuration config = res.getConfiguration();
			Log.e(TAG, "Exception on Locale: " + config.locale.toString(), e);
		}
		catch (Error e) {
			Configuration config = res.getConfiguration();
			Log.e(TAG, "Error on Locale: " + config.locale.toString(), e);
		}
	}
}
