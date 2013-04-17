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
	// Test Drivers
	
	// Run screenshot sweep on APAC locales
	public void testBookingsAPAC() throws Exception {
		testBookings(mDriver.mLocaleUtils.APAC_LOCALES);
	}
	
	// Run screenshot sweep on Western locales
	public void testBookingsWestern() throws Exception {
		testBookings(mDriver.mLocaleUtils.WESTERN_LOCALES);
	}

	// Run screenshot sweep on North, South, and Central American locales
	public void testBookingsAmericas() throws Exception {
		testBookings(mDriver.mLocaleUtils.AMERICAN_LOCALES);
	}

	private void testBookings(Locale[] locales) throws Exception {
		mDriver.setAllowScreenshots(true);
		mDriver.setAllowOrientationChange(false);

		mDriver.changeAPI("Production");

		for (int i = 0; i < locales.length; i++) {
			
			//Reset screenshot file name to start with index 1
			mDriver.setScreenshotCount(1);
			
			mDriver.enterLog(TAG, "Starting sweep of " + locales[i].toString());
			Locale testingLocale = locales[i];

			mDriver.mLocaleUtils.setLocale(testingLocale);
			mDriver.delay();
			mDriver.changePOS(locales[i]);
			mDriver.setSpoofBookings();

			mDriver.launchHotels();
			
			//If the app is on a hotel confirmation screen, "NEW SEARCH"
			//will be on that screen. Click it to get back to hotel search
			if (mSolo.searchText(mRes.getString(R.string.NEW_SEARCH))) {
				mSolo.clickOnText(mRes.getString(R.string.NEW_SEARCH));
			}
			// Open calendar and guest picker fragments
			mDriver.delay();
			mDriver.pressCalendar();
			mDriver.pressGuestPicker();
			
			//Enter and select San Francisco as destination
			mDriver.selectLocation("San Francisco");

			//Open sort dialog for hotels search
			mDriver.pressSort();
			
			//Filter fragment for generic string 
			mDriver.filterFor("a");
			
			//Select second hotel in list
			mDriver.selectHotel(2);
			mDriver.delay();
			
			// From hotel info screen
			// Check reviews and come back,
			// Press to book room, and select 
			// first room in list
			mDriver.checkReviews();
			mDriver.pressBookRoom();
			mDriver.selectRoom(0);
			mDriver.delay();
			
			//Go through the log in and booking processes
			mDriver.logInAndBook();
			mDriver.mLocaleUtils.setLocale(testingLocale);
			mDriver.delay(5);
			mSolo.goBack();
			
			//Look at the flights search screen
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