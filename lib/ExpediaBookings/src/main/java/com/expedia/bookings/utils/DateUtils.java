package com.expedia.bookings.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

public class DateUtils {

	private static final String LX_DATE_PATTERN = "yyyy-MM-dd";
	/**
	 * Formats the given date in yyyyMMDD format and returns as int
	 */
	public static int convertDatetoInt(LocalDate date) {
		int dateInteger = 0;
		if (date != null) {
			String year = "" + date.getYear();
			String month = "";
			String day = "";
			if (date.getMonthOfYear() < 10) {
				month = "0" + date.getMonthOfYear();
			}
			else {
				month = "" + date.getMonthOfYear();
			}
			if (date.getDayOfMonth() < 10) {
				day = "0" + date.getDayOfMonth();
			}
			else {
				day = "" + date.getDayOfMonth();
			}
			String dateString = year + month + day;
			dateInteger = Integer.valueOf(dateString);
		}
		return dateInteger;
	}

	/**
	 * Return YYYY-mm-ddThh:mm:ss format for a given DateTime
	 */
	public static String carSearchFormatFromDateTime(DateTime d) {
		DateTimeFormatter dateFmt = ISODateTimeFormat.date();
		DateTimeFormatter timeFmt = ISODateTimeFormat.hourMinuteSecond();

		return dateFmt.print(d) + "T" + timeFmt.print(d);
	}

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

	public static String convertToLXDate(LocalDate date) {
		return date != null ? date.toString(LX_DATE_PATTERN) : null;
	}

	public static String localDateToMMMd(LocalDate date) {
		return date.toString("MMM d");
	}

	public static String localDateToMMMMd(LocalDate date) {
		return date.toString("MMMM d");
	}

	public static String localDateToEEEMMMd(LocalDate date) {
		return date.toString("EEE, MMM d");
	}

	public static String localDateToyyyyMMdd(LocalDate date) {
		return date.toString("yyyy-MM-dd");
	}

	public static String localDateToMMddyyyy(LocalDate date) {
		return date.toString("MM/dd/yyy");
	}

	public static String localDateTohmma(DateTime date) {
		return date.toString("h:mm a").toLowerCase();
	}

	public static String dateTimeToMMMdhmma(DateTime date) {
		return date.toString("MMM d, h:mm a");
	}

	public static String dateTimeToMMMdhmma(LocalDate date) {
		return date.toString("MMM d, h:mm a");
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

	public static LocalDate yyyyMMddHHmmssToLocalDate(String dateyyyyMMddHHmmss) {
		return LocalDate.parse(dateyyyyMMddHHmmss, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public static DateTime yyyyMMddHHmmssToDateTime(String dateyyyyMMddHHmmss) {
		return DateTime.parse(dateyyyyMMddHHmmss, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public static DateTime yyyyMMddHHmmToDateTime(String dateyyyyMMddHHmm) {
		return DateTime.parse(dateyyyyMMddHHmm, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"));
	}

	public static DateTime dateyyyyMMddHHmmSSSZToDateTime(String dateyyyyMMddHHmmSSSZ) {
		return DateTime.parse(dateyyyyMMddHHmmSSSZ, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
	}

	public static DateTime dateyyyyMMddHHmmSSSZToDateTimeWithTimeZone(String dateyyyyMMddHHmmSSSZ) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withOffsetParsed();
		return DateTime.parse(dateyyyyMMddHHmmSSSZ, dateTimeFormatter);
	}

	public static DateTime yyyyMMddTHHmmssToDateTimeSafe(String dateyyyyMMddTHHmmss, DateTime defaultValue) {
		try {
			return DateTime.parse(dateyyyyMMddTHHmmss, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss"));
		}
		catch (Exception e) {
			return defaultValue;
		}
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
	 * Converts from format 2014-07-05T12:30:00.000-05:00 to "12:30 pm"
	 */
	public static String formatTimeShort(String timeStr) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withOffsetParsed();
		DateTime time = DateTime.parse(timeStr, fmt);
		return localDateTohmma(time);
	}

	/**
	 * Parses minutes out of the duration string in ISO format: P[yY][mM][dD][T[hH][mM][s[.s]S]]
	 */
	public static int parseDurationMinutes(String durationString) {
		PeriodFormatter formatter = ISOPeriodFormat
			.standard();
		Period p = formatter.parsePeriod(durationString);
		Minutes m = p.toStandardMinutes();
		return m.getMinutes();
	}
}
