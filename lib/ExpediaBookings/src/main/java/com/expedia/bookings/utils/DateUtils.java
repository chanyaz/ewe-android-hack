package com.expedia.bookings.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

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
}
