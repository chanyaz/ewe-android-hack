package com.expedia.bookings.test.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.hotels.HotelsSearchScreen;

public class HotelsTestDriver extends TestDriver {

	private HotelsSearchScreen mHotelsSearchScreen;

	public HotelsTestDriver(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public HotelsSearchScreen hotelsSearchScreen() {
		if (mHotelsSearchScreen == null) {
			mHotelsSearchScreen = new HotelsSearchScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mHotelsSearchScreen;
	}

}
