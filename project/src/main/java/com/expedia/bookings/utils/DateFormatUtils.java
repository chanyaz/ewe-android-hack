package com.expedia.bookings.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.SearchParams;
import com.squareup.phrase.Phrase;

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
	public static final int FLAGS_DATE_NO_YEAR_ABBREV_MONTH_ABBREV_WEEKDAY =
		DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY;

	/**
	 * Formatted Date example: Apr 12
	 * <p/>
	 * When used in {@link DateFormatUtils#formatDateRange(Context, FlightSearchParams, int)} or {@link DateFormatUtils#formatDateRange(Context, HotelSearchParams, int)} returns Apr 12 - 15
	 */
	public static final int FLAGS_DATE_ABBREV_MONTH = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;

	// #9770: Add an hour of buffer so that the date range is always > the number of days
	private static final long DATE_RANGE_BUFFER = DateUtils.HOUR_IN_MILLIS;

	// Called by the formatDateRange methods below
	public static String formatDateRange(Context context, LocalDate startDate, LocalDate endDate, int flags) {
		final long start = startDate.toDateTimeAtStartOfDay().getMillis();
		final long end = endDate.toDateTimeAtStartOfDay().getMillis() + DATE_RANGE_BUFFER;
		return DateUtils.formatDateRange(context, start, end, flags);
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
		return formatDateRange(context, searchParams.getCheckInDate(), searchParams.getCheckOutDate(), flags);
	}

	public static String formatDateRange(Context context, FlightSearchParams searchParams, int flags) {
		if (searchParams.getReturnDate() != null) {
			// If it's a two-way flight, let's format the date range from departure - arrival
			return formatDateRange(context, searchParams.getDepartureDate(), searchParams.getReturnDate(), flags);
		}

		// If it's a one-way flight, let's just send the formatted departure date.
		return JodaUtils.formatLocalDate(context, searchParams.getDepartureDate(), flags);
	}

	public static String formatDateRange(Context context, SearchParams params, int flags) {
		if (params.getEndDate() != null) {
			return formatDateRange(context, params.getStartDate(), params.getEndDate(), flags);
		}

		return JodaUtils.formatLocalDate(context, params.getStartDate(), flags);
	}

	public static String formatStartEndDateTimeRange(Context context, DateTime startDateTime, DateTime endDateTime,
		boolean isContDesc) {
		String formattedStartDateTime = com.expedia.bookings.utils.DateUtils
			.dateTimeToMMMd(startDateTime) + ", " + DateUtils
			.formatDateTime(context, startDateTime.getMillis(), DateFormatUtils.FLAGS_TIME_FORMAT);
		if (endDateTime != null) {
			String formattedEndDateTime = com.expedia.bookings.utils.DateUtils.dateTimeToMMMd(
				endDateTime) + ", " + DateUtils
				.formatDateTime(context, endDateTime.getMillis(), DateFormatUtils.FLAGS_TIME_FORMAT);
			return Phrase.from(context,
				isContDesc ? R.string.car_toolbar_date_range_cont_desc_TEMPLATE
					: R.string.car_toolbar_date_range_TEMPLATE)
				.put("from_date_time", formattedStartDateTime)
				.put("to_date_time", formattedEndDateTime)
				.format().toString();
		}

		return Phrase.from(context.getResources(), R.string.select_return_date_TEMPLATE)
			.put("startdate", formattedStartDateTime)
			.format().toString();

	}

	public static String formatRailDateTimeRange(Context context, LocalDate startDate, int startMillis,
											LocalDate endDate, int endMillis, Boolean isRoundTrip) {
		DateTime startDateTime = com.expedia.bookings.utils.DateUtils.localDateAndMillisToDateTime(startDate, startMillis);
		if (isRoundTrip) {
			DateTime endDateTime = com.expedia.bookings.utils.DateUtils.localDateAndMillisToDateTime(endDate, endMillis);
			return formatStartEndDateTimeRange(context, startDateTime, endDateTime, false);
		}
		else {
			return com.expedia.bookings.utils.DateUtils.dateTimeToMMMd(startDateTime) + ", " + DateUtils
				.formatDateTime(context, startDateTime.getMillis(), DateFormatUtils.FLAGS_TIME_FORMAT);
		}
	}

	public static String formatRailDateRange(Context context, LocalDate startDate, LocalDate endDate) {
		if (endDate == null) {
			return Phrase.from(context, R.string.calendar_instructions_date_rail_one_way_TEMPLATE)
				.put("startdate", com.expedia.bookings.utils.DateUtils.localDateToMMMd(startDate)).format().toString();
		}
		else {
			return Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE)
				.put("startdate", com.expedia.bookings.utils.DateUtils.localDateToMMMd(startDate))
				.put("enddate", com.expedia.bookings.utils.DateUtils.localDateToMMMd(endDate)).format().toString();
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

	/**
	 * Convenience method for formatting date range in hotelsv2 from 2015-10-1 to Oct 01, 2015
	 */
	public static String formatHotelsV2DateRange(Context context, String checkinDate, String checkoutDate) {
		DateTimeFormatter parser = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTime checkinDateTime = parser.parseDateTime(checkinDate);
		DateTime checkoutDateTime = parser.parseDateTime(checkoutDate);

		DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM dd, yyyy");
		return Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE)
			.put("startdate", formatter.print(checkinDateTime)).put("enddate", formatter.print(checkoutDateTime))
			.format().toString();
	}

	public static String formatPackageDateRangeContDesc(Context context, String checkinDate, String checkoutDate) {
		return formatPackageDateRangeTemplate(context, checkinDate, checkoutDate,
			R.string.calendar_instructions_date_range_cont_desc_TEMPLATE);
	}

	public static String formatPackageDateRange(Context context, String checkinDate, String checkoutDate) {
		return formatPackageDateRangeTemplate(context, checkinDate, checkoutDate, R.string.calendar_instructions_date_range_TEMPLATE);
	}

	private static String formatPackageDateRangeTemplate(Context context, String checkinDate, String checkoutDate,
		int stringResID) {
		DateTimeFormatter parser = DateTimeFormat.forPattern("yyyy-MM-dd");
		String checkinDateTime = formatDateToShortDayAndDate(parser.parseDateTime(checkinDate));
		String checkoutDateTime = formatDateToShortDayAndDate(parser.parseDateTime(checkoutDate));

		return Phrase.from(context, stringResID)
			.put("startdate", checkinDateTime).put("enddate", checkoutDateTime).format().toString();
	}

	public static String formatBirthDate(Context context, int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		return JodaUtils.formatLocalDate(context, new LocalDate(year, month, day), FLAGS_MEDIUM_DATE_FORMAT);
	}

	public static String formatLocalDateToShortDayAndDate(String date) {
		DateTimeFormatter parser = DateTimeFormat.forPattern("yyyy-MM-dd");
		return formatDateToShortDayAndDate(parser.parseDateTime(date));
	}

	public static String formatLocalDateToShortDayAndDate(LocalDate localDate) {
		return formatDateToShortDayAndDate(localDate.toDateTimeAtStartOfDay());
	}

	public static String formatDateToShortDayAndDate(DateTime date) {
		SimpleDateFormat ft = new SimpleDateFormat("EEE MMM dd, yyyy", Locale.getDefault());
		return ft.format(date.toDate());
	}

	public static String formatLocalDateToShortDayAndMonth(DateTime date) {
		SimpleDateFormat ft = new SimpleDateFormat("EEE MMM dd", Locale.getDefault());
		return ft.format(date.toDate());
	}

	public static String formatLocalDateToEEEMMMdBasedOnLocale(LocalDate date) {
		String pattern = "EEE, MMM d";
		String bestDateTimePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern);
		String formattedDate;
		try {
			formattedDate = date.toString(bestDateTimePattern);
		}
		catch (Exception e) {
			formattedDate = date.toString(pattern);
		}
		return formattedDate;
	}

}
