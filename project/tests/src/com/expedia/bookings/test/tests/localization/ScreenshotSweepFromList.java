package com.expedia.bookings.test.tests.localization;

import java.util.Locale;

import android.content.res.Resources;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.hotels.HotelsHappyPath;
import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.ScreenshotMethodInterface;
import com.expedia.bookings.test.utils.ScreenshotSweepRunnerUtils;
import com.expedia.bookings.test.utils.TestPreferences;

public class ScreenshotSweepFromList extends
		ActivityInstrumentationTestCase2<SearchActivity> {

	public ScreenshotSweepFromList() { // Default constructor
		super(SearchActivity.class);
	}

	private static final String TAG = ScreenshotSweepFromList.class.getSimpleName();
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
		mUser = new HotelsUserData(getActivity());
		mUser.setHotelCityToRandomUSCity();
		mUser.setBookingServer("Production");
	}

	private class Runner implements ScreenshotMethodInterface {
		@Override
		public void execute() throws Exception {
			//Limit to two POSs at a time.
			for (int i = 0; i < 4; i++) {
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

				HotelsHappyPath.execute(mDriver, mUser, 1);
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