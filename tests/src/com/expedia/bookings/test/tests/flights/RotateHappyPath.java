package com.expedia.bookings.test.tests.flights;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.FlightsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

public class RotateHappyPath extends ActivityInstrumentationTestCase2<SearchActivity> {
	public RotateHappyPath() { //Default constructor
		super(SearchActivity.class);
	}

	private static final String TAG = "Flights Rotate Happy Path";
	private Resources mRes;
	DisplayMetrics mMetric;
	private FlightsTestDriver mDriver;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;

	protected void setUp() throws Exception {
		super.setUp();
		mUser = new HotelsUserData();
		mRes = getActivity().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(true);
		mPreferences.setScreenshotPermission(false);
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
	}

	// This test goes through a prototypical flight booking
	// UI flow, through checkout.
	// It runs pulling from the Integration API

	public void testMethod() throws Exception {
		FlightsHappyPath.execute(mDriver, mUser);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
