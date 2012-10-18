package com.expedia.bookings.test.tests.flights;

import java.lang.reflect.Field;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.FlightsInputUtils;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.testutils.RobotiumWorkflowUtils;


public class FlightsUITest extends ActivityInstrumentationTestCase2<SearchActivity> {

	private static final String TAG = "FlightsTest";
	private Field[] mExpediaBookingsRfileFields;
	private Solo solo;
	private boolean testRanThroughCompletion;
	private String environment;
	private Instrumentation instr;
	
	public FlightsUITest() {
		super("com.expedia.bookings", SearchActivity.class);
	}	


	private void flip() {
		solo.setActivityOrientation(Solo.LANDSCAPE);
		solo.sleep(1000);
		solo.setActivityOrientation(Solo.PORTRAIT);
		solo.sleep(1000);
	}
	
	@Override
	protected void setUp() throws Exception {
		instr = getInstrumentation();
		solo = new Solo(instr, getActivity());
		mExpediaBookingsRfileFields = R.id.class.getFields();
		testRanThroughCompletion = false;
		Log.d(TAG, "################### BEGIN TEST ######################");
	}

	public void testHappyPath() {
		solo.clickOnView(solo.getView(R.id.flights_button));
		FlightsInputUtils.performFlightSearch(instr, solo, "SFO", "NYC", 10, R.id.search);
		RobotiumWorkflowUtils.waitForListViewToPopulate(solo, mExpediaBookingsRfileFields);
		flip();
		solo.clickInList(2);
		flip();
		solo.clickOnView(solo.getView(R.id.select_button));
		flip();
		solo.clickInList(2);
		flip();
		solo.clickOnView(solo.getView(R.id.select_button));
		flip();
		solo.clickOnView(solo.getView(R.id.menu_checkout));
		flip();
		solo.clickOnView(solo.getView(R.id.traveler_info_btn));
		
		testRanThroughCompletion = true;
	}
	
	
	@Override
	protected void tearDown() throws Exception {
		Log.d(TAG, "in tearDown()");
		Log.d(TAG, "testRanThrougCompletion: " + testRanThroughCompletion);
		assertTrue(testRanThroughCompletion);
		Log.d(TAG, "sleeping");
		solo.sleep(5000);
		//Robotium will finish all the activities that have been opened
		solo.finishOpenedActivities();
	}	
	
}
