package com.expedia.bookings.test.tests.integration;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.widget.*;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.*;
import com.expedia.bookings.fragment.FlightListFragment;
import com.expedia.bookings.fragment.StatusFragment;
import com.mobiata.testutils.CalendarTouchUtils;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.util.Ui;
import com.mobiata.android.widget.CalendarDatePicker;

public class FlightHappyPathTest extends InstrumentationTestCase {

	private Activity mCurrentActivity;
	private Solo mSolo;

	private Instrumentation mInstr;
	private Instrumentation.ActivityMonitor mMonitor;

	@Override
	public void setUp() {
		mInstr = getInstrumentation();
	}

	@Override
	protected void tearDown() {
		mSolo.finishOpenedActivities();
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
	public void testFlightSearchOneWay() throws Throwable {
		// create Intent to launch FlightSearchActivity (default Flights launcher)
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(mInstr.getTargetContext(), FlightSearchActivity.class.getName());

		// Register we are interested monitoring in the FlightSearchActivity
		registerMonitor(FlightSearchActivity.class.getName());

		// launch activity via instrumentation
		mInstr.startActivitySync(intent);

		// assert the activity gets launched
		mCurrentActivity = mInstr.waitForMonitorWithTimeout(mMonitor, 5);
		assertNotNull(mCurrentActivity);
		assertEquals(FlightSearchActivity.class, mCurrentActivity.getClass());

		// instantiate the Robotium Solo class for manual touch interaction
		mSolo = new Solo(mInstr, mCurrentActivity);

		// insert two airport codes (perhaps this data should be part of a larger mocking framework?)
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				Ui.setText(mCurrentActivity, R.id.departure_airport_edit_text, "DTW");
				Ui.setText(mCurrentActivity, R.id.arrival_airport_edit_text, "SFO");
			}

		});

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
		CalendarDatePicker cal = (CalendarDatePicker) mCurrentActivity.findViewById(R.id.calendar_date_picker);
		assertNotNull(cal);
		Time day = CalendarTouchUtils.getDay(4);
		assertNotNull(day);
		assertNotNull(mSolo);
		CalendarTouchUtils.clickOnDay(mSolo, cal, day);

		// create monitor for FlightSearchResultsActivity
		registerMonitor(FlightSearchResultsActivity.class.getName());

		// perform Search!
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				Ui.findView(mCurrentActivity, R.id.search).performClick();
			}
		});

		// wait for SearchResults with a 40 second timeout. change timeout?
		mCurrentActivity = mInstr.waitForMonitorWithTimeout(mMonitor, 40000);

		mInstr.waitForIdleSync();

		// assert that a list of results is present
		assertEquals(FlightSearchResultsActivity.class, mCurrentActivity.getClass());

		// The expected behavior for FlightSearchResultsActivity is to progress through fragments. monitor for fragments
		mSolo.waitForFragmentByTag(StatusFragment.TAG);
		mSolo.waitForFragmentByTag(FlightListFragment.TAG);
		mInstr.waitForIdleSync();

		// find the list of flights and assert that it exists
		ListView lv = (ListView) mCurrentActivity.findViewById(android.R.id.list);
		assertNotNull(lv);

		// add monitor for OverviewActivity in anticipation of click in list
		registerMonitor(FlightTripOverviewActivity.class.getName());

		// click on the first Flight in the list (first child of ListView that is not a header, 0th index) 
		mSolo.clickInList(lv.getHeaderViewsCount() + 1);
		mCurrentActivity = mInstr.waitForMonitorWithTimeout(mMonitor, 10000);
		mInstr.waitForIdleSync();
		assertEquals(FlightTripOverviewActivity.class, mCurrentActivity.getClass());

		// wait 5 seconds to ensure that the price update call goes through
		mSolo.sleep(5000);

		View priceUpdateDialog = mSolo.getView(android.R.id.button3);
		if (priceUpdateDialog != null) {
			mSolo.clickOnView(mSolo.getView(android.R.id.button3));
		}

		// click on the checkout button to launch the FlightCheckoutActivity
		registerMonitor(FlightCheckoutActivity.class.getName());
		mSolo.clickOnView(mSolo.getView(R.id.checkout_btn));
		mCurrentActivity = mInstr.waitForMonitorWithTimeout(mMonitor, 10000);
		mInstr.waitForIdleSync();
		assertEquals(FlightCheckoutActivity.class, mCurrentActivity.getClass());

		//

		mSolo.sleep(10000);
	}

	// helper method to make things a little bit easier
	private void registerMonitor(String className) {
		if (mMonitor != null) {
			mInstr.removeMonitor(mMonitor);
		}

		mMonitor = mInstr.addMonitor(className, null, false);
	}

	@LargeTest
	public void testFlightSearchRoundtrip() throws Throwable {

	}
}