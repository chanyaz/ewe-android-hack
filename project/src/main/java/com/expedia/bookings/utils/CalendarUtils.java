package com.expedia.bookings.utils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.text.HtmlCompat;
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
		long now = DateTime.now().getMillis();
		return now < timestamp || timestamp + cutoff < now;
	}

	/**
	 * Configures the calendar date picker for the specified
	 * line of business and mode
	 */
	public static void configureCalendarDatePicker(CalendarDatePicker calendarDatePicker,
		CalendarDatePicker.SelectionMode mode, LineOfBusiness business) {
		// Set max calendar date
		Time maxTime = new Time(System.currentTimeMillis());
		// Always set these variables
		calendarDatePicker.setSelectionMode(mode);
		if (business == LineOfBusiness.FLIGHTS) {
			calendarDatePicker.setMaxRange(330);
			maxTime.monthDay += 330;
		}
		else if (business == LineOfBusiness.HOTELS) {
			if (mode == CalendarDatePicker.SelectionMode.HYBRID) {
				calendarDatePicker.setMinRange(2);
				calendarDatePicker.setMaxRange(500);
			}
			else {
				calendarDatePicker.setMaxRange(29);
			}
			maxTime.monthDay += 500;
		}
		maxTime.normalize(true);

		// Reset the calendar's today cache
		calendarDatePicker.resetTodayCache();

		// Set the min calendar date
		LocalDate today = LocalDate.now();
		calendarDatePicker.setMinDate(today.getYear(), today.getMonthOfYear() - 1, today.getDayOfMonth());

		// Reset the calendar's today cache
		calendarDatePicker.resetTodayCache();

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
			searchParams.setCheckInDate(null);
		}
		else {
			LocalDate startDate = new LocalDate(picker.getStartYear(), picker.getStartMonth() + 1,
					picker.getStartDayOfMonth());
			searchParams.setCheckInDate(startDate);
		}
		if (picker.getEndTime() == null) {
			searchParams.setCheckOutDate(null);
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
			picker.updateEndDate(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
		}
	}

	public static CharSequence getCalendarDatePickerTitle(Context context, HotelSearchParams params) {
		int nights = params.getStayDuration();
		if (params.getCheckInDate() == null && params.getCheckOutDate() == null) {
			return context.getResources().getString(R.string.calendar_instructions_hotels_no_dates_selected);
		}
		else if (nights <= 1) {
			return HtmlCompat.fromHtml(context.getString(R.string.drag_to_extend_your_stay));
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
