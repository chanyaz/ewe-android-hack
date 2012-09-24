package com.expedia.bookings.test.tests.ui;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

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

	////////////////////////////////////////////////////////////////
	// Rotate Methods

	public void rotateInfo() {
		//Rotate info screen
		mDriver.enterLog(TAG, "RotateInfo");
		mSolo.pressMenuItem(1);
		mDriver.landscape();
		mDriver.delay(1);
		mDriver.portrait();
		mSolo.goBack();
	}

	public void rotateSort() {
		//Rotate sort fragment
		//currently there is a bug where if you rotate to landscape-portrait-landscape
		//the sort fragment disappears. As a result, no goBack() needed
		mDriver.enterLog(TAG, "RotateSort");
		mSolo.clickOnText("SORT");
		mDriver.delay(1);
		mDriver.landscape();
		mDriver.portrait();
		mDriver.delay(1);
	}

	public void rotateFilter() {
		//Rotate filter fragment
		mDriver.enterLog(TAG, "RotateFilter");
		mDriver.delay();
		mSolo.clickOnText("FILTER");
		mDriver.delay(1);
		mDriver.landscape();
		mDriver.delay();
		mDriver.portrait();
		mDriver.delay();
		mSolo.goBack();
		mDriver.delay();
		}

	public void rotateHotelDetails(int index) {
		// Select a hotel, rotate hotel details
		mDriver.enterLog(TAG, "rotateHotelDetails");
		mDriver.delay();
		mSolo.waitForActivity("ExpediaBookingApp.java");
		mSolo.clickInList(index);
		mDriver.delay(3);
		mDriver.landscape();
		mDriver.delay(2);
		mDriver.portrait();
		//driver.delay(5);
	}

	public void rotateRoomsAndRates() {
		// Rotate Rooms & Rates screen
		mDriver.enterLog(TAG, "RotateRoomsAndRates");
		mDriver.delay();
		mDriver.pressBookRoom();
		mSolo.waitForActivity("RoomsAndRatesListActivity");
		mDriver.delay(1);
		mDriver.landscape();
		mDriver.delay();
		mDriver.portrait();
	}

	public void rotateBookingInfo() {
		// Rotate Booking Info
		mDriver.enterLog(TAG, "RotateBookingInfo");
		mSolo.clickInList(0);
		mSolo.waitForActivity("BookingInfoActivity");
		mDriver.delay(3);
		mDriver.landscape();
		mDriver.delay();
		mDriver.portrait();
		mDriver.delay();
	}

	public void rotateLogInAndBooking() {
		// Log in and get to confirmation screen
		mDriver.enterLog(TAG, "rotateLogInAndBooking");
		try {
			mDriver.logIn(mRes);
			mDriver.enterCCV();
			mDriver.landscape();
			mDriver.delay();
			mDriver.portrait();
			mDriver.confirmAndBook();
		}
		catch (Exception e) {

		}
	}

	////////////////////////////////////////////////////////////////
	// Test Driver 

	public void testMethod() throws Exception {
		mDriver.setAllowOrientationChange(true);
		mDriver.setAllowScreenshots(false);

		mDriver.clearPrivateData();
		//driver.closeBanner();

		mSolo.sleep(2000);
		rotateInfo();
		mDriver.selectLocation("San Francisco");
		rotateFilter(); 
		mDriver.delay();
		rotateSort();
		
		rotateHotelDetails(0);

		rotateRoomsAndRates();

		rotateBookingInfo();

		rotateLogInAndBooking();

	}
	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}
}
