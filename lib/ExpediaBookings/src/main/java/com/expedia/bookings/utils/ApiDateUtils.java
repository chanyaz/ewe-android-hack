package com.expedia.bookings.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

/**
 * Provides methods for formatting dates to/from the API.
 */
public class ApiDateUtils {

	/**
	 * Return YYYY-MM-ddThh:mm:ss format for a given DateTime
	 */
	public static String toYYYYMMTddhhmmss(DateTime d) {
		DateTimeFormatter dateFmt = ISODateTimeFormat.date();
		DateTimeFormatter timeFmt = ISODateTimeFormat.hourMinuteSecond();

		return dateFmt.print(d) + "T" + timeFmt.print(d);
	}

	/**
	 * Formats the milliseconds in date format and return as string
	 */
	public static String convertMilliSecondsForLogging(Long timeinMilliSeconds) {
		final TimeZone utc = TimeZone.getTimeZone("Etc/GMT");

		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.US);
		format.setTimeZone(utc);

		Calendar calendar = new GregorianCalendar();
		calendar.setTimeZone(utc);
		calendar.setTimeInMillis(timeinMilliSeconds);

		return format.format(calendar.getTime());
	}

	public static String localDateToyyyyMMddSafe(LocalDate date) {
		return date != null ? date.toString("yyyy-MM-dd") : null;
	}

	public static String localDateToyyyyMMdd(LocalDate date) {
		return date != null ? date.toString("yyyy-MM-dd") : null;
	}

	public static LocalDate yyyyMMddToLocalDate(String dateyyyyMMdd) {
		return LocalDate.parse(dateyyyyMMdd, DateTimeFormat.forPattern("yyyy-MM-dd"));
	}

	public static LocalDate yyyyMMddToLocalDateSafe(String dateyyyyMMdd, LocalDate defaultValue) {
		try {
			return LocalDate.parse(dateyyyyMMdd, DateTimeFormat.forPattern("yyyy-MM-dd"));
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static Date yyyyMMddHHmmssToDate(String dateyyyyMMddHHmmss) {
		Date date = new Date();
		DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		date.setTime(format.parseMillis(dateyyyyMMddHHmmss));
		return date;
	}

	public static LocalDate yyyyMMddHHmmssToLocalDate(String dateyyyyMMddHHmmss) {
		return LocalDate.parse(dateyyyyMMddHHmmss, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public static DateTime yyyyMMddHHmmssToDateTime(String dateyyyyMMddHHmmss) {
		return DateTime.parse(dateyyyyMMddHHmmss, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public static DateTime yyyyMMddHHmmToDateTime(String dateyyyyMMddHHmm) {
		return DateTime.parse(dateyyyyMMddHHmm, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"));
	}

	public static DateTime dateyyyyMMddHHmmSSSZToDateTimeWithTimeZone(String dateyyyyMMddHHmmSSSZ) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withOffsetParsed();
		return DateTime.parse(dateyyyyMMddHHmmSSSZ, dateTimeFormatter);
	}

	public static DateTime localDateAndMillisToDateTime(LocalDate date, int millis) {
		DateTime convertedDateTime = new DateTime();
		return convertedDateTime.withYear(date.getYear())
			.withMonthOfYear(date.getMonthOfYear())
			.withDayOfMonth(date.getDayOfMonth())
			.withTimeAtStartOfDay()
			.plusMillis(millis);
	}

	public static LocalDate ensureDateIsTodayOrInFuture(LocalDate date) {
		LocalDate today = new LocalDate();
		return date.isBefore(today) ? today : date;
	}

	/**
	 * Parses minutes out of the duration string in ISO format: P[yY][mM][dD][T[hH][mM][s[.s]S]]
	 */
	public static int parseDurationMinutesFromISOFormat(String iSODurationString) {
		PeriodFormatter formatter = ISOPeriodFormat
			.standard();
		Period p = formatter.parsePeriod(iSODurationString);
		Minutes m = p.toStandardMinutes();
		return m.getMinutes();
	}

	/**
	 * Parse long millis into HH:mm:ss format
	 */
	public static String formatMillisToHHmmss(LocalDate date, int millis) {
		DateTime dateTime = localDateAndMillisToDateTime(date, millis);
		return dateTime.toString("HH:mm:ss");
	}

	/**
	 * Parse the duration string in MM/dd/yyyy
	 */
	@SuppressWarnings("SimpleDateFormat")
	public static String toMMddyyyy(String dateTimeString) throws ParseException {
		Date date = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.US).parse(dateTimeString);
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		return formatter.format(date);
	}
}
