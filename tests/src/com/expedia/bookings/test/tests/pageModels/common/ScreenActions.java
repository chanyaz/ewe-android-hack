package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.ScreenshotUtils;
import com.expedia.bookings.test.utils.TestPreferences;
import com.expedia.bookings.test.utils.UserLocaleUtils;
import com.jayway.android.robotium.solo.Solo;

/*
 * This is the parent class for all page models.
 * It contains actions that should be considered
 * universal across all or most EBad UI.
 */

public class ScreenActions extends Solo {
	private static final String TAG = "com.expedia.bookings.test";

	protected TestPreferences mPreferences;
	private int mScreenShotCount;
	private ScreenshotUtils mScreen;
	protected int mScreenWidth;
	protected int mScreenHeight;
	public UserLocaleUtils mLocaleUtils;

	protected Instrumentation mInstrumentation;
	protected Resources mRes;
	protected Context mContext;

	private static final String mScreenshotDirectory = "Robotium-Screenshots";

	public ScreenActions(Instrumentation instrumentation, Activity activity, Resources res, TestPreferences preferences) {
		super(instrumentation, activity);
		mPreferences = preferences;
		mScreenShotCount = 1;
		mLocaleUtils = new UserLocaleUtils(res);
		mRes = res;
		mInstrumentation = instrumentation;
		mScreen = new ScreenshotUtils(mScreenshotDirectory, this);
		mScreenWidth = mRes.getDisplayMetrics().widthPixels;
		mScreenHeight = mRes.getDisplayMetrics().heightPixels;
	}

	public void enterLog(String TAG, String logText) {
		Log.v(TAG, logText);
	}

	public void delay(int seconds) {
		seconds = seconds * 1000;
		sleep(seconds);
	}

	public void delay() {
		delay(3);
	}

	public void setScreenshotCount(int count) {
		mScreenShotCount = count;
	}

	public void screenshot(String fileName) { //screenshot is saved to device SD card.
		if (mPreferences.getScreenshotPermission()) {
			String currentLocale = mRes.getConfiguration().locale.toString();
			enterLog(TAG, "Taking screenshot: " + fileName);
			mScreen.screenshot(currentLocale + " " + String.format("%02d", mScreenShotCount) + " " + fileName);
			mScreenShotCount++;
		}
	}

	// Log failure upon catching Throwable, and create and store screenshot
	// Maintain mAllowScreenshots state from before screenshot is taken
	public void takeScreenshotUponFailure(Throwable e, String testName) {
		Log.e(TAG, "Taking screenshot due to " + e.getClass().getName(), e);
		final boolean currentSSPermission = mPreferences.getScreenshotPermission();
		if (!currentSSPermission) {
			mPreferences.setScreenshotPermission(true);
		}
		screenshot(testName + "-FAILURE");
		if (!currentSSPermission) {
			mPreferences.setScreenshotPermission(false);
		}
	}

	public void landscape() {
		if (mPreferences.getRotationPermission()) {
			delay(2);
			setActivityOrientation(Solo.LANDSCAPE);
			delay(2);
		}
	}

	public void portrait() {
		if (mPreferences.getRotationPermission()) {
			delay(2);
			setActivityOrientation(Solo.PORTRAIT);
			delay(2);
		}
	}

	public void waitForStringToBeGone(String s, int timeoutMax) throws Exception {
		int count_max = timeoutMax;
		int count = 0;
		;
		while (searchText(s, 1, false, true) && count < count_max) {
			delay(1);
			count++;
		}
		if (searchText(s, 1, false, true)) {
			throw new Exception("String never went away: " + s);
		}
	}

	public void waitForStringToBeGone(String s) throws Exception {
		waitForStringToBeGone(s, 20);
	}

	public void waitForViewToBeGone(View v, int timeoutMax) throws Exception {
		int count = 0;
		while (count < timeoutMax) {
			if (!v.isShown()) {
				break;
			}
			delay(1);
			count++;
		}
	}

	protected View positiveButton() {
		return getView(R.id.positive_button);
	}

	protected View negativeButton() {
		return getView(R.id.negative_button);
	}
}
