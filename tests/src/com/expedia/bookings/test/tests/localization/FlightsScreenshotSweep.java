package com.expedia.bookings.test.tests.localization;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.jayway.android.robotium.solo.Solo;

public class FlightsScreenshotSweep extends
		ActivityInstrumentationTestCase2<SearchActivity> {

	public FlightsScreenshotSweep() { // Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String TAG = "FlightsSweep";

	private Solo mSolo;

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsRobotHelper mDriver;

	protected void setUp() throws Exception {
		super.setUp();
		mSolo = new Solo(getInstrumentation(), getActivity());

		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
		mDriver = new HotelsRobotHelper(mSolo, mRes);
		mDriver.setAllowOrientationChange(false);
		mDriver.setAllowScreenshots(true);
		mDriver.setScreenshotCount(0);
	}

	////////////////////////////////////////////////////////////////
	// Test Driver	
	
	public void testMethod() throws Exception {
		for(int i = 0; i < mDriver.FLIGHTS_LOCALES.length; i++) {
			mDriver.changePOS(mDriver.FLIGHTS_LOCALES[i]);
			mDriver.flightsHappyPath("LAX", "SEA", false);
		}
	}
	
	
	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");
		mSolo.finishOpenedActivities();
	}

}
