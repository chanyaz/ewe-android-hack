package com.expedia.bookings.test.tests.hotels;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class RotateHappyPath extends ActivityInstrumentationTestCase2<SearchActivity> {
	public RotateHappyPath() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private Solo mSolo;
	private static final String TAG = "Hotels Rotate Happy Path";
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
		mDriver = new HotelsRobotHelper(mSolo, mRes);
		mDriver.setScreenshotCount(1);
		mUser = new HotelsUserData();
		mUser.setHotelCityToRandomUSCity();
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
		try {
			mDriver.delay();
			mDriver.setAllowOrientationChange(true); //sets Driver to do the rotates
			mDriver.setAllowScreenshots(false); //no screenshots.
			mSolo.clickOnScreen(50, 50);
			mDriver.setSpoofBookings();
			mDriver.clearPrivateData();
			mDriver.delay(2);

			mDriver.launchHotels();

			mDriver.pressCalendar();
			mDriver.pressGuestPicker();

			mDriver.selectLocation(mUser.mHotelSearchCity);

			mDriver.pressSort();
			mDriver.filterFor("a");
			mDriver.selectHotel(2);
			mDriver.delay(8);
			mDriver.checkReviews();
			mDriver.pressBookRoom();
			mDriver.selectRoom(0);

			mDriver.delay();

			mDriver.bookingScreenShots();
			mDriver.logInAndBook(true, true);
			mDriver.captureInfoScreen();
		}
		catch (Error e) {
			mDriver.takeScreenshotUponFailure(e, TAG);
			throw e;
		}
		catch (Exception e) {
			mDriver.takeScreenshotUponFailure(e, TAG);
			throw e;
		}
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}
}
