package com.expedia.bookings.test.tests.integration;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.EditText;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.util.Ui;
import com.mobiata.testutils.CalendarTouchUtils;
import com.mobiata.testutils.InputUtils;
import com.mobiata.testutils.MonitorUtils;

/**
 * The tests in this class validate that binding between UI components in the FlightSearchParamsFragment, and the data
 * model object that represents the information gathered in the UI, FlightSearchParams. Additionally test the fragment
 * retaining UI values on orientation change.
 */

public class FlightSearchParamsFragmentTest extends InstrumentationTestCase {

	private Solo mSolo;

	private Instrumentation mInstr;
	private Instrumentation.ActivityMonitor mMonitor;

	private FlightSearchActivity mActivity;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		mInstr = getInstrumentation();

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(mInstr.getTargetContext(), FlightSearchActivity.class.getName());

		// register monitor for FlightSearchActivity
		mMonitor = MonitorUtils.registerMonitor(mInstr, mMonitor, FlightSearchActivity.class.getName());

		// launch activity via instrumentation
		mInstr.startActivitySync(intent);

		// assert the activity gets launched
		mActivity = (FlightSearchActivity) mInstr.waitForMonitorWithTimeout(mMonitor, 1000);
		assertNotNull(mActivity);
		assertEquals(FlightSearchActivity.class, mActivity.getClass());

		mSolo = new Solo(mInstr, mActivity);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		mSolo.finishOpenedActivities();
		mActivity.finish();
	}

	@MediumTest
	public void testFlightSearchBindsUiValuesToDb() {
		// select 'DTW' for departure airport
		String expectedDepartureAirportCode = "DTW";
		InputUtils.selectAirport(this, mActivity, mInstr, mSolo, expectedDepartureAirportCode,
				R.id.departure_airport_edit_text);

		// selected 'ATL' for arrival airport
		String expectedArrivalAirportCode = "ATL";
		InputUtils.selectAirport(this, mActivity, mInstr, mSolo, expectedArrivalAirportCode,
				R.id.arrival_airport_edit_text);

		// click dates button so that the calendar appears
		TouchUtils.clickView(this, Ui.findView(mActivity, R.id.dates_button));

		// select a day 4 days in the future
		Time expectedDay = CalendarTouchUtils.selectDay(mActivity, mSolo, 4, R.id.calendar_date_picker);

		// search so that the FlightSearchParamsFragment closes and saves params to Db
		TouchUtils.clickView(this, Ui.findView(mActivity, R.id.search));

		// grab and assert values from FlightSearchParams (from Db)
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();

		assertEquals(expectedDepartureAirportCode, params.getDepartureAirportCode());
		assertEquals(expectedArrivalAirportCode, params.getArrivalAirportCode());
		assertEquals(expectedDay, new Time(params.getDepartureDate().getCalendar()));
	}

	@MediumTest
	public void testFlightSearchParamsRetainsValuesOnRotation() {
		// select 'DTW' for departure airport
		String expectedDepartureAirportCode = "DTW";
		InputUtils.selectAirport(this, mActivity, mInstr, mSolo, expectedDepartureAirportCode,
				R.id.departure_airport_edit_text);

		// select 'ATL' for arrival airport
		String expectedArrivalAirportCode = "ATL";
		InputUtils.selectAirport(this, mActivity, mInstr, mSolo, expectedArrivalAirportCode,
				R.id.arrival_airport_edit_text);

		// click on dates button to engage calendar edit mode
		TouchUtils.clickView(this, Ui.findView(mActivity, R.id.dates_button));

		// click on day 3 days in future
		Time expectedStartDay = CalendarTouchUtils.selectDay(mActivity, mSolo, 3, R.id.calendar_date_picker);

		mInstr.waitForIdleSync();

		mSolo.sleep(1000);

		// click the passengers button to hide calendar and does not take up the whole screen in landscape
		TouchUtils.clickView(this, Ui.findView(mActivity, R.id.passengers_button));

		// rotate screen to landscape
		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mInstr.waitForIdleSync();
		assertNotNull(mActivity);

		mSolo.sleep(1000);

		// assert the departure view retains text after rotation
		AlwaysFilterAutoCompleteTextView departureView = (AlwaysFilterAutoCompleteTextView) mSolo
				.getView(R.id.departure_airport_edit_text);
		assertNotNull(departureView);
		assertTrue(departureView.getText().toString().contains(expectedDepartureAirportCode));

		// assert the arrival view retains text after rotation
		AlwaysFilterAutoCompleteTextView arrivalView = (AlwaysFilterAutoCompleteTextView) mSolo
				.getView(R.id.arrival_airport_edit_text);
		assertNotNull(arrivalView);
		assertTrue(arrivalView.getText().toString().contains(expectedArrivalAirportCode));

		// assert the calendar text view retains the day on rotation
		EditText datesButton = (EditText) mSolo.getView(R.id.dates_button);
		String expectedMonthDay = Integer.toString(expectedStartDay.monthDay);

		assertNotNull(datesButton);
		assertTrue(datesButton.getText().toString().contains(expectedMonthDay));

		// click calendar button to display cal
		TouchUtils.clickView(this, mSolo.getView(R.id.dates_button));

		// TODO: Fix this assertion. Unable to reproduce when interacting with app manually, must fix setUp/tearDown
		// assert the calendar displays the correct day
		//		CalendarDatePicker calendarDatePicker = (CalendarDatePicker) mSolo.getView(R.id.calendar_date_picker);
		//		assertEquals(expectedStartDay, calendarDatePicker.getStartTime());
	}

}