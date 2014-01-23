package com.expedia.bookings.test.utils;

import com.mobiata.android.Log;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;
import com.robotium.solo.Solo;

public class CalendarTouchUtils {

	private static final String TAG = "CalendarTouchUtils";

	/**
	 * Select the given day based on offset and return this day as a Time object
	 * @param solo - Robotium solo class
	 * @param daysOffset - number of days offset from today to select
	 * @param id - id of the calendar view to click
	 * @return Time object representing the offset, expected value to be used by tests
	 */
	public static Time selectDay(Solo solo, int daysOffset, int id) {
		Time day = getDay(daysOffset);

		selectDayInternal(solo, day, id);

		return day;
	}

	public static void selectDay(Solo solo, Time day, int id) {
		selectDayInternal(solo, day, id);
	}

	private static void selectDayInternal(Solo solo, Time day, int id) {
		CalendarDatePicker cal = (CalendarDatePicker) solo.getCurrentActivity().findViewById(id);
		CalendarTouchUtils.clickOnDay(solo, cal, day);
	}

	/**
	 * Retrieve a time object with a given offset in days
	 */
	public static Time getDay(int daysOffset) {
		Time time = new Time(System.currentTimeMillis());
		time.monthDay += daysOffset;
		time.normalize(true);

		return time;
	}

	/**
	 * Returns true if the day to be clicked is present on the current month grid
	 */
	public static boolean clickOnDay(Solo solo, CalendarDatePicker cal, Time day) {
		float x = cal.getX(day);
		float y = cal.getY(day);

		if (x >= 0.0f && y >= 0.0f) {
			solo.clickOnScreen(cal.getX(day), cal.getY(day));
			return true;
		}
		return false;
	}

	public static void clickNextMonth(Solo solo, CalendarDatePicker cal) {
		solo.clickOnScreen(cal.getNextMonthX(), cal.getNextMonthY());
	}

	public static void clickPrevMonth(Solo solo, CalendarDatePicker cal) {
		solo.clickOnScreen(cal.getPrevMonthX(), cal.getPrevMonthY());
	}

	public static void clickOnFutureMonthDay(Solo solo, CalendarDatePicker cal, Time time) {
		advanceToMonth(solo, cal, time);
		clickOnDay(solo, cal, time);
	}

	public static void advanceToMonth(Solo solo, CalendarDatePicker cal, Time time) {
		int numClicksNecessary;
		int startYear = cal.getStartYear();
		int startMonth = cal.getStartMonth();
		if (startYear == time.year) {
			numClicksNecessary = time.month - startMonth;
		}
		else {
			numClicksNecessary = time.month - startMonth + 12;
		}
		Log.d(TAG, "current month/year: " + startMonth + "/" + startYear +
				", desired month/year: " + time.month + "/" + time.year +
				" numClicksNecessary: " + numClicksNecessary);
		for (int i = 0; i < java.lang.Math.abs(numClicksNecessary); i++) {
			if (numClicksNecessary > 0) {
				clickNextMonth(solo, cal);
			}
			else if (numClicksNecessary < 0) {
				clickPrevMonth(solo, cal);
			}
		}
	}

	public static void dragOnCalendar(Solo solo, CalendarDatePicker cal, Time from, Time to) {
		dragOnCalendar(solo, cal, from, to, 40);
	}

	public static void dragOnCalendar(Solo solo, CalendarDatePicker cal, Time from, Time to, int stepCount) {
		float fromX = cal.getX(from);
		float fromY = cal.getY(from);

		float toX = cal.getX(to);
		float toY = cal.getY(to);

		solo.drag(fromX, toX, fromY, toY, stepCount);

		// sleep to ensure the system settles down and the calendar has time to process changes before tests assert
		solo.sleep(500);
	}

}
