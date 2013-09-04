package com.expedia.bookings.test.utils;

public class TestPreferences {

	private boolean mAllowScreenshots;
	private boolean mAllowOrientationChange;

	public TestPreferences() {

	}

	public static TestPreferences generateTestPreferences() {
		return new TestPreferences();
	}

	public TestPreferences setScreenshotPermission(boolean takeScreenshots) {
		mAllowScreenshots = takeScreenshots;
		return this;
	}

	public TestPreferences setRotationPermission(boolean doRotations) {
		mAllowOrientationChange = doRotations;
		return this;
	}

	public boolean getScreenshotPermission() {
		return mAllowScreenshots;
	}

	public boolean getRotationPermission() {
		return mAllowOrientationChange;
	}

}
