package com.expedia.bookings.test.utils;

import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mContext = getActivity().getApplicationContext();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData();
		mConfigFileUtils = new ConfigFileUtils();
		mUser.setBookingServer(mConfigFileUtils.getConfigValue("Server"));

		// Set Server API programatically
		SettingUtils.save(this.getActivity().getApplicationContext(),
				mRes.getString(R.string.preference_which_api_to_use_key), mUser.getBookingServer());
	}

	public String getString(int resourceID) {
		return mRes.getString(resourceID);
	}

	public String getString(int id, Object... formatArgs) {
		return mRes.getString(id, formatArgs);
	}

}
