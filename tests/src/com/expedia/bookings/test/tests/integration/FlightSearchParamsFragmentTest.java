package com.expedia.bookings.test.tests.integration;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.test.FlakyTest;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.EditText;
import android.widget.TextView;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.FlightSearchResultsActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.fragment.FlightListFragment;
import com.expedia.bookings.fragment.StatusFragment;
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.util.Ui;
import com.mobiata.testutils.*;

/**
 * The tests in this class validate that binding between UI components in the FlightSearchParamsFragment, and the data
 * model object that represents the information gathered in the UI, FlightSearchParams. Additionally test the fragment
 * retaining UI values on orientation change.
 */

public class FlightSearchParamsFragmentTest extends InstrumentationTestCase {

	private static final String ISO_FORMAT = "%Y-%m-%d";

	private Solo mSolo;

	private Instrumentation mInstr;
	private Instrumentation.ActivityMonitor mMonitor;

	private Activity mActivity;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		mInstr = getInstrumentation();

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(mInstr.getTargetContext(), FlightSearchActivity.class.getName());

		// register monitor for FlightSearchActivity
		mMonitor = MonitorUtils.registerMonitor(mInstr, mMonitor, FlightSearchActivity.class.getName());

		// launch activity via instrumentation
		mInstr.startActivitySync(intent);

		// assert the activity gets launched
		mActivity = mInstr.waitForMonitorWithTimeout(mMonitor, 1000);
		assertNotNull(mActivity);
		assertEquals(FlightSearchActivity.class, mActivity.getClass());

		mSolo = new Solo(mInstr, mActivity);
		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		mSolo.finishOpenedActivities();
		mActivity.finish();
	}

	@MediumTest
	public void testFlightSearchBindsUiValuesToDb() {
		performFlightSearchAndAssertDb("DTW", "ATL", 5, R.id.search, true);
	}

	@MediumTest
	public void testFlightSearchBindsUiValuesToDbAndChangingValuesLaunchesNewCorrectSearch() {
		performFlightSearchAndAssertDb("DTW", "ATL", 4, R.id.search, true);

		mSolo.goBack();

		performFlightSearchAndAssertDb("SFO", "SEA", 3, R.id.search, true);
	}

	@MediumTest
	public void testFlightSearchModificationsInlineReflectedInFlightSearchActivity() {
		performFlightSearchAndAssertDb("DTW", "JFK", 4, R.id.search, true);

		mSolo.clickOnView(mSolo.getView(R.id.menu_search));

		String expectedAirport1 = "ATL";
		String expectedAirport2 = "MSP";
		performFlightSearchAndAssertDb(expectedAirport1, expectedAirport2, 2, R.id.search, false);

		mSolo.goBack();

		assertSearchParamsInUi(expectedAirport1, expectedAirport2);
	}

	@MediumTest
	public void testSearchTwiceWithRotationYieldsCorrectSearchResultsInUi() {
		String air1 = "SEA";
		String air2 = "ATL";
		performFlightSearchAndAssertDb(air1, air2, 4, R.id.search, true);

		waitForFlightResults();

		mSolo.goBack();

		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		mSolo.sleep(1500);

		assertSearchParamsInUi(air1, air2);

		CalendarTouchUtils.selectDay(mSolo, 3, R.id.calendar_date_picker);

		mSolo.clickOnView(mSolo.getView(R.id.search));

		waitForFlightResults();

		String titleText = ((TextView) mSolo.getView(R.id.title_text_view)).getText().toString();
		assertTrue(titleText.contains("Atlanta"));
	}

	@MediumTest
	public void testSearchTwiceWithRotationYieldsCorrectDbState() {
		String air1 = "SFO";
		String air2 = "SEA";
		performFlightSearchAndAssertDb(air1, air2, 4, R.id.search, true);

		waitForFlightResults();

		mSolo.goBack();

		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		mSolo.sleep(1500);

		assertSearchParamsInUi(air1, air2);

		CalendarTouchUtils.selectDay(mSolo, 3, R.id.calendar_date_picker);

		mSolo.clickOnView(mSolo.getView(R.id.search));

		assertDb(air1, air2, false, null);
	}

	@MediumTest
	public void testFlightSearchParamsRetainsValuesOnRotation() {
		// select 'DTW' for departure airport
		String expectedDepartureAirportCode = "DTW";
		InputUtils.selectAirport(mInstr, mSolo, expectedDepartureAirportCode, R.id.departure_airport_edit_text);

		// select 'ATL' for arrival airport
		String expectedArrivalAirportCode = "ATL";
		InputUtils.selectAirport(mInstr, mSolo, expectedArrivalAirportCode, R.id.arrival_airport_edit_text);

		// click on dates button to engage calendar edit mode
		TouchUtils.clickView(this, Ui.findView(mActivity, R.id.dates_button));

		// click on day 3 days in future
		Time expectedStartDay = CalendarTouchUtils.selectDay(mSolo, 3, R.id.calendar_date_picker);

		mInstr.waitForIdleSync();

		mSolo.sleep(1000);

		// click the passengers button to hide calendar and does not take up the whole screen in landscape
		TouchUtils.clickView(this, Ui.findView(mActivity, R.id.passengers_button));

		// rotate screen to landscape
		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mInstr.waitForIdleSync();
		assertNotNull(mActivity);

		mSolo.sleep(1000);

		assertSearchParamsInUi(expectedDepartureAirportCode, expectedArrivalAirportCode);

		// assert the calendar text view retains the day on rotation
		EditText datesButton = (EditText) mSolo.getView(R.id.dates_button);
		String expectedMonthDay = Integer.toString(expectedStartDay.monthDay);

		assertNotNull(datesButton);
		assertTrue(datesButton.getText().toString().contains(expectedMonthDay));

		// TODO: Fix this assertion. Unable to reproduce when interacting with app manually, must fix setUp/tearDown
		// click calendar button to display cal
		//		TouchUtils.clickView(this, mSolo.getView(R.id.dates_button));
		// assert the calendar displays the correct day
		//		CalendarDatePicker calendarDatePicker = (CalendarDatePicker) mSolo.getView(R.id.calendar_date_picker);
		//		assertEquals(expectedStartDay, calendarDatePicker.getStartTime());
	}

	@MediumTest
	public void testParamsMatchRequestUrl() {
		// clear logcat so when the logcat is read it is not too large, nor there be duplicate desired strings
		LogcatUtils.clearLogcat();

		// expected airport codes
		String expectedDepartureAirport = "DTW";
		String expectedArrivalAirport = "ATL";

		// expected departure date
		final int daysOffset = 5;
		String expectedDepartureDate = CalendarTouchUtils.getDay(daysOffset).format(ISO_FORMAT);

		// perform the search
		performFlightSearchAndAssertDb(expectedDepartureAirport, expectedArrivalAirport, daysOffset, R.id.search, true);
		waitForFlightResults();

		// grab the logcat containing debug info regarding the request
		String log = LogcatUtils.readLogcat("ExpediaBookings", true);
		String requestUrl = LogcatUtils.extractRequestUrl(log);
		assertNotNull(requestUrl);

		// grab the values from the request URL
		String actualDepartureAirport = ParseUtils.extractValue(requestUrl, "departureAirport");
		String actualArrivalAirport = ParseUtils.extractValue(requestUrl, "arrivalAirport");
		String actualDepartureDate = ParseUtils.extractValue(requestUrl, "departureDate");

		// assert the actual values were retrieved from logcat

		// assert params in URL match params inserted from UI
		assertEquals(expectedDepartureAirport, actualDepartureAirport);
		assertEquals(expectedArrivalAirport, actualArrivalAirport);
		assertEquals(expectedDepartureDate, actualDepartureDate);
	}

	@MediumTest
	public void testParamsMatchRequestUrlWithModifiedSearchParams() {
		String air1 = "SEA";
		String air2 = "ATL";
		performFlightSearchAndAssertDb(air1, air2, 4, R.id.search, true);
		waitForFlightResults();

		mSolo.goBack();

		LogcatUtils.clearLogcat();

		// perform second flight search
		String expectedDepartureAirport = "DTW";
		String expectedArrivalAirport = "JFK";

		int daysOffset = 3;
		String expectedDepartureDate = CalendarTouchUtils.getDay(daysOffset).format(ISO_FORMAT);

		performFlightSearch(expectedDepartureAirport, expectedArrivalAirport, daysOffset, R.id.search);

		// grab the logcat containing debug info regarding the request
		String log = LogcatUtils.readLogcat("ExpediaBookings", true);
		String requestUrl = LogcatUtils.extractRequestUrl(log);
		assertNotNull(requestUrl);

		// grab the values from the request URL
		String actualDepartureAirport = ParseUtils.extractValue(requestUrl, "departureAirport");
		String actualArrivalAirport = ParseUtils.extractValue(requestUrl, "arrivalAirport");
		String actualDepartureDate = ParseUtils.extractValue(requestUrl, "departureDate");

		// assert params in URL match params inserted from UI
		assertEquals(expectedDepartureAirport, actualDepartureAirport);
		assertEquals(expectedArrivalAirport, actualArrivalAirport);
		assertEquals(expectedDepartureDate, actualDepartureDate);
	}

	@MediumTest
	public void testParamsMatchRequestUrlWithRotation() {
		// perform a flight search
		String dep1 = "SEA";
		String arr1 = "ATL";
		performFlightSearchAndAssertDb(dep1, arr1, 4, R.id.search, true);
		waitForFlightResults();

		// wait for results and then go back
		//		waitForFlightResults();
		mSolo.goBack();

		// click on dates button to remove the calendar as the display/content fragment
		mSolo.clickOnView(mSolo.getView(R.id.passengers_button));

		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// clear logcat so when the logcat is read it is not too large, nor there be duplicate desired strings
		LogcatUtils.clearLogcat();

		// perform second flight search
		String expectedDepartureAirport = "SFO";
		String expectedArrivalAirport = "PDX";

		int daysOffset = 3;
		String expectedDepartureDate = CalendarTouchUtils.getDay(daysOffset).format(ISO_FORMAT);

		performFlightSearch(expectedDepartureAirport, expectedArrivalAirport, daysOffset, R.id.search);

		// grab the logcat containing debug info regarding the request
		String log = LogcatUtils.readLogcat("ExpediaBookings", true);
		String requestUrl = LogcatUtils.extractRequestUrl(log);
		assertNotNull(requestUrl);

		// grab the values from the request URL
		String actualDepartureAirport = ParseUtils.extractValue(requestUrl, "departureAirport");
		String actualArrivalAirport = ParseUtils.extractValue(requestUrl, "arrivalAirport");
		String actualDepartureDate = ParseUtils.extractValue(requestUrl, "departureDate");

		// assert params in URL match params inserted from UI
		assertEquals(expectedDepartureAirport, actualDepartureAirport);
		assertEquals(expectedArrivalAirport, actualArrivalAirport);
		assertEquals(expectedDepartureDate, actualDepartureDate);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// HELPER METHODS

	/**
	 * Helper method that will perform a flight search for the given airports and days
	 * @param air1
	 * @param air2
	 * @param daysOffset
	 */
	private void performFlightSearchAndAssertDb(String air1, String air2, int daysOffset, int searchId,
			boolean assertDate) {
		Time expectedDay = performFlightSearch(air1, air2, daysOffset, searchId);

		assertDb(air1, air2, assertDate, expectedDay);
	}

	private void performFlightSearchAndAssertUi(String air1, String air2, int daysOffset, int searchId,
			String title) {
		performFlightSearch(air1, air2, daysOffset, searchId);

		waitForFlightResults();

		String titleText = ((TextView) mSolo.getView(R.id.title_text_view)).getText().toString();
		assertTrue(titleText.contains(title));
	}

	private void waitForFlightResults() {
		mSolo.waitForActivity(FlightSearchResultsActivity.class.getName(), 2000);
		mSolo.waitForFragmentByTag(StatusFragment.TAG, 2000);
		mSolo.waitForFragmentByTag(FlightListFragment.TAG, 30000);
	}

	private Time performFlightSearch(String air1, String air2, int daysOffset, int searchId) {
		InputUtils.selectAirport(mInstr, mSolo, air1, R.id.departure_airport_edit_text);

		InputUtils.selectAirport(mInstr, mSolo, air2, R.id.arrival_airport_edit_text);

		// click dates button so that the calendar appears
		mSolo.clickOnView(mSolo.getView(R.id.dates_button));
		mSolo.sleep(1500);

		// select a day 'daysOffset' in the future
		Time expectedDay = CalendarTouchUtils.selectDay(mSolo, daysOffset, R.id.calendar_date_picker);

		// search so that the FlightSearchParamsFragment closes and saves params to Db
		mSolo.clickOnView(mSolo.getView(searchId));

		return expectedDay;
	}

	private void assertDb(String air1, String air2, boolean assertDate, Time expectedDay) {
		// grab and assert values from FlightSearchParams (from Db)
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();

		assertEquals(air1, params.getDepartureAirportCode());
		assertEquals(air2, params.getArrivalAirportCode());
		if (assertDate) {
			assertEquals(expectedDay, new Time(params.getDepartureDate().getCalendar()));
		}
	}

	private void assertSearchParamsInUi(String expectedDepartureAirportCode, String expectedArrivalAirportCode) {
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
	}

}