package com.expedia.bookings.test.tests.localization;

import java.util.Locale;

import ErrorsAndExceptions.OutOfPOSException;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Environment;
import android.test.mock.MockContext;
import android.util.Log;

import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.tests.flights.FlightsHappyPath;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.test.utils.FlightsTestDriver;

public class FlightsScreenshotSweepFromList extends CustomActivityInstrumentationTestCase<LaunchActivity> {

	private FlightsTestDriver mDriver;
	private static final String TAG = "FlightsScreenshotSweepFromList";
	private static final String LOCALE_LIST_LOCATION =
			Environment.getExternalStorageDirectory().getPath() + "/locales_list.txt";

	public FlightsScreenshotSweepFromList() {
		super(LaunchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mPreferences.setScreenshotPermission(true);
		mPreferences.setRotationPermission(false);
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser.setHotelCityToRandomUSCity();
	}

	public void testBookings() throws Exception {
		try {
			//Limit to eight POSs at a time.
			for (int i = 0; i < 8; i++) {
				mDriver.setScreenshotCount(0);
				Locale testingLocale = mDriver.mLocaleUtils.selectNextLocaleFromInternalList(LOCALE_LIST_LOCATION);
				mDriver.enterLog(TAG, "Starting sweep of " + testingLocale.toString());
				mDriver.delay();

				mDriver.launchScreen().openMenuDropDown();
				if (mDriver.searchText(mDriver.launchScreen().settingsString())) {
					mDriver.launchScreen().pressSettings();
				}
				else {
					mDriver.clickInList(0);
				}
				mDriver.settingsScreen().clickCountryString();
				mDriver.settingsScreen().selectPOSFromLocale(testingLocale);
				mDriver.delay(1);
				mDriver.goBack();

				PointOfSale currentPOS = PointOfSale.getPointOfSale();
				if (currentPOS.supportsFlights()) {
					FlightsHappyPath.execute(mDriver, mUser);
				}
				else {
					mDriver.enterLog(TAG,
							"Skipping because flights not supported on locale: " + testingLocale.toString());
				}
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
