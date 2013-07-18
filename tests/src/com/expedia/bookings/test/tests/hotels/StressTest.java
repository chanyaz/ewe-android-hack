package com.expedia.bookings.test.tests.hotels;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.jayway.android.robotium.solo.Solo;

public class StressTest extends ActivityInstrumentationTestCase2<SearchActivity> {

	public StressTest() {
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String TAG = "Hotels Stress Test";

	private Solo mSolo;

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsRobotHelper mDriver;

	public static int NUMBER_OF_HOTELS = 48;

	protected void setUp() throws Exception {
		super.setUp();
		mSolo = new Solo(getInstrumentation(), getActivity());
		//Log.configureLogging("ExpediaBookings", true);

		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
		mDriver = new HotelsRobotHelper(mSolo, mRes);
	}

	public void testMethod() throws Exception {
		try {
			mDriver.setAllowScreenshots(false);
			mDriver.setAllowOrientationChange(false);
			mDriver.delay();
			mDriver.changePOS(mDriver.mLocaleUtils.AMERICAN_LOCALES[5]);
			mDriver.setSpoofBookings();
			mDriver.launchHotels();
			mDriver.delay();
			mDriver.browseRooms(NUMBER_OF_HOTELS, "New York City", false);
		}
		catch (Exception e) {
			mDriver.takeScreenshotUponFailure(e, TAG);
			throw e;
		}
		catch (Error e) {
			mDriver.takeScreenshotUponFailure(e, TAG);
			throw e;
		}

	}

	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}

}
