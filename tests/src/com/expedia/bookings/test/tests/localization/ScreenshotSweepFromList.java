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

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.hotels.HotelsHappyPath;
import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

public class ScreenshotSweepFromList extends
		ActivityInstrumentationTestCase2<SearchActivity> {

	public ScreenshotSweepFromList() { // Default constructor
		super(SearchActivity.class);
	}

	private static final String TAG = "POS Test";
	private static final String LOCALE_LIST_LOCATION =
			Environment.getExternalStorageDirectory().getPath() + "/locales_list.txt";

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsTestDriver mDriver;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;

	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(true);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData();
		mUser.setHotelCityToRandomUSCity();
		mUser.setBookingServer("Production");
	}

	////////////////////////////////////////////////////////////////
	// Test Drivers

	public void testBookings() throws Exception {
		try {
			//Limit to two POSs at a time.
			for (int i = 0; i < 2; i++) {
				Locale testingLocale = mDriver.mLocaleUtils.selectNextLocaleFromInternalList(LOCALE_LIST_LOCATION);
				mDriver.enterLog(TAG, "Starting sweep of " + testingLocale.toString());
				mDriver.mLocaleUtils.setLocale(testingLocale);
				HotelsHappyPath.execute(mDriver, mUser, 1);
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
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}