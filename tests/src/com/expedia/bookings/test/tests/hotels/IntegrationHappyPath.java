package com.expedia.bookings.test.tests.hotels;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

public class IntegrationHappyPath extends ActivityInstrumentationTestCase2<SearchActivity> {

	public IntegrationHappyPath() {
		super(SearchActivity.class);
	}

	private static final String TAG = "Hotels Integration Happy Path";

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsTestDriver mDriver;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;

	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData();
		mUser.setHotelCityToRandomUSCity();
		mUser.mBookingServer = "Integration";
	}

	// This test goes through a prototypical hotel booking
	// UI flow, through check out.
	// It runs pulling from the Integration API

	public void testMethod() throws Exception {
		HotelsHappyPath.execute(mDriver, mUser, 1);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
