package com.expedia.bookings.test.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;
import com.robotium.solo.Solo;

public class CustomActivityInstrumentationTestCase<T> extends ActivityInstrumentationTestCase2 {

	@SuppressWarnings("unchecked")
	public CustomActivityInstrumentationTestCase(Class<T> activityClass) {
		super(activityClass);
	}

	protected Resources mRes;
	protected Context mContext;
	protected DisplayMetrics mMetric;
	protected HotelsTestDriver mDriver;
	protected HotelsUserData mUser;
	protected ConfigFileUtils mConfigFileUtils;
	protected TestPreferences mPreferences;
	private Solo mSolo;
	private ScreenshotUtils mScreen;

	@Override
	public void runTest() throws Throwable {
		try {
			super.runTest();
		}
		catch (Throwable t) {
			String failedTestMethodName = t.getStackTrace()[0].getMethodName();
			mScreen = new ScreenshotUtils("Robotium-Screenshots", mSolo);
			mScreen.screenshot(failedTestMethodName + "--FAILURE");
			throw t;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mSolo = new Solo(getInstrumentation());
		mContext = getActivity().getApplicationContext();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData(getInstrumentation());
		if (ConfigFileUtils.doesConfigFileExist()) {
			mConfigFileUtils = new ConfigFileUtils();
			mUser.setBookingServer(mConfigFileUtils.getConfigValue("Server"));
		}

		// Set Server API programatically
		SettingUtils.save(getActivity().getApplicationContext(),
			mRes.getString(R.string.preference_which_api_to_use_key), mUser.getBookingServer());

		// Disable v2 automatically.
		SettingUtils.save(getActivity().getApplicationContext(),
			"preference_disable_domain_v2_hotel_search", true);

		SettingUtils.save(getActivity().getApplicationContext(),
			R.string.preference_spoof_bookings, true);
	}

	public String getString(int resourceID) {
		return mRes.getString(resourceID);
	}

	public String getString(int id, Object... formatArgs) {
		return mRes.getString(id, formatArgs);
	}

	protected Activity getCurrentActivity() {
		return mSolo.getCurrentActivity();
	}

}
