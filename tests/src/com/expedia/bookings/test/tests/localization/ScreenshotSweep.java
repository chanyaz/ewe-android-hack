// POS/Locale Screenshot Sweep
// Made for Expedia Hotels Android App.
// Kevin Carpenter
// Some code derived from Daniel Lew's LocalizationTests.java

package com.expedia.bookings.test.tests.localization;

import java.util.Locale;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class ScreenshotSweep extends
		ActivityInstrumentationTestCase2<SearchActivity> {

	public ScreenshotSweep() { // Default constructor
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

	public void testBookingsAPAC() throws Exception {
		testBookings(mDriver.APAC_LOCALES);
	}

	public void testBookingsWestern() throws Exception {
		testBookings(mDriver.WESTERN_LOCALES);
	}

	public void testBookingsAmericas() throws Exception {
		testBookings(mDriver.AMERICAN_LOCALES);
	}

	private void testBookings(Locale[] locales) throws Exception {
		mDriver.setAllowScreenshots(true);
		mDriver.setAllowOrientationChange(false);

		mDriver.changeAPI("Production");

		for (int i = 0; i < locales.length; i++) {
			
			mDriver.setScreenshotCount(1);
			mDriver.enterLog(TAG, "Starting sweep of " + locales[i].toString());
			System.gc();
			Locale testingLocale = locales[i];
			mDriver.setLocale(testingLocale);

			mDriver.setLocale(testingLocale);
			mDriver.delay();
			mDriver.changePOS(locales[i]);
			mDriver.setSpoofBookings();

			mDriver.launchHotels();
			if (mSolo.searchText(mRes.getString(R.string.NEW_SEARCH))) {
				mSolo.clickOnText(mRes.getString(R.string.NEW_SEARCH));
			}
			mDriver.delay();
			System.gc();
			mDriver.pressCalendar();
			mDriver.pressGuestPicker();
			mDriver.selectLocation("San Francisco");

			mDriver.pressSort();
			mDriver.filterFor("a");
			mDriver.selectHotel(2);
			mDriver.delay();

			mDriver.checkReviews();
			mDriver.pressBookRoom();
			mDriver.selectRoom(0);
			System.gc();
			mDriver.delay();

			mDriver.logInAndBook();
			mDriver.setLocale(testingLocale);
			mDriver.delay(5);
			mSolo.goBack();

			mDriver.checkFlightsScreen();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}

}