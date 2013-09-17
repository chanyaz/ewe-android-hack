package com.expedia.bookings.test.utils;

import com.expedia.bookings.R;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

public class CustomActivityInstrumentationTestCase<T> extends ActivityInstrumentationTestCase2 {

	@SuppressWarnings("unchecked")
	public CustomActivityInstrumentationTestCase(Class<T> activityClass) {
		super(activityClass);
	}

	protected Resources mRes;
	protected DisplayMetrics mMetric;
	protected HotelsTestDriver mDriver;
	protected HotelsUserData mUser;
	protected TestPreferences mPreferences;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData();
	}

	public String getString(int resourceID) {
		return mRes.getString(resourceID);
	}

	public String getString(int id, Object... formatArgs) {
		return mRes.getString(id, formatArgs);
	}

}
