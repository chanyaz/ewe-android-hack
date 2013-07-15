package com.expedia.bookings.test.tests.flights;

import ErrorsAndExceptions.IntegrationFailureError;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class ProductionHappyPath extends ActivityInstrumentationTestCase2<SearchActivity> {
	public ProductionHappyPath() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private Solo mSolo;
	private static final String TAG = "RotateHappyPath";
	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsRobotHelper mDriver;
	private HotelsUserData mUser;

	protected void setUp() throws Exception {
		super.setUp();
		mSolo = new Solo(getInstrumentation(), getActivity());
		mUser = new HotelsUserData();
		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
		mDriver = new HotelsRobotHelper(mSolo, mRes, mUser);

		mDriver.setScreenshotCount(1);
		mDriver.setAllowOrientationChange(false);
		mDriver.setWriteEventsToFile(false);
		mUser.setAirportsToRandomUSAirports();
	}

	// This test goes through a prototypical flight booking
	// UI flow, up to finally checking out.
	// It runs pulling from the Production API
	
	public void testMethod() throws Exception {
		mSolo.clickOnScreen(50, 50);
		mDriver.ignoreSweepstakesActivity();
		mDriver.changePOS(mDriver.mLocaleUtils.FLIGHTS_LOCALES[2]);
		mDriver.setSpoofBookings();
		mDriver.changeAPI("Production");

		try {
			mDriver.flightsHappyPath(mUser.mDepartureAirport, mUser.mArrivalAirport, 1, false, false);
		}
		catch (IntegrationFailureError e) {
			fail(e.getMessage());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");
		mSolo.finishOpenedActivities();
	}
}