package com.expedia.bookings.test.tests.localization;

import java.util.Locale;

import android.os.Environment;

import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.tests.flights.FlightsHappyPath;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.test.utils.FlightsTestDriver;
import com.expedia.bookings.test.utils.ScreenshotMethodInterface;
import com.expedia.bookings.test.utils.ScreenshotSweepRunnerUtils;

public class FlightsScreenshotSweepFromList extends CustomActivityInstrumentationTestCase<LaunchActivity> {

	private FlightsTestDriver mDriver;
	private static final String TAG = FlightsScreenshotSweepFromList.class.getSimpleName();
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

	private class Runner implements ScreenshotMethodInterface {
		@Override
		public void execute() throws Exception {
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
	}

	public void testBookings() throws Exception {
		Runner runner = new Runner();
		ScreenshotSweepRunnerUtils.run(runner, mRes);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
