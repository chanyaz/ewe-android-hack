package com.expedia.bookings.test.tests.flightsEspresso;


import android.content.Intent;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.ConfigFileUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

/**
 * Created by dmadan on 4/8/14.
 */
public class HappyPathRunnerE extends ActivityInstrumentationTestCase2<SearchActivity> {
	public HappyPathRunnerE() {
		super(SearchActivity.class);
	}

	private static final String TAG = "Flights Production Happy Path";

	private HotelsUserData mUser;
	private Resources mRes;
	private TestPreferences mPreferences;
	private ConfigFileUtils mConfigFileUtils;

	protected void setUp() throws Exception {
		super.setUp();
		Intent intent = new Intent();
		intent.putExtra("isAutomation", true);
		setActivityIntent(intent);
		getActivity();
		mUser = new HotelsUserData(getInstrumentation());
		mRes = getActivity().getResources();
		mPreferences = new TestPreferences();
		mConfigFileUtils = new ConfigFileUtils();
		mPreferences.setRotationPermission(mConfigFileUtils.getBooleanConfigValue("Rotations"));
		mPreferences.setScreenshotPermission(mConfigFileUtils.getBooleanConfigValue("Screenshots"));
	}

	// This test goes through a prototypical flight booking
	// UI flow, up to finally checking out.
	// It runs pulling from the server determined by the config.json file

	public void testMethod() throws Exception {
		mUser.setAirportsToRandomUSAirports();
		mUser.setBookingServer(mConfigFileUtils.getConfigValue("Server"));
		mUser.setServerIP(mConfigFileUtils.getConfigValue("Mock Proxy IP"));
		mUser.setServerPort(mConfigFileUtils.getConfigValue("Mock Proxy Port"));
		FlightsHappyPathE.execute(mUser);
	}
}
