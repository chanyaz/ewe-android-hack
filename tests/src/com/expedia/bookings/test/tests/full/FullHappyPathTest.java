package com.expedia.bookings.test.tests.full;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class FullHappyPathTest extends ActivityInstrumentationTestCase2<SearchActivity> {

	public FullHappyPathTest() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private Solo mSolo;
	private static final String TAG = "Full Happy Path Test";
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
		mDriver.setAllowOrientationChange(false);

		mUser = new HotelsUserData();
		mUser.setHotelCityToRandomUSCity();
		mUser.setAirportsToRandomUSAirports();
	}

	////////////////////////////////////////////////////////////////
	// Test Driver 

	public void testMethod() throws Exception {
		try {
			mDriver.setSpoofBookings();
			mDriver.clearPrivateData();

			mDriver.launchHotels();
			mDriver.browseRooms(4, mUser.mHotelSearchCity, true);

			mDriver.flightsHappyPath(mUser.mDepartureAirport, mUser.mArrivalAirport, 1, true, false);
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
