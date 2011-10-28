package com.expedia.bookings.utils;

import java.util.Calendar;
import java.util.TimeZone;

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
}
