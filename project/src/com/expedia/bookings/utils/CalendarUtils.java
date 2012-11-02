package com.expedia.bookings.utils;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SearchParams;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.VintageCalendarDatePicker;

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
		if (start.get(Calendar.YEAR) > end.get(Calendar.YEAR)
				|| (start.get(Calendar.YEAR) == end.get(Calendar.YEAR) && start.get(Calendar.DAY_OF_YEAR) > end
						.get(Calendar.DAY_OF_YEAR))) {
			Calendar tmp = end;
			end = start;
			start = tmp;
			reverse = true;
		}

		Calendar cal = (Calendar) start.clone();
		int endDay = end.get(Calendar.DAY_OF_YEAR);
		int endYear = end.get(Calendar.YEAR);
		long daysBetween = 0;
		while (cal.get(Calendar.DAY_OF_YEAR) != endDay || cal.get(Calendar.YEAR) != endYear) {
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
	 * Configures the calendar date picker for Hotels
	 * 
	 * @param calendarDatePicker
	 * @param mode
	 */
	public static void configureCalendarDatePicker(CalendarDatePicker calendarDatePicker,
			CalendarDatePicker.SelectionMode mode) {
		// Always set these variables
		calendarDatePicker.setSelectionMode(mode);
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

	/**
	 * Configures the calendar date picker for Flights
	 *
	 * @param calendarDatePicker
	 * @param mode
	 */
	public static void configureCalendarDatePickerForFlights(CalendarDatePicker calendarDatePicker,
			CalendarDatePicker.SelectionMode mode) {
		// Always set these variables
		calendarDatePicker.setSelectionMode(mode);
		calendarDatePicker.setMaxRange(330);

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

	// #9770: Add an hour of buffer so that the date range is always > the number of days
	private static final int DATE_RANGE_BUFFER = 1000 * 60 * 60; // 1 hour

	/**
	 * Convenience method for formatting date range represented by a particular SearchParams.
	 * 
	 * @param context the context
	 * @param searchParams the params to format
	 * @return a numeric representation of the stay range (e.g., "10/31 - 11/04").
	 */
	public static String formatDateRange(Context context, SearchParams searchParams) {
		return DateUtils.formatDateRange(context, searchParams.getCheckInDate().getTimeInMillis(),
				searchParams.getCheckOutDate().getTimeInMillis() + DATE_RANGE_BUFFER,
				DateUtils.FORMAT_NUMERIC_DATE + DateUtils.FORMAT_UTC);
	}

	public static CharSequence getCalendarDatePickerTitle(Context context) {
		int nights = Db.getSearchParams().getStayDuration();
		if (nights <= 1) {
			return Html.fromHtml(context.getString(R.string.drag_to_extend_your_stay));
		}
		else {
			return context.getResources().getQuantityString(R.plurals.length_of_stay, nights, nights);
		}
	}

	public static boolean isSearchDateTonight() {
		SearchParams params = Db.getSearchParams();
		com.expedia.bookings.data.Date today = new com.expedia.bookings.data.Date(Calendar.getInstance());
		com.expedia.bookings.data.Date checkIn = new com.expedia.bookings.data.Date(params.getCheckInDate());
		return params.getStayDuration() == 1 && today.equals(checkIn);
	}
}
