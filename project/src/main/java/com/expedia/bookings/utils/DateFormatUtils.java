package com.expedia.bookings.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
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
	 * Formatted Date example: Apr 12
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


	private static String formatStartEndDateTimeRange(Context context, DateTime startDateTime, DateTime endDateTime,
		boolean isContDesc) {
		String formattedStartDateTime = LocaleBasedDateFormatUtils.dateTimeToMMMd(startDateTime) + ", " + DateUtils
			.formatDateTime(context, startDateTime.getMillis(), DateFormatUtils.FLAGS_TIME_FORMAT);
		if (endDateTime != null) {
			String formattedEndDateTime = LocaleBasedDateFormatUtils.dateTimeToMMMd(endDateTime) + ", " + DateUtils
				.formatDateTime(context, endDateTime.getMillis(), DateFormatUtils.FLAGS_TIME_FORMAT);
			return Phrase.from(context,
				isContDesc ? R.string.date_time_range_cont_desc_TEMPLATE
					: R.string.date_time_range_TEMPLATE)
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
		DateTime startDateTime = com.expedia.bookings.utils.DateUtils
			.localDateAndMillisToDateTime(startDate, startMillis);
		if (isRoundTrip) {
			DateTime endDateTime = com.expedia.bookings.utils.DateUtils
				.localDateAndMillisToDateTime(endDate, endMillis);
			return formatStartEndDateTimeRange(context, startDateTime, endDateTime, false);
		}
		else {
			return LocaleBasedDateFormatUtils.dateTimeToMMMd(startDateTime) + ", " + DateUtils
				.formatDateTime(context, startDateTime.getMillis(), DateFormatUtils.FLAGS_TIME_FORMAT);
		}
	}

	public static String formatRailDateRange(Context context, LocalDate startDate, LocalDate endDate) {
		if (endDate == null) {
			return Phrase.from(context, R.string.calendar_instructions_date_rail_one_way_TEMPLATE)
				.put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(startDate)).format().toString();
		}
		else {
			return Phrase.from(context, R.string.start_dash_end_date_range_TEMPLATE)
				.put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(startDate))
				.put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(endDate)).format().toString();
		}
	}

	/**
	 * Convenience method for formatting date range in hotelsv2 from 2015-10-1 to Oct 01, 2015
	 */
	public static String formatHotelsV2DateRange(Context context, String checkinDate, String checkoutDate) {
		DateTimeFormatter parser = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTime checkinDateTime = parser.parseDateTime(checkinDate);
		DateTime checkoutDateTime = parser.parseDateTime(checkoutDate);

		DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM dd, yyyy");
		return Phrase.from(context, R.string.start_dash_end_date_range_TEMPLATE)
			.put("startdate", formatter.print(checkinDateTime)).put("enddate", formatter.print(checkoutDateTime))
			.format().toString();
	}

	public static String formatPackageDateRangeContDesc(Context context, String checkinDate, String checkoutDate) {
		return formatPackageDateRangeTemplate(context, checkinDate, checkoutDate,
			R.string.start_to_end_date_range_cont_desc_TEMPLATE);
	}

	public static String formatPackageDateRange(Context context, String checkinDate, String checkoutDate) {
		return formatPackageDateRangeTemplate(context, checkinDate, checkoutDate,
			R.string.start_dash_end_date_range_TEMPLATE);
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
		return LocaleBasedDateFormatUtils.localDateToEEEMMMd(date);
	}
}
