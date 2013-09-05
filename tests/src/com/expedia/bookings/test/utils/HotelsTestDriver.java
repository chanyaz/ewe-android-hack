package com.expedia.bookings.test.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelsReviewsScreen;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelsSearchScreen;

public class HotelsTestDriver extends TestDriver {

	private HotelsSearchScreen mHotelsSearchScreen;
	private HotelsDetailsScreen mHotelsDetailsScreen;
	private HotelsReviewsScreen mHotelsReviewsScreen;

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

	public HotelsDetailsScreen hotelsDetailsScreen() {
		if (mHotelsDetailsScreen == null) {
			mHotelsDetailsScreen = new HotelsDetailsScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mHotelsDetailsScreen;
	}

	public HotelsReviewsScreen hotelsReviewsScreen() {
		if (mHotelsReviewsScreen == null) {
			mHotelsReviewsScreen = new HotelsReviewsScreen(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mHotelsReviewsScreen;
	}

}
