package com.expedia.bookings.test.tests.flights;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.FlightsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;

public class IntegrationHappyPath extends ActivityInstrumentationTestCase2<SearchActivity> {
	public IntegrationHappyPath() {
		super(SearchActivity.class);
	}

	private static final String TAG = "Flights Integration Happy Path";
	private Resources mRes;
	DisplayMetrics mMetric;
	private FlightsTestDriver mDriver;
	private HotelsUserData mUser;

	protected void setUp() throws Exception {
		super.setUp();
		mUser = new HotelsUserData();
		mRes = getActivity().getResources();
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes);
	}

	// This test goes through a prototypical flight booking
	// UI flow, through checkout.
	// It runs pulling from the Integration API

	public void testMethod() throws Exception {
		mUser.mServerName = "Integration";
		FlightsHappyPath.execute(mDriver, mUser);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}