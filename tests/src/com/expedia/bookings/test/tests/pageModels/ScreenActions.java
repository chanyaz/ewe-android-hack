package com.expedia.bookings.test.tests.pageModels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.ScreenshotUtils;
import com.expedia.bookings.test.utils.UserLocaleUtils;
import com.jayway.android.robotium.solo.Solo;

/*
 * This is the parent class for all page models.
 * It contains actions that should be considered
 * universal across all or most EBad UI.
 */

public class ScreenActions extends Solo{
	private static final String TAG = "com.expedia.bookings.test";
	private boolean mAllowScreenshots;
	private boolean mAllowOrientationChange;
	private boolean mWriteEventsToFile;
	private int mScreenShotCount;
	protected Resources mRes;
	protected Context mContext;
	private HotelsUserData mUser; //user info container
	private ScreenshotUtils mScreen;
	private int mScreenWidth;
	private int mScreenHeight;
	public UserLocaleUtils mLocaleUtils;

	private static final String mScreenshotDirectory = "Robotium-Screenshots";


	public ScreenActions(Instrumentation instrumentation, Activity activity, Resources res) {
		super(instrumentation, activity);
		mAllowScreenshots = false;
		mAllowOrientationChange = false;
		mWriteEventsToFile = false;
		mScreenShotCount = 1;
		mRes = res;
		mLocaleUtils = new UserLocaleUtils(res);

		mScreen = new ScreenshotUtils(mScreenshotDirectory, this);
		mScreenWidth = mRes.getDisplayMetrics().widthPixels;
		mScreenWidth = mRes.getDisplayMetrics().heightPixels;
	}

	public void enterLog(String TAG, String logText) {
		Log.v(TAG, "Robotium: " + logText);
	}

	public void delay(int time) { //Enter time in seconds
		time = time * 1000;
		sleep(time);
	}

	public void delay() { //Defaults to 3 seconds
		sleep(3000);
	}

	public void setAllowScreenshots(Boolean allowed) {
		mAllowScreenshots = allowed;
	}

	public void setAllowOrientationChange(Boolean allowed) {
		mAllowOrientationChange = allowed;
	}

	public void setWriteEventsToFile(Boolean allowed) {
		mWriteEventsToFile = allowed;
	}

	public void setScreenshotCount(int count) {
		mScreenShotCount = count;
	}

	public void screenshot(String fileName) { //screenshot is saved to device SD card.
		if (mAllowScreenshots) {
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
		final boolean currentSSPermission = mAllowScreenshots;
		if (!currentSSPermission) {
			mAllowScreenshots = true;
		}
		screenshot(testName + "-FAILURE");
		if (!currentSSPermission) {
			mAllowScreenshots = false;
		}
	}

	public void landscape() {
		if (mAllowOrientationChange) {
			delay(2);
			setActivityOrientation(Solo.LANDSCAPE);
			delay(2);
		}
	}

	public void portrait() {
		if (mAllowOrientationChange) {
			delay(2);
			setActivityOrientation(Solo.PORTRAIT);
			delay(2);
		}
	}

	public void waitForStringToBeGone(int id, int timeoutMax) throws Exception {
		int string_id = id;
		int count_max = timeoutMax;
		int count = 0;
		String target = mRes.getString(string_id);
		while (searchText(target, 1, false, true) && count < count_max) {
			delay(1);
			count++;
		}
		if (searchText(mRes.getString(string_id), 1, false, true)) {
			throw new Exception("String never went away: " + target);
		}
	}

	public void waitForStringToBeGone(int id) throws Exception {
		waitForStringToBeGone(id, 20);
	}
}
