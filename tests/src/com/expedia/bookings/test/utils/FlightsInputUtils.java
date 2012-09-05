package com.expedia.bookings.test.utils;

import android.app.Instrumentation;
import com.expedia.bookings.R;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.testutils.CalendarTouchUtils;
import com.mobiata.testutils.InputUtils;

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
		InputUtils.selectAirport(instr, solo, air1, R.id.departure_airport_edit_text);

		InputUtils.selectAirport(instr, solo, air2, R.id.arrival_airport_edit_text);

		// click dates button so that the calendar appears
		solo.clickOnView(solo.getView(R.id.dates_button));
		solo.sleep(1500);

		// select a day 'daysOffset' in the future
		CalendarTouchUtils.selectDay(solo, day, R.id.calendar_date_picker);

		// search so that the FlightSearchParamsFragment closes and saves params to Db
		solo.clickOnView(solo.getView(searchId));
	}

}
