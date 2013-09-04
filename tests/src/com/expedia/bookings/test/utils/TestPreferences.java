package com.expedia.bookings.test.utils;

public class TestPreferences {

	private boolean mAllowScreenshots;
	private boolean mAllowOrientationChange;

	public TestPreferences(boolean takeScreenshots, boolean doRotations) {
		mAllowScreenshots = takeScreenshots;
		mAllowOrientationChange = doRotations;
	}

	public void setScreenshotPermission(boolean takeScreenshots) {
		mAllowScreenshots = takeScreenshots;
	}

	public void setRotationpermission(boolean doRotations) {
		mAllowOrientationChange = doRotations;
	}

	public boolean getScreenshotPermission() {
		return mAllowScreenshots;
	}

	public boolean getRotationPermission() {
		return mAllowOrientationChange;
	}

}
