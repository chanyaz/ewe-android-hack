package com.expedia.bookings.test.utils;

import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;

/**
 * Created with IntelliJ IDEA.
 * User: brad
 * Date: 7/13/12
 * Time: 10:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class CalendarTouchUtils {

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

}
