package com.expedia.bookings.test.tests.flights;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.testutils.CalendarTouchUtils;
import com.mobiata.android.widget.CalendarDatePicker;

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
		mDriver.setAllowOrientationChange(true);
		
	}
                                                      
	public void testMethod() throws Exception {
		

		mDriver.changePOS(mDriver.AMERICAN_LOCALES[5]);
		mDriver.clearPrivateData();
		mDriver.launchFlights();
		
		mSolo.enterText(0, "SFO");
		mDriver.landscape();
		mDriver.delay();
		mDriver.portrait();
		mDriver.delay();
		
		mSolo.enterText(1, "LAX");
		mDriver.landscape();
		mDriver.delay();
		mDriver.portrait();
		mDriver.delay();
		
		mSolo.clickOnText(mRes.getString(R.string.hint_select_departure));
		mDriver.landscape();
		mDriver.delay();
		mDriver.portrait();
		mDriver.delay();
		
		Time start = CalendarTouchUtils.getDay(2);
		Time end = CalendarTouchUtils.getDay(5);
		CalendarDatePicker mCal = new CalendarDatePicker(mSolo.getCurrentActivity().getApplicationContext());
		CalendarTouchUtils.clickOnDay(mSolo, mCal, start); //can't get this to work
		CalendarTouchUtils.clickOnDay(mSolo, mCal, end);

		mSolo.goBack();
	}
	
	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		mDriver.enterLog(TAG, "tearing down...");
		mSolo.finishOpenedActivities();
	}
}


