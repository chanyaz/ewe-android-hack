package com.expedia.bookings.test.tests.ui;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.jayway.android.robotium.solo.Solo;

public class BrowsingTest extends ActivityInstrumentationTestCase2<SearchActivity> {
	
	BrowsingTest(){ //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}
	
	private static final String TAG = "SearchTest";

	private Solo mSolo;

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
	}
	
	public void testMethod() throws Exception{
		
		mDriver.setAllowScreenshots(false);
		mDriver.setAllowOrientationChange(false);
		
		mDriver.changeAPI("Integration");
		mDriver.selectLocation("Las Vegas");
		
		
		for(int i = 0; i < 2; i++){
			for(int j = 0; j < 4; j++){
				mDriver.selectHotel(j);
				mDriver.delay(3);
				mSolo.goBack();
				mDriver.delay(3);
			}
			mSolo.scrollDown();
		}
		mDriver.selectHotel(15);
		mDriver.pressBookRoom();
		mDriver.selectRoom(0);
		mDriver.logInAndBook();
		
	
	}
	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}
	
}
