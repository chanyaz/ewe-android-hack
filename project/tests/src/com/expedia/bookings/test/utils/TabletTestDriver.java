package com.expedia.bookings.test.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModels.tablet.SearchScreen;

public class TabletTestDriver extends ScreenActions {

	private SearchScreen mTabletSearchScreen;

	public TabletTestDriver(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public SearchScreen searchScreen() {
		if (mTabletSearchScreen == null) {
			mTabletSearchScreen = new SearchScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mTabletSearchScreen;
	}

}
