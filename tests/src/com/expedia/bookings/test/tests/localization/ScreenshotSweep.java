// POS/Locale Screenshot Sweep
// Made for Expedia Hotels Android App.
// Kevin Carpenter
// Some code derived from Daniel Lew's LocalizationTests.java

package com.expedia.bookings.test.tests.localization;

import java.util.Locale;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class ScreenshotSweep extends ActivityInstrumentationTestCase2<SearchActivity> {

	public ScreenshotSweep() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String TAG = "SearchTest";

	private Solo mSolo;

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsRobotHelper mDriver;

	protected void setUp() throws Exception {
		super.setUp();
		mSolo = new Solo(getInstrumentation(), getActivity());
		//Log.configureLogging("ExpediaBookings", true);

		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
		mDriver = new HotelsRobotHelper(mSolo, mRes);
	}

	//////////////////////////////////////////////////////////////// 
	// Test Driver
	
	public void testBookingsAPAC() throws Exception{
		testBookings(mDriver.APAC_LOCALES);
	}
	
	public void testBookingsWestern() throws Exception{
		testBookings(mDriver.WESTERN_LOCALES);
	}
	
	public void testBookingsAmericas() throws Exception{
		testBookings(mDriver.AMERICAN_LOCALES);
	}
	
	private void testBookings(Locale[] locales) throws Exception {
		mDriver.setAllowScreenshots(true);
		mDriver.setAllowOrientationChange(false);

		for (int i = 0; i < mDriver.APAC_LOCALES.length; i++) {
			mSolo.finishInactiveActivities();
			mDriver.enterLog(TAG, "Starting sweep of " + locales[i].toString());

			Locale testingLocale = locales[i];
			mDriver.setLocale(testingLocale);

			mDriver.setScreenshotCount(1);
			mDriver.setLocale(testingLocale);
			mDriver.delay();
			mDriver.changePOS(locales[i]);

			mDriver.changeAPI("Production");
			mDriver.delay(1);
			mDriver.pressCalendar();
			mDriver.pressGuestPicker();

			mDriver.selectLocation("New York City");

			mDriver.pressSort();
			mDriver.filterFor("Hilton");

			mDriver.selectHotel(3);
			mDriver.delay();
			mDriver.checkReviews();
			mDriver.pressBookRoom();
			mDriver.selectRoom(0);

			mDriver.delay();

			mDriver.bookingScreenShots();
			mDriver.logInAndBook();
			mDriver.captureInfoScreen();

		}
	}

	

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}

}