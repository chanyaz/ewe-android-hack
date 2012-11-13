package com.expedia.bookings.test.tests.ui;

import java.util.ArrayList;
import java.util.Locale;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.jayway.android.robotium.solo.Solo;

public class SortTest extends ActivityInstrumentationTestCase2<SearchActivity> {

	public SortTest() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}
	
	private static final String TAG = "SortTest";

	private Solo mSolo;

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsRobotHelper mDriver;
	
	TextView mResultCells;

	protected void setUp() throws Exception {
		super.setUp();
		mSolo = new Solo(getInstrumentation(), getActivity());
		//Log.configureLogging("ExpediaBookings", true);

		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
		mDriver = new HotelsRobotHelper(mSolo, mRes);
		
		mDriver.setAllowScreenshots(false);
		mDriver.setAllowOrientationChange(false);
	}

	////////////////////////////////////////////////////////////////
	// Test Driver

	public void testBooking() throws Exception {
		//		mDriver.delay();
		//		mDriver.selectLocation("NYC");
		//
		//		//mDriver.sortPrice();
		//		mDriver.delay(5);
		//
		//		mResultCells = mSolo.getText(3);
		//		mDriver.enterLog(TAG, mResultCells.getTag(0).toString());
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");

		mSolo.finishOpenedActivities();
	}

	
}
