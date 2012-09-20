package com.expedia.bookings.test.tests.integration;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.FlightSearchResultsActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.fragment.FlightListFragment;
import com.expedia.bookings.fragment.StatusFragment;
import com.expedia.bookings.test.utils.FlightsInputUtils;
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
		Activity activity = mInstr.waitForMonitorWithTimeout(mMonitor, 1000);
		assertNotNull(activity);
		assertEquals(FlightSearchActivity.class, activity.getClass());

		mSolo = new Solo(mInstr, activity);
		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		mSolo.finishOpenedActivities();
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
		performFlightSearch("DTW", "JFK", 4, R.id.search);

		// click on menu search to bring up FlightSearchOverlay for inline search
		mSolo.clickOnView(mSolo.getView(R.id.menu_search));

		// perform another, modified search
		String expectedAirport1 = "ATL";
		String expectedAirport2 = "MSP";
		performFlightSearch(expectedAirport1, expectedAirport2, 2, R.id.search);

		// go back
		mSolo.goBack();

		// assert the search params are reflected in the first activity
		assertSearchParamsInUi(expectedAirport1, expectedAirport2);
	}

	@MediumTest
	public void testSearchModificationsInlineWithRotationsReflectedInFlightSearchActivity() {
		performFlightSearch("PDX", "SEA", 2, R.id.search);

		// click on menu search to bring up FlightSearchOverlay for inline search
		mSolo.clickOnView(mSolo.getView(R.id.menu_search));

		String expectedAirport1 = "IAH";
		String expectedAirport2 = "JFK";

		FlightsInputUtils.selectAirport(mInstr, mSolo, expectedAirport1, R.id.departure_airport_edit_text);
		FlightsInputUtils.selectAirport(mInstr, mSolo, expectedAirport2, R.id.arrival_airport_edit_text);
		CalendarTouchUtils.selectDay(mSolo, 4, R.id.calendar_date_picker);

		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		mInstr.waitForIdleSync();

		mInstr.invokeMenuActionSync(mSolo.getCurrentActivity(), R.id.search, 0);

		assertSearchParamsInUi(expectedAirport1, expectedAirport2);
	}

	@MediumTest
	public void testSearchTwiceYieldsCorrectSearchResultsInUi() {
		performFlightSearch("DTW", "JFK", 4, R.id.search);

		mSolo.goBack();

		performFlightSearchAndAssertUi("ATL", "IAH", 3, R.id.search, "Houston");
	}

	@MediumTest
	public void testSearchTwiceWithRotationYieldsCorrectSearchResultsInUi() {
		performFlightSearch("SEA", "ATL", 4, R.id.search);

		mSolo.goBack();
		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mSolo.sleep(1500);

		performFlightSearchAndAssertUi("MSP", "DTW", 3, R.id.search, "Detroit");
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
		FlightsInputUtils.selectAirport(mInstr, mSolo, expectedDepartureAirportCode, R.id.departure_airport_edit_text);

		// select 'ATL' for arrival airport
		String expectedArrivalAirportCode = "ATL";
		FlightsInputUtils.selectAirport(mInstr, mSolo, expectedArrivalAirportCode, R.id.arrival_airport_edit_text);

		// click on dates button to engage calendar edit mode
		TouchUtils.clickView(this, Ui.findView(mSolo.getCurrentActivity(), R.id.dates_button));

		// click on day 3 days in future
		Time expectedStartDay = CalendarTouchUtils.selectDay(mSolo, 3, R.id.calendar_date_picker);

		mInstr.waitForIdleSync();

		mSolo.sleep(1000);

		// click the passengers button to hide calendar and does not take up the whole screen in landscape
		TouchUtils.clickView(this, Ui.findView(mSolo.getCurrentActivity(), R.id.travelers_button));

		// rotate screen to landscape
		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mInstr.waitForIdleSync();
		assertNotNull(mSolo.getCurrentActivity());

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

		// grab request url
		String requestUrl = retrieveRequestUrl();

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

		// grab request url
		String requestUrl = retrieveRequestUrl();

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
		mSolo.clickOnView(mSolo.getView(R.id.travelers_button));

		mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// clear logcat so when the logcat is read it is not too large, nor there be duplicate desired strings
		LogcatUtils.clearLogcat();

		// perform second flight search
		String expectedDepartureAirport = "SFO";
		String expectedArrivalAirport = "PDX";

		int daysOffset = 3;
		String expectedDepartureDate = CalendarTouchUtils.getDay(daysOffset).format(ISO_FORMAT);

		performFlightSearch(expectedDepartureAirport, expectedArrivalAirport, daysOffset, R.id.search);

		// grab request url
		String requestUrl = retrieveRequestUrl();

		// grab the values from the request URL
		String actualDepartureAirport = ParseUtils.extractValue(requestUrl, "departureAirport");
		String actualArrivalAirport = ParseUtils.extractValue(requestUrl, "arrivalAirport");
		String actualDepartureDate = ParseUtils.extractValue(requestUrl, "departureDate");

		// assert params in URL match params inserted from UI
		assertEquals(expectedDepartureAirport, actualDepartureAirport);
		assertEquals(expectedArrivalAirport, actualArrivalAirport);
		assertEquals(expectedDepartureDate, actualDepartureDate);
	}

	@SmallTest
	public void testActionBarPopupDropdownBackBehavior() {
		performFlightSearch("ATL", "JFK", 4, R.id.search);

		waitForFlightResults();

		FlightsInputUtils.clickOnActionBarCustomView(mSolo);

		mSolo.goBack();

		assertEquals(FlightSearchResultsActivity.class, mSolo.getCurrentActivity().getClass());
	}

	@SmallTest
	public void testActionBarPopupDropdownDismissOnOutsideClick() {
		performFlightSearch("ATL", "JFK", 4, R.id.search);

		waitForFlightResults();

		FlightsInputUtils.clickOnActionBarCustomView(mSolo);

		View dropDown = mSolo.getView(R.id.nav_dropdown_list);
		assertNotNull(dropDown);

		// click in the bottom right corner of the screen, theoretically should not be the dropdown
		Display display = mSolo.getCurrentActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		mSolo.clickOnScreen(width * .75f, height * .75f);

		mSolo.sleep(1000);

		View dropDownAgain = mSolo.getView(R.id.nav_dropdown_list);
		assertNull(dropDownAgain);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// HELPER METHODS

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
		return FlightsInputUtils.performFlightSearch(mInstr, mSolo, air1, air2, daysOffset, searchId);
	}

	private void assertDb(String air1, String air2, boolean assertDate, Time expectedDay) {
		// grab and assert values from FlightSearchParams (from Db)
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();

		assertEquals(air1, params.getDepartureLocation().getDestinationId());
		assertEquals(air2, params.getArrivalLocation().getDestinationId());
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

	private String retrieveRequestUrl() {
		// sleep to combat logcat lag
		mSolo.sleep(1000);

		String log = LogcatUtils.readLogcat("ExpediaBookings", true);
		assertNotNull(log);
		String requestUrl = LogcatUtils.extractRequestUrl(log);
		assertNotNull(requestUrl);

		return requestUrl;
	}
}