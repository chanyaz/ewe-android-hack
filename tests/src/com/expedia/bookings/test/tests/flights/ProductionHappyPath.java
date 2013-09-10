package com.expedia.bookings.test.tests.flights;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.FlightsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

public class ProductionHappyPath extends ActivityInstrumentationTestCase2<SearchActivity> {
	public ProductionHappyPath() {
		super(SearchActivity.class);
	}

	private static final String TAG = "Flights Production Happy Path";
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
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
	}

	// This test goes through a prototypical flight booking
	// UI flow, up to finally checking out.
	// It runs pulling from the Production API

	public void testMethod() throws Exception {
		mUser.setAirportsToRandomUSAirports();
		mUser.mServerName = "Proxy";
		mUser.mProxyIP = "172.17.249.23";
		mUser.mProxyPort = "3000";
		FlightsHappyPath.execute(mDriver, mUser);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}