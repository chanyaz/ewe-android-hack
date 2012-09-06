package com.expedia.bookings.test.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.view.View;
import com.expedia.bookings.R;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.Log;
import com.mobiata.android.text.format.Time;
import com.mobiata.testutils.CalendarTouchUtils;

/**
 * The purpose of this class file is to provide a set of utility methods for interacting with the flights application
 * programmatically via test
 */
public class FlightsInputUtils {

	public static Time performFlightSearch(Instrumentation instr, Solo solo, String air1, String air2,
			int daysOffset,
			int searchId) {
		Time day = CalendarTouchUtils.getDay(daysOffset);

		performFlightSearchInternal(instr, solo, air1, air2, day, searchId);

		return day;
	}

	public static void performFlightSearch(Instrumentation instr, Solo solo, String air1, String air2, Time day,
			int searchId) {
		performFlightSearchInternal(instr, solo, air1, air2, day, searchId);
	}

	private static void performFlightSearchInternal(Instrumentation instr, Solo solo, String air1, String air2,
			Time day,
			int searchId) {
		selectAirport(instr, solo, air1, R.id.departure_airport_edit_text);

		selectAirport(instr, solo, air2, R.id.arrival_airport_edit_text);

		// click dates button so that the calendar appears
		solo.clickOnView(solo.getView(R.id.dates_button));
		solo.sleep(1500);

		// select a day 'daysOffset' in the future
		CalendarTouchUtils.selectDay(solo, day, R.id.calendar_date_picker);

		// search so that the FlightSearchParamsFragment closes and saves params to Db
		// NOTE: this is done by invoking the menu via instrumentation because for some reason Robotium (and TouchUtils)
		// barf at finding the search view (which is within the ActionBar and thus considered internally as a menu) when
		// the FlightSearchOverlayActivity is launched
		instr.invokeMenuActionSync(solo.getCurrentActivity(), searchId, 0);
	}

	/**
	 * Sends the airport to the appropriate view via sendStringSync. make sure to click the appropriate item in the list
	 * @param str - airport code to be selected
	 * @param id - id of the view that is accepting the airport code
	 */
	public static void selectAirport(Instrumentation instr, Solo solo, String str, final int id) {
		solo.clickOnView(solo.getView(id));
		instr.sendStringSync(str);

		// sleep for 3 seconds in order to ensure the AirportCursorAdapter can properly filter the list before we click
		// it. This operation is especially slow in the emulator. after 3 seconds the list should theoretically settle.
		solo.sleep(3000);

		solo.clickInList(1);
	}

}
