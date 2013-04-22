package com.expedia.bookings.utils;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SearchParams;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;

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
		if (mode == CalendarDatePicker.SelectionMode.RANGE) {
			calendarDatePicker.setMaxRange(29);
		}
		else if (mode == CalendarDatePicker.SelectionMode.HYBRID) {
			calendarDatePicker.setMaxRange(330);
		}

		// Reset the calendar's today cache
		calendarDatePicker.resetTodayCache();

		// Set the min calendar date
		Calendar today = Calendar.getInstance();
		calendarDatePicker.setMinDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH));

		// Reset the calendar's today cache
		calendarDatePicker.resetTodayCache();

		// Set max calendar date
		Time maxTime = new Time(System.currentTimeMillis());
		maxTime.monthDay += 330;
		maxTime.normalize(true);

		calendarDatePicker.setMaxDate(maxTime.year, maxTime.month, maxTime.monthDay);
	}

	public static void syncParamsFromDatePicker(SearchParams searchParams, CalendarDatePicker picker) {
		if (picker.getSelectionMode() != CalendarDatePicker.SelectionMode.RANGE) {
			throw new UnsupportedOperationException("Can't use syncParamsFromDatePicker with picker of type "
					+ picker.getSelectionMode());
		}
		Date startDate = new Date(picker.getStartYear(), picker.getStartMonth() + 1, picker.getStartDayOfMonth());
		Date endDate = new Date(picker.getEndYear(), picker.getEndMonth() + 1, picker.getEndDayOfMonth());

		// Ensure the dates from the picker are valid before using them
		Date nowDate = new Date();
		nowDate.fromCalendar(Calendar.getInstance());

		boolean bogus = startDate.before(nowDate);
		if (bogus) {
			// Reset the SearchParams and Calendar to default stay if we somehow got bogus values from picker
			searchParams.setDefaultStay();
			Calendar checkIn = searchParams.getCheckInDate();
			Calendar checkOut = searchParams.getCheckOutDate();

			picker.updateStartDate(checkIn.get(Calendar.YEAR), checkIn.get(Calendar.MONTH),
					checkIn.get(Calendar.DAY_OF_MONTH));
			picker.updateEndDate(checkOut.get(Calendar.YEAR), checkOut.get(Calendar.MONTH),
					checkOut.get(Calendar.DAY_OF_MONTH));
		}
		else {
			searchParams.setCheckInDate(startDate);
			searchParams.setCheckOutDate(endDate);
		}
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

	public static CharSequence getCalendarDatePickerTitle(Context context, SearchParams params) {
		int nights = params.getStayDuration();
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
