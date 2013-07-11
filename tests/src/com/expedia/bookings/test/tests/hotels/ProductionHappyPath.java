package com.expedia.bookings.test.tests.hotels;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.jayway.android.robotium.solo.Solo;

public class ProductionHappyPath extends ActivityInstrumentationTestCase2<SearchActivity> {

	public ProductionHappyPath() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String TAG = "SearchTest";

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
		mDriver = new HotelsRobotHelper(mSolo, mRes);
		mUser = new HotelsUserData();
	}

	// This test goes through a prototypical hotel booking
	// UI flow, up to finally checking out.
	// It runs pulling from the Production API
	
	public void testMethod() throws Exception {
		mUser.setHotelCityToRandomUSCity();
		mDriver.setAllowScreenshots(false);
		mDriver.setAllowOrientationChange(false);
		
		mDriver.changePOS(mDriver.mLocaleUtils.AMERICAN_LOCALES[5]);
		mDriver.changeAPI("Production");
		mDriver.clearPrivateData();
		mDriver.setSpoofBookings();
		
		mDriver.launchHotels();
		mDriver.delay();
		mDriver.browseRooms(4, mUser.mHotelSearchCity, false);

	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}

}