package com.expedia.bookings.utils;

import java.util.Calendar;
import java.util.TimeZone;

import org.joda.time.LocalDate;

import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;

public class CalendarUtils {

	/**
	 * Checks if a timestamp has expired, given a particular cutoff
	 * 
	 * Returns false if somehow the timestamp is in front of now (which should be impossible)
	 * or the timestamp is more than "cutoff" away
	 */
	public static boolean isExpired(long timestamp, long cutoff) {
		long now = Calendar.getInstance().getTimeInMillis();
		return now < timestamp || timestamp + cutoff < now;
	}

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
	 * Configures the calendar date picker for the specified
	 * line of business and mode
	 *
	 * @param calendarDatePicker
	 * @param mode
	 */
	public static void configureCalendarDatePicker(CalendarDatePicker calendarDatePicker,
			CalendarDatePicker.SelectionMode mode, LineOfBusiness business) {
		// Always set these variables
		calendarDatePicker.setSelectionMode(mode);
		if (business == LineOfBusiness.FLIGHTS) {
			calendarDatePicker.setMaxRange(330);
		}
		else if (business == LineOfBusiness.HOTELS) {
			if (mode == CalendarDatePicker.SelectionMode.HYBRID) {
				calendarDatePicker.setMinRange(2);
				calendarDatePicker.setMaxRange(330);
			}
			else {
				calendarDatePicker.setMaxRange(29);
			}
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

	public static void syncParamsFromDatePickerRange(HotelSearchParams searchParams, CalendarDatePicker picker) {
		if (picker.getSelectionMode() != CalendarDatePicker.SelectionMode.RANGE) {
			throw new UnsupportedOperationException("Can't use syncParamsFromDatePicker with picker of type "
					+ picker.getSelectionMode());
		}
		LocalDate startDate = new LocalDate(picker.getStartYear(), picker.getStartMonth() + 1,
				picker.getStartDayOfMonth());
		LocalDate endDate = new LocalDate(picker.getEndYear(), picker.getEndMonth() + 1, picker.getEndDayOfMonth());

		// Ensure the dates from the picker are valid before using them
		LocalDate nowDate = LocalDate.now();

		boolean bogus = startDate.isBefore(nowDate);
		if (bogus) {
			// Reset the HotelSearchParams and Calendar to default stay if we somehow got bogus values from picker
			searchParams.setDefaultStay();

			updateCalendarPickerStartDate(picker, searchParams.getCheckInDate());
			updateCalendarPickerEndDate(picker, searchParams.getCheckOutDate());
		}
		else {
			searchParams.setCheckInDate(startDate);
			searchParams.setCheckOutDate(endDate);
		}
	}

	public static void syncParamsFromDatePickerHybrid(HotelSearchParams searchParams, CalendarDatePicker picker) {
		if (picker.getSelectionMode() != CalendarDatePicker.SelectionMode.HYBRID) {
			throw new UnsupportedOperationException("Can't use syncParamsFromDatePickerHybrid with picker of type "
					+ picker.getSelectionMode());
		}

		if (picker.getStartTime() == null) {
			searchParams.setCheckInDate((LocalDate) null);
		}
		else {
			LocalDate startDate = new LocalDate(picker.getStartYear(), picker.getStartMonth() + 1,
					picker.getStartDayOfMonth());
			searchParams.setCheckInDate(startDate);
		}
		if (picker.getEndTime() == null) {
			searchParams.setCheckOutDate((LocalDate) null);
		}
		else {
			LocalDate endDate = new LocalDate(picker.getEndYear(), picker.getEndMonth() + 1, picker.getEndDayOfMonth());
			searchParams.setCheckOutDate(endDate);
		}
	}

	public static void updateCalendarPickerStartDate(CalendarDatePicker picker, LocalDate date) {
		if (date != null) {
			picker.updateStartDate(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
		}
	}

	public static void updateCalendarPickerEndDate(CalendarDatePicker picker, LocalDate date) {
		if (date != null) {
			picker.updateStartDate(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
		}
	}

	// #9770: Add an hour of buffer so that the date range is always > the number of days
	private static final long DATE_RANGE_BUFFER = DateUtils.HOUR_IN_MILLIS;

	/**
	 * Convenience method for formatting date range represented by a particular HotelSearchParams.
	 *
	 * @param context the context
	 * @param searchParams the params to format
	 * @return a numeric representation of the stay range (e.g., "10/31 - 11/04").
	 */
	public static String formatDateRange(Context context, HotelSearchParams searchParams) {
		return formatDateRange(context, searchParams, DateUtils.FORMAT_NUMERIC_DATE);
	}

	public static String formatDateRange(Context context, HotelSearchParams searchParams, int flags) {
		return DateUtils.formatDateRange(context, searchParams.getCheckInDate().toDateTimeAtStartOfDay().getMillis(),
				searchParams.getCheckOutDate().toDateTimeAtStartOfDay().getMillis() + DATE_RANGE_BUFFER, flags);
	}

	/**
	 * Alternative formatter - instead of solely using the system formatter, it is more of "DATE to DATE"
	 */
	public static String formatDateRange2(Context context, HotelSearchParams params, int flags) {
		CharSequence from = DateUtils.formatDateTime(context, params.getCheckInDate().toDateTimeAtStartOfDay()
				.getMillis(), flags);
		CharSequence to = DateUtils.formatDateTime(context, params.getCheckOutDate().toDateTimeAtStartOfDay()
				.getMillis(), flags);
		return context.getString(R.string.date_range_TEMPLATE, from, to);
	}

	public static CharSequence getCalendarDatePickerTitle(Context context, HotelSearchParams params) {
		int nights = params.getStayDuration();
		if (params.getCheckInDate() == null && params.getCheckOutDate() == null) {
			return context.getResources().getString(R.string.calendar_instructions_hotels_no_dates_selected);
		}
		else if (nights <= 1) {
			return Html.fromHtml(context.getString(R.string.drag_to_extend_your_stay));
		}
		else {
			return context.getResources().getQuantityString(R.plurals.length_of_stay, nights, nights);
		}
	}

	public static boolean isSearchDateTonight(HotelSearchParams params) {
		LocalDate now = LocalDate.now();
		LocalDate checkIn = params.getCheckInDate();
		return params.getStayDuration() == 1 && now.equals(checkIn);
	}
}
