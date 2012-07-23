package com.expedia.bookings.test.tests.integration;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.*;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightDetailsActivity;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.FlightSearchResultsActivity;
import com.expedia.bookings.test.utils.CalendarTouchUtils;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.util.Ui;
import com.mobiata.android.widget.CalendarDatePicker;

public class FlightHappyPathTest extends InstrumentationTestCase {

	private Activity mCurrentActivity;
	private Solo mSolo;
	private Instrumentation mInstr;

	@Override
	public void setUp() {
		mInstr = getInstrumentation();
	}

	@Override
	protected void tearDown() {

	}

	/**
	 * This is a relatively simple end-to-end test that goes through the basic happy path of booking a flight. Will be
	 * extensible in the future
	 *
	 * NOTES: In order for this test to properly pass, here are a few requirements:
	 * 1. Must be logged in to VPN. In order to talk to Expedia's Flights API atm, the device running tests must be
	 * on the Expedia network through VPN or from within Expedia's network
	 * 2. A device must be present to run the tests. This should be relatively easy to figure out when running tests
	 * locally, however, once these tests are running on Jenkins, more care will be exercised to ensure a device is 
	 * ready to run tests.
	 */

	@LargeTest
	public void testFlightSearch() throws Throwable {
		// create Intent to launch FlightSearchActivity
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(mInstr.getTargetContext(), FlightSearchActivity.class.getName());

		// Register we are interested in the FlightSearchActivity
		Instrumentation.ActivityMonitor monitor = mInstr.addMonitor(FlightSearchActivity.class.getName(),
				null, false);

		// launch activity 
		mInstr.startActivitySync(intent);

		// assert the activity gets launched
		mCurrentActivity = mInstr.waitForMonitorWithTimeout(monitor, 5);
		assertNotNull(mCurrentActivity);
		assertEquals(FlightSearchActivity.class, mCurrentActivity.getClass());

		// instantiate the Robotium Solo class for manual touch interaction
		mSolo = new Solo(mInstr, mCurrentActivity);

		// Enter airport fields for the FlightSearchParams
		mSolo.enterText((EditText) mCurrentActivity.findViewById(R.id.departure_airport_edit_text), "DTW");
		mSolo.enterText((EditText) mCurrentActivity.findViewById(R.id.arrival_airport_edit_text), "SFO");

		// click the dates button to show the Calendar fragment in the content pane
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				Ui.findView(mCurrentActivity, R.id.dates_button).performClick();
			}
		});

		// make sure all of the calendar drawing, processing gets consumed/handled
		mInstr.waitForIdleSync();

		// Enter date for the FlightSearchParams
		CalendarDatePicker cal = (CalendarDatePicker) mCurrentActivity.findViewById(R.id.dates_date_picker);
		Time day = CalendarTouchUtils.getDay(3);
		CalendarTouchUtils.clickOnDay(mSolo, cal, day);

		// create monitor for FlightSearchResultsActivity
		mInstr.removeMonitor(monitor);
		monitor = mInstr.addMonitor(FlightSearchResultsActivity.class.getName(), null, false);

		// perform Search!
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				Ui.findView(mCurrentActivity, R.id.search).performClick();
			}
		});

		// wait for SearchResults with a 40 second timeout. change timeout?
		mCurrentActivity = mInstr.waitForMonitorWithTimeout(monitor, 40000);

		mInstr.waitForIdleSync();

		// assert that a list of results is present
		assertEquals(FlightSearchResultsActivity.class, mCurrentActivity.getClass());

		ListView lv = (ListView) mCurrentActivity.findViewById(android.R.id.list);
		assertNotNull(lv);

		// click on the first element in list (that is not a header)
		RelativeLayout row = (RelativeLayout) lv.getChildAt(0 + lv.getHeaderViewsCount());

		int[] location = new int[2];
		row.getLocationInWindow(location);
		mSolo.clickOnScreen((float) location[0], (float) location[1]);

		mInstr.waitForIdleSync();
		
		/*
		 * THIS PART OF THE TESTS NEEDS TO BE REWRITTEN DUE TO DESIGN CHANGES

		row = (RelativeLayout) lv.getChildAt(1);
		final Button detailsButton = (Button) row.findViewById(R.id.details_button);

		assertNotNull(detailsButton);

		// monitor for FlightDetailsActivity
		mInstr.removeMonitor(monitor);
		monitor = mInstr.addMonitor(FlightDetailsActivity.class.getName(), null, false);

		// click details button to launch details activity
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				detailsButton.performClick();
			}
		});
		mCurrentActivity = mInstr.waitForMonitorWithTimeout(monitor, 5000);

		assertEquals(FlightDetailsActivity.class, mCurrentActivity.getClass());
		*/
	}

}