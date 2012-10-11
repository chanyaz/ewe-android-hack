package com.expedia.bookings.test.tests.integration;

import java.lang.reflect.Field;

import android.opengl.GLSurfaceView;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsTestingUtils;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.testutils.RobotiumWorkflowUtils;


public class HotelsHappyPathTest extends ActivityInstrumentationTestCase2<SearchActivity> {

	private static final String TAG = "SearchTest";
	private Field[] mExpediaBookingsRfileFields;
	private Solo solo;
	private boolean testRanThroughCompletion;
	private String environment;
	private Time reservationDate;
	private String city;
	


	public HotelsHappyPathTest() {
		super("com.expedia.bookings", SearchActivity.class);
	}

	protected void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
		mExpediaBookingsRfileFields = R.id.class.getFields();
		testRanThroughCompletion = false;
		Log.d(TAG, "################### BEGIN TEST ######################");
	}

	public void testBooking() throws Exception {
		Log.d(TAG, "testBooking()");

		//environment = "Proxy";
		environment = "Integration";
		reservationDate = new Time("2013-01-15");
		city = "Miami";
		
		//hotel name to assert at each page
		String mHotelName;

		//one-time setup + prep
		Log.d(TAG, "environment configured as: " + environment);
		RobotiumWorkflowUtils.setEnvironment(solo, environment);
		RobotiumWorkflowUtils.clearMenu(solo);

		solo.clickOnText("Hotel");
		
		//city
		HotelsTestingUtils.selectCity(solo, city);

		//calendar
		HotelsTestingUtils.setCalendar(solo, reservationDate);

		HotelsTestingUtils.waitForSearchResultsSpinner(solo);

		//hotel name
		HotelsTestingUtils.selectHotel(solo);
		mHotelName = HotelsTestingUtils.getHotelName(solo);
		
		
		//hotel room
		HotelsTestingUtils.waitForProgressBar(solo);
		HotelsTestingUtils.selectRoom(solo);
		assertEquals(mHotelName, HotelsTestingUtils.getHotelName(solo));

		//book and check out
		HotelsTestingUtils.enterBookingInfo(solo);
		assertEquals(mHotelName, HotelsTestingUtils.getHotelName(solo));
		HotelsTestingUtils.completeBooking(solo, environment);
		assertEquals(mHotelName, HotelsTestingUtils.getHotelName(solo));

		//return to first page 
		HotelsTestingUtils.leaveConfirmationPage(solo, environment);

		//this is the final method of the test; set completion flag
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