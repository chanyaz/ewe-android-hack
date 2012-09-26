package com.expedia.bookings.test.tests.mock;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsTestingUtils;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.testutils.RobotiumWorkflowUtils;

public class HotelsSearchWithProxy extends ActivityInstrumentationTestCase2<SearchActivity> {

	private static final String TAG = "HotelsSearchWithProxy";
	private static final String ENVIRONMENT = "Proxy";
	private static final Time RESERVATION_DATE = new Time("2013-01-15");

	private Solo solo;

	private String city;
	private String expectedHotelName;
	private String actualHotelName;

	public HotelsSearchWithProxy() {
		super("com.expedia.bookings", SearchActivity.class);
	}

	protected void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
		Log.d(TAG, "################### BEGIN TEST ######################");
		Log.d(TAG, "osVersion: " + android.os.Build.VERSION.RELEASE);
		expectedHotelName = null;
		actualHotelName = null;
	}


	public void testSearchWithProxy() throws Exception {

		city = "Miami (and vicinity), Florida";
		expectedHotelName = "Rodeway Inn Miami Airport";
				
		RobotiumWorkflowUtils.setEnvironment(solo, ENVIRONMENT);
		HotelsTestingUtils.selectCity(solo, city);
		HotelsTestingUtils.setCalendar(solo, RESERVATION_DATE);
		actualHotelName = HotelsTestingUtils.getHotelName(solo);
		
		assertEquals(expectedHotelName, actualHotelName);
		
	}
	
	
	@Override
	protected void tearDown() throws Exception {
		Log.d(TAG, "in tearDown()");
		Log.d(TAG, "sleeping");
		solo.sleep(10000);
		solo.finishOpenedActivities();
	}

}	
	

