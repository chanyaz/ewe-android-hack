package com.expedia.bookings.test.tests.hotels;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.util.Log;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class IntegrationHappyPath extends ActivityInstrumentationTestCase2<SearchActivity> {

	public IntegrationHappyPath() {
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String TAG = "Hotels Integration Happy Path";

	private Solo mSolo;

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsRobotHelper mDriver;
	private HotelsUserData mUser;

	protected void setUp() throws Exception {
		super.setUp();
		mSolo = new Solo(getInstrumentation(), getActivity());
		//Log.configureLogging("ExpediaBookings", true);

		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
		mDriver = new HotelsRobotHelper(mSolo, mRes);
		mUser = new HotelsUserData();
	}

	// This test goes through a prototypical hotel booking
	// UI flow, through check out.
	// It runs pulling from the Production API

	public void testMethod() throws Exception {
		try {
			mUser.setHotelCityToRandomUSCity();
			mDriver.setAllowScreenshots(false);
			mDriver.setAllowOrientationChange(false);

			mDriver.changePOS(mDriver.mLocaleUtils.AMERICAN_LOCALES[5]);
			mDriver.changeAPI("Integration");
			mDriver.clearPrivateData();

			mDriver.launchHotels();
			mDriver.delay();
			mDriver.browseRooms(4, mUser.mHotelSearchCity, true);
		}
		catch (Error e) {
			mDriver.takeScreenshotUponFailure(e, TAG);
			throw e;
		}
		catch (Exception e) {
			mDriver.takeScreenshotUponFailure(e, TAG);
			throw e;
		}
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}

}
