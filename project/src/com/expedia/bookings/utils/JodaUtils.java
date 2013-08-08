package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.base.AbstractPartial;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.data.Date;
import com.mobiata.android.json.JSONUtils;

public class JodaUtils {

	public static DateTime fromMillisAndOffset(long millisFromEpoch, int tzOffsetMillis) {
		return new org.joda.time.DateTime(millisFromEpoch, DateTimeZone.forOffsetMillis(tzOffsetMillis));
	}

	public static boolean isAfterOrEquals(AbstractPartial first, AbstractPartial second) {
		return first.isAfter(second) || first.isEqual(second);
	}

	public static boolean isBeforeOrEquals(AbstractPartial first, AbstractPartial second) {
		return first.isBefore(second) || first.isEqual(second);
	}

	public static int daysBetween(ReadablePartial start, ReadablePartial end) {
		return Days.daysBetween(start, end).getDays();
	}

	public static int daysBetween(ReadableInstant start, ReadableInstant end) {
		return Days.daysBetween(start, end).getDays();
	}

	public static String formatTimeZone(DateTime dateTime) {
		return formatTimeZone(dateTime.getZone().getOffset(dateTime));
	}

	public static String formatTimeZone(int tzOffsetMillis) {
		int offsetSeconds = tzOffsetMillis / 1000;
		int offsetMinutes = Math.abs(offsetSeconds / 60);
		int offsetHours = offsetMinutes / 60;
		offsetMinutes -= (offsetHours * 60);
		String timeZoneString = "GMT";
		if (offsetHours > 0 || offsetMinutes > 0) {
			timeZoneString += ((offsetSeconds > 0) ? "+" : "-") + offsetHours;
			if (offsetMinutes > 0) {
				timeZoneString += ":" + ((offsetMinutes < 10) ? "0" : "") + offsetMinutes;
			}
		}

		return timeZoneString;
	}

	//////////////////////////////////////////////////////////////////////////
	// Formatting

	/**
	 * This is the equivalent of android.text.format.getDateFormat(), when used in DateUtils
	 */
	public static final int FLAGS_DATE_FORMAT = DateUtils.FORMAT_NUMERIC_DATE;

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

	public static String formatLocalDate(Context context, LocalDate localDate, int flags) {
		return formatDateTime(context, localDate.toDateTimeAtStartOfDay(), flags);
	}

	public static String formatDateTime(Context context, DateTime dateTime, int flags) {
		DateTime utcDateTime = dateTime.withZoneRetainFields(DateTimeZone.UTC);
		return DateUtils.formatDateTime(context, utcDateTime.getMillis(), flags | DateUtils.FORMAT_UTC);
	}

	//////////////////////////////////////////////////////////////////////////
	// JSON

	public static void putLocalDateInJson(JSONObject obj, String key, LocalDate localDate) throws JSONException {
		if (obj != null && !TextUtils.isEmpty(key) && localDate != null) {
			obj.put(key, localDate.toString());
		}
	}

	public static LocalDate getLocalDateFromJsonBackCompat(JSONObject obj, String localDateKey, String oldDateKey) {
		if (obj.has(oldDateKey)) {
			Date date = JSONUtils.getJSONable(obj, oldDateKey, Date.class);
			return Date.toLocalDate(date);
		}
		else if (obj.has(localDateKey)) {
			return LocalDate.parse(obj.optString(localDateKey));
		}

		return null;
	}

	public static void putDateTimeInJson(JSONObject obj, String key, DateTime dateTime) throws JSONException {
		if (obj != null && !TextUtils.isEmpty(key) && dateTime != null) {
			obj.put(key, dateTime.toString());
		}
	}

	public static DateTime getDateTimeFromJsonBackCompat(JSONObject obj, String dateTimeKey, String oldDateTimeKey) {
		if (obj.has(oldDateTimeKey)) {
			com.expedia.bookings.data.DateTime dateTime = JSONUtils.getJSONable(obj, oldDateTimeKey,
					com.expedia.bookings.data.DateTime.class);
			return com.expedia.bookings.data.DateTime.toJodaDateTime(dateTime);
		}
		else if (obj.has(dateTimeKey)) {
			return DateTime.parse(obj.optString(dateTimeKey));
		}

		return null;
	}

	public static void putDateTimeListInJson(JSONObject obj, String key, List<DateTime> dateTimes) throws JSONException {
		if (obj != null && !TextUtils.isEmpty(key) && dateTimes != null) {
			JSONArray arr = new JSONArray();
			for (DateTime dateTime : dateTimes) {
				arr.put(dateTime.toString());
			}
			obj.put(key, arr);
		}
	}

	public static List<DateTime> getDateTimeListFromJsonBackCompat(JSONObject obj, String listKey, String oldListKey) {
		if (obj.has(oldListKey)) {
			List<com.expedia.bookings.data.DateTime> oldDateTimes = JSONUtils.getJSONableList(obj, oldListKey,
					com.expedia.bookings.data.DateTime.class);
			if (oldDateTimes != null) {
				List<DateTime> dateTimes = new ArrayList<DateTime>();
				for (com.expedia.bookings.data.DateTime oldDateTime : oldDateTimes) {
					dateTimes.add(com.expedia.bookings.data.DateTime.toJodaDateTime(oldDateTime));
				}
				return dateTimes;
			}
		}
		else if (obj.has(listKey)) {
			JSONArray arr = obj.optJSONArray(listKey);
			List<DateTime> dateTimes = new ArrayList<DateTime>();
			int len = arr.length();
			for (int a = 0; a < len; a++) {
				dateTimes.add(DateTime.parse(arr.optString(a)));
			}
			return dateTimes;
		}

		return null;
	}
}
