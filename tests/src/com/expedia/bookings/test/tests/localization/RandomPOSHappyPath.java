// POS/Locale Screenshot Sweep
// Made for Expedia Hotels Android App.
// Kevin Carpenter
// Some code derived from Daniel Lew's LocalizationTests.java

package com.expedia.bookings.test.tests.localization;

import java.util.Locale;

import ErrorsAndExceptions.OutOfPOSException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
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
				mDriver.setAllowOrientationChange(false);
				//change device locale and set variable
				Locale testingLocale = mDriver.mLocaleUtils.selectNextLocaleFromInternalList(LOCALE_LIST_LOCATION);
				mDriver.enterLog(TAG, "Starting sweep of " + testingLocale.toString());

				mDriver.ignoreSweepstakesActivity();
				mUser.setHotelCityToRandomUSCity();

				mDriver.changePOS(mDriver.mLocaleUtils.AMERICAN_LOCALES[5]);
				mDriver.changeAPI("Integration");
				mDriver.clearPrivateData();
				mDriver.setSpoofBookings();

				mDriver.launchHotels();
				mDriver.delay();
				mDriver.browseRooms(1, mUser.mHotelSearchCity, true);
				mDriver.mLocaleUtils.setLocale(testingLocale);
				mDriver.delay(5);
			}
		}
		catch (OutOfPOSException e) {
			Log.e(TAG, "POSHappyPath out of POSs. Throwing exception", e);
			throw e;
		}
		catch (RuntimeException r) {
			Log.e(TAG, "RuntimeException", r);
			throw r;
		}
		catch (Exception e) {
			Configuration config = mRes.getConfiguration();
			Log.e(TAG, "Exception on Locale: " + config.locale.toString(), e);
		}
		catch (Error e) {
			Configuration config = mRes.getConfiguration();
			Log.e(TAG, "Error on Locale: " + config.locale.toString(), e);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}

}