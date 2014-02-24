package com.expedia.bookings.test.tests.hotels;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.ConfigFileUtils;
import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;
import com.mobiata.android.util.SettingUtils;

public class HappyPathRunner extends ActivityInstrumentationTestCase2<SearchActivity> {

	public HappyPathRunner() {
		super(SearchActivity.class);
	}

	private static final String TAG = "Hotels Production Happy Path";

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsTestDriver mDriver;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;
	private ConfigFileUtils mConfigFileUtils;

	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData(getActivity());
		mUser.setHotelCityToRandomUSCity();
		mConfigFileUtils = new ConfigFileUtils();
		mPreferences.setRotationPermission(mConfigFileUtils.getBooleanConfigValue("Rotations"));
		mPreferences.setScreenshotPermission(mConfigFileUtils.getBooleanConfigValue("Screenshots"));
		mUser.setBookingServer(mConfigFileUtils.getConfigValue("Server"));
		mUser.setServerIP(mConfigFileUtils.getConfigValue("Mock Proxy IP"));
		mUser.setServerPort(mConfigFileUtils.getConfigValue("Mock Proxy Port"));
		// Disable v2 automatically.
		SettingUtils.save(getActivity().getApplicationContext(),
			"preference_disable_domain_v2_hotel_search", true);
	}

	// This test goes through a prototypical hotel booking
	// UI flow, through check out.
	// It runs pulling from the Integration API

	public void testMethod() throws Exception {
		final int numberOfHotelsToLookAt = mConfigFileUtils.getIntegerConfigValue("Hotel Count");
		HotelsHappyPath.execute(mDriver, mUser, numberOfHotelsToLookAt);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}