package com.expedia.bookings.test.tests.flights;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.ConfigFileUtils;
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
	private ConfigFileUtils mConfigFileUtils;

	protected void setUp() throws Exception {
		super.setUp();
		mUser = new HotelsUserData();
		mRes = getActivity().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mConfigFileUtils = new ConfigFileUtils();

	}

	// This test goes through a prototypical flight booking
	// UI flow, up to finally checking out.
	// It runs pulling from the Production API

	public void testMethod() throws Exception {
		mUser.setAirportsToRandomUSAirports();
		mUser.setBookingServer("Proxy");
		mUser.setServerIP(mConfigFileUtils.getConfigValue("Mock Proxy IP"));
		mUser.setServerPort(mConfigFileUtils.getConfigValue("Mock Proxy Port"));
		FlightsHappyPath.execute(mDriver, mUser);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}