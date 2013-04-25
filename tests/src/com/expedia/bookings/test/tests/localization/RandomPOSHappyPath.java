// POS/Locale Screenshot Sweep
// Made for Expedia Hotels Android App.
// Kevin Carpenter
// Some code derived from Daniel Lew's LocalizationTests.java

package com.expedia.bookings.test.tests.localization;

import java.util.Locale;

import ErrorsAndExceptions.OutOfPOSException;
import android.content.res.Resources;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import junit.framework.AssertionFailedError;
import android.util.DisplayMetrics;
import android.util.Log;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class RandomPOSHappyPath extends
		ActivityInstrumentationTestCase2<SearchActivity> {

	public RandomPOSHappyPath() { // Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String TAG = "SearchTest";
	private static final String LOCALE_LIST_LOCATION =
			Environment.getExternalStorageDirectory().getPath() + "/locales_list.txt";

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
		mUser = new HotelsUserData();
		mUser.setHotelCityToRandomUSCity();

		mDriver = new HotelsRobotHelper(mSolo, mRes, mUser);
	}

	////////////////////////////////////////////////////////////////
	// Test Drivers

	public void testBookings() throws Exception {
		try {
			//Limit to two POSs at a time.
			for (int i = 0; i < 2; i++) {
				mDriver.setAllowScreenshots(true);
				//change device locale and set variable
				Locale testingLocale = mDriver.mLocaleUtils.selectNextLocaleFromInternalList(LOCALE_LIST_LOCATION);
				mDriver.enterLog(TAG, "Starting sweep of " + testingLocale.toString());

				mDriver.changeAPI("Production");

				//Reset screenshot file name to start with index 1
				mDriver.setScreenshotCount(1);

				mDriver.changePOS(testingLocale);
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
				mDriver.selectLocation(mUser.mHotelSearchCity);

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
			}
		}
		catch (OutOfPOSException e) {
			Log.e(TAG, "POSHappyPath out of POSs. Throwing exception");
			throw e;
		}
		catch (Exception e) {
			Log.e(TAG, "Caught exception. Rewriting locale list on device.");
			mDriver.mLocaleUtils.appendCurrentLocaleBackOnToList(LOCALE_LIST_LOCATION);
		}
		catch (Error e) {
			Log.e(TAG, "Caught error. Rewriting locale list on device.");
			mDriver.mLocaleUtils.appendCurrentLocaleBackOnToList(LOCALE_LIST_LOCATION);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}

}