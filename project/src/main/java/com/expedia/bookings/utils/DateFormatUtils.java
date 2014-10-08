package com.expedia.bookings.utils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.content.Context;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;

/**
 */
public class DateFormatUtils {

	/**
	 * This is the equivalent of android.text.format.getDateFormat(), when used in DateUtils
	 */
	public static final int FLAGS_DATE_NUMERIC = DateUtils.FORMAT_NUMERIC_DATE;
	/**
	 * This is the equivalent of android.text.format.getLongDateFormat(), when used in DateUtils
	 */
	public static final int FLAGS_LONG_DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
		| DateUtils.FORMAT_SHOW_YEAR;

	/**
	 * This is the equivalent of android.text.format.getMediumDateFormat(), when used in DateUtils
	 */
	public static final int FLAGS_MEDIUM_DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH |
		DateUtils.FORMAT_SHOW_YEAR;
	/**
	 * This is the equivalent of android.text.format.getTimeFormat(), when used in DateUtils
	 */
	public static final int FLAGS_TIME_FORMAT = DateUtils.FORMAT_SHOW_TIME;

	/**
	 * Formatted Date example: April 12
	 * <p/>
	 * When used in {@link DateFormatUtils#formatDateRange(Context, LocalDate, LocalDate, int)} returns April 12 - 15
	 */
	public static final int FLAGS_DATE_SHOW = DateUtils.FORMAT_SHOW_DATE;

	/**
	 * Formatted Date example: Apr 12
	 * <p/>
	 * When used in {@link DateFormatUtils#formatDateRange(Context, LocalDate, LocalDate, int)} returns Apr 12 - 15
	 */
	public static final int FLAGS_DATE_ABBREV_ALL = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL;

	/**
	 * Formatted Date example: Apr 12
	 * <p/>
	 * When used in {@link DateFormatUtils#formatDateRange(Context, LocalDate, LocalDate, int)} returns Apr 12 - 15
	 */
	public static final int FLAGS_DATE_NO_YEAR_ABBREV_MONTH_ABBREV_WEEKDAY = DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY;

	/**
	 * Formatted Date example: Apr 12
	 * <p/>
	 * When used in {@link DateFormatUtils#formatDateRange(Context, FlightSearchParams, int)} or {@link DateFormatUtils#formatDateRange(Context, HotelSearchParams, int)} returns Apr 12 - 15
	 */
	public static final int FLAGS_DATE_ABBREV_MONTH = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;

	// #9770: Add an hour of buffer so that the date range is always > the number of days
	private static final long DATE_RANGE_BUFFER = DateUtils.HOUR_IN_MILLIS;

	public static String formatDateRange(Context context, LocalDate start, LocalDate end, int flags) {
		return formatDateRange(context, start.toDateTimeAtStartOfDay(), end.toDateTimeAtStartOfDay(), flags);
	}

	public static String formatDateRange(Context context, DateTime start, DateTime end, int flags) {
		return DateUtils.formatDateRange(context, start.getMillis(), end.getMillis() + 1000, flags
			| DateUtils.FORMAT_UTC);
	}

	/**
	 * Convenience method for formatting date range represented by a particular HotelSearchParams.
	 *
	 * @param context      the context
	 * @param searchParams the params to format
	 * @return a numeric representation of the stay range (e.g., "10/31 - 11/04").
	 */
	public static String formatDateRange(Context context, HotelSearchParams searchParams) {
		return formatDateRange(context, searchParams, FLAGS_DATE_NUMERIC);
	}

	public static String formatDateRange(Context context, HotelSearchParams searchParams, int flags) {
		return DateUtils.formatDateRange(context, searchParams.getCheckInDate().toDateTimeAtStartOfDay().getMillis(),
			searchParams.getCheckOutDate().toDateTimeAtStartOfDay().getMillis() + DATE_RANGE_BUFFER, flags);
	}

	public static String formatDateRange(Context context, FlightSearchParams searchParams, int flags) {
		// If it's a two-way flight, let's format the date range from departure - arrival
		if (searchParams.getReturnDate() != null) {
			return DateUtils.formatDateRange(context, searchParams.getDepartureDate().toDateTimeAtStartOfDay()
					.getMillis(),
				searchParams.getReturnDate().toDateTimeAtStartOfDay().getMillis() + DATE_RANGE_BUFFER, flags);
		}
		else {
			// If it's a one-way flight, let's just send the formatted departure date.
			return DateUtils.formatDateTime(context, searchParams.getDepartureDate().toDateTimeAtStartOfDay()
				.getMillis(), flags);
		}
	}

	/**
	 * Alternative formatter - instead of solely using the system formatter, it is more of "DATE to DATE"
	 */
	public static String formatRangeDateToDate(Context context, HotelSearchParams params, int flags) {
		CharSequence from = JodaUtils.formatLocalDate(context, params.getCheckInDate(), flags);
		CharSequence to = JodaUtils.formatLocalDate(context, params.getCheckOutDate(), flags);
		return context.getString(R.string.date_range_TEMPLATE, from, to);
	}
}
