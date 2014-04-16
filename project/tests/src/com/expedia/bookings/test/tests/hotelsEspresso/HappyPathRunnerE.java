package com.expedia.bookings.test.tests.hotelsEspresso;

import android.content.Intent;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.ConfigFileUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;
import com.mobiata.android.util.SettingUtils;

/**
 * Created by dmadan on 4/11/14.
 */
public class HappyPathRunnerE extends ActivityInstrumentationTestCase2<SearchActivity> {

	public HappyPathRunnerE() {
		super(SearchActivity.class);
	}

	private static final String TAG = "Hotels Production Happy Path";

	private Resources mRes;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;
	private ConfigFileUtils mConfigFileUtils;
	SearchActivity activity;

	protected void setUp() throws Exception {
		super.setUp();
		Intent intent = new Intent();
		intent.putExtra("isAutomation", true);
		setActivityIntent(intent);
		activity = getActivity();

		mRes = getActivity().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mUser = new HotelsUserData(getInstrumentation());
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
		HotelsHappyPathE.execute(mUser, numberOfHotelsToLookAt, activity);
	}
}
