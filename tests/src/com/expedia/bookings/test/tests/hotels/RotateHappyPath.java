package com.expedia.bookings.test.tests.hotels;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.jayway.android.robotium.solo.Solo;

public class RotateHappyPath extends ActivityInstrumentationTestCase2<SearchActivity> {
	public RotateHappyPath() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private Solo mSolo;
	private static final String TAG = "RotateHappyPath";
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
		mDriver.setScreenshotCount(1);
	}

	void rotateFilter() {
		mSolo.clickOnText(mRes.getString(R.string.filter));
		mDriver.landscape();
		mDriver.portrait();
		mSolo.goBack();
	}

	////////////////////////////////////////////////////////////////
	// Test Driver 

	public void testMethod() throws Exception {
		mDriver.delay();
		mDriver.setAllowOrientationChange(true); //sets Driver to do the rotates
		mDriver.setAllowScreenshots(false); //no screenshots.
		mSolo.clickOnScreen(50, 50);
		mDriver.clearPrivateData();
		mDriver.delay(2);

		mDriver.launchHotels();

		mDriver.pressCalendar();
		mDriver.pressGuestPicker();

		mDriver.selectLocation("New York City");

		mDriver.pressSort();
		mDriver.filterFor("a");
		mDriver.selectHotel(2);
		mDriver.delay(8);
		mDriver.checkReviews();
		mDriver.pressBookRoom();
		mDriver.selectRoom(0);

		mDriver.delay();

		mDriver.bookingScreenShots();
		mDriver.logInAndBook();
		mDriver.captureInfoScreen();
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}
}
