package com.expedia.bookings.test.utils;

public class TestPreferences {

	private boolean mAllowScreenshots;
	private boolean mAllowOrientationChange;

	public TestPreferences() {

	}

	public void setScreenshotPermission(boolean takeScreenshots) {
		mAllowScreenshots = takeScreenshots;
	}

	public void setRotationPermission(boolean doRotations) {
		mAllowOrientationChange = doRotations;
	}

	public boolean getScreenshotPermission() {
		return mAllowScreenshots;
	}

	public boolean getRotationPermission() {
		return mAllowOrientationChange;
	}

}
