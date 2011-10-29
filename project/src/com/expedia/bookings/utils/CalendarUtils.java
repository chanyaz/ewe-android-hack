package com.expedia.bookings.utils;

import java.util.Calendar;
import java.util.TimeZone;

import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.SelectionMode;

public class CalendarUtils {
	/**
	 * This ACCURATELY calculates the difference (in days) between two Calendars.
	 * 
	 * WARNING: IT IS NOT REMOTELY FAST.  It is VERY slow.  You should only use this
	 * if you know that the difference in days has a low bound (a couple months max).
	 * @param cal1 the first calendar
	 * @param cal2 the second calendar
	 * @return positive number of days between if cal1 < cal2, otherwise negative days between
	 */
	public static long getDaysBetween(Calendar start, Calendar end) {
		boolean reverse = false;
		if (start.after(end)) {
			Calendar tmp = end;
			end = start;
			start = tmp;
			reverse = true;
		}

		Calendar cal = (Calendar) start.clone();
		long daysBetween = 0;
		while (cal.before(end)) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
			daysBetween++;
		}

		return (reverse) ? -daysBetween : daysBetween;
	}

	public static TimeZone getFormatTimeZone() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		if (tz == null) {
			return TimeZone.getDefault();
		}
		return tz;
	}

	/**
	 * Configures the calendar date picker for the app
	 * 
	 * @param calendarDatePicker
	 */
	public static void configureCalendarDatePicker(CalendarDatePicker calendarDatePicker) {
		// Always set these variables
		calendarDatePicker.setSelectionMode(SelectionMode.RANGE);
		calendarDatePicker.setMaxRange(29);

		/* Set the min calendar date
		 * 
		 * 7880: initializing the date on the calendar to 1 day prior to
		 * the current date so that the date is selectable by the user
		 * for searches in other timezones where its still a day behind
		 */
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DAY_OF_MONTH, -1);

		calendarDatePicker.setMinDate(yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH),
				yesterday.get(Calendar.DAY_OF_MONTH));

		// Set max calendar date
		Time maxTime = new Time(System.currentTimeMillis());
		maxTime.monthDay += 330;
		maxTime.normalize(true);

		calendarDatePicker.setMaxDate(maxTime.year, maxTime.month, maxTime.monthDay);
	}
}
