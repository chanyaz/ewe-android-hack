package com.expedia.bookings.test.tests.pageModelsEspresso.common;

import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.ScreenshotUtils;
import com.expedia.bookings.test.utils.TestPreferences;
import com.expedia.bookings.test.utils.UserLocaleUtils;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/7/14.
 */
public class ScreenActions {
	private static final String TAG = "com.expedia.bookings.test";

	protected TestPreferences mPreferences;
	private int mScreenShotCount;
	private ScreenshotUtils mScreen;
	public UserLocaleUtils mLocaleUtils;

	protected static Instrumentation mInstrumentation;
	protected static Resources mRes;
	protected static Context mContext;

	private static final String mScreenshotDirectory = "Robotium-Screenshots";

	public static void enterLog(String TAG, String logText) {
		Log.v(TAG, logText);
	}

	public static void delay(int seconds) {
		seconds = seconds * 1000;
		try {
			Thread.sleep(seconds);
		}
		catch (InterruptedException e) {

		}
	}

	public static void delay() {
		delay(3);
	}

	public static void waitForViewToBeGone(View v, int timeoutMax) throws Exception {
		int count = 0;
		while (count < timeoutMax) {
			if (!v.isShown()) {
				break;
			}
			delay(1);
			count++;
		}
	}

	protected static ViewInteraction positiveButton() {
		return onView(withId(R.id.positive_button));
	}

	protected static ViewInteraction negativeButton() {
		return onView(withId(R.id.negative_button));
	}

}
