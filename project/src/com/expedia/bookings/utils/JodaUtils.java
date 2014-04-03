package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.base.AbstractPartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.mobiata.android.json.JSONUtils;

public class JodaUtils extends com.mobiata.android.time.util.JodaUtils {

	public static DateTime fromMillisAndOffset(long millisFromEpoch, int tzOffsetMillis) {
		return new org.joda.time.DateTime(millisFromEpoch, DateTimeZone.forOffsetMillis(tzOffsetMillis));
	}

	public static boolean isAfterOrEquals(AbstractPartial first, AbstractPartial second) {
		return first.isAfter(second) || first.isEqual(second);
	}

	public static boolean isBeforeOrEquals(AbstractPartial first, AbstractPartial second) {
		return first.isBefore(second) || first.isEqual(second);
	}

	/**
	 * Checks if a timestamp has expired, given a particular cutoff
	 * 
	 * Returns true if:
	 * - "Now" is before the timestamp (which should be impossible, since the timestamp should at least be == now)
	 * - The end of the valid range (timestamp + cutoff) is still before now.
	 * 
	 * Returns false if:
	 * - Timestamp is null (this means it's not even valid yet for testing)
	 */
	public static boolean isExpired(DateTime timestamp, long cutoff) {
		if (timestamp == null) {
			return false;
		}

		DateTime now = DateTime.now();
		return now.isBefore(timestamp) || timestamp.plusMillis((int) cutoff).isBefore(now);
	}

	public static String formatTimeZone(DateTime dateTime) {
		return dateTime.getZone().getShortName(dateTime.getMillis());
	}

	public static String formatDateRange(Context context, LocalDate start, LocalDate end, int flags) {
		return formatDateRange(context, start.toDateTimeAtStartOfDay(), end.toDateTimeAtStartOfDay(), flags);
	}

	public static String formatDateRange(Context context, DateTime start, DateTime end, int flags) {
		return DateUtils.formatDateRange(context, start.getMillis(), end.getMillis() + 1000, flags
				| DateUtils.FORMAT_UTC);
	}

	/**
	 * This method is mostly ripped from DateUtils.getRelativeTimeSpanString(), and
	 * works in much the same way.  The major difference is that it doesn't suck
	 * and actually calculates everything correctly, using durations (instead of
	 * millisecond math, which is prone to be off if you're comparing two different
	 * timezones).
	 */
	public static CharSequence getRelativeTimeSpanString(Context context, DateTime time, DateTime now,
			long minResolution, int flags) {
		Resources r = context.getResources();
		boolean abbrevRelative = (flags & (DateUtils.FORMAT_ABBREV_RELATIVE | DateUtils.FORMAT_ABBREV_ALL)) != 0;

		boolean past = now.isAfter(time);
		Duration duration = past ? new Duration(time, now) : new Duration(now, time);

		int resId;
		long count;
		if (duration.isShorterThan(Duration.standardMinutes(1)) && minResolution < DateUtils.MINUTE_IN_MILLIS) {
			count = duration.getStandardMinutes();
			if (past) {
				if (abbrevRelative) {
					resId = R.plurals.abbrev_num_seconds_ago;
				}
				else {
					resId = R.plurals.num_seconds_ago;
				}
			}
			else {
				if (abbrevRelative) {
					resId = R.plurals.abbrev_in_num_seconds;
				}
				else {
					resId = R.plurals.in_num_seconds;
				}
			}
		}
		else if (duration.isShorterThan(Duration.standardHours(1)) && minResolution < DateUtils.HOUR_IN_MILLIS) {
			count = duration.getStandardMinutes();
			if (past) {
				if (abbrevRelative) {
					resId = R.plurals.abbrev_num_minutes_ago;
				}
				else {
					resId = R.plurals.num_minutes_ago;
				}
			}
			else {
				if (abbrevRelative) {
					resId = R.plurals.abbrev_in_num_minutes;
				}
				else {
					resId = R.plurals.in_num_minutes;
				}
			}
		}
		else if (duration.isShorterThan(Duration.standardDays(1)) && minResolution < DateUtils.DAY_IN_MILLIS) {
			count = duration.getStandardHours();
			if (past) {
				if (abbrevRelative) {
					resId = R.plurals.abbrev_num_hours_ago;
				}
				else {
					resId = R.plurals.num_hours_ago;
				}
			}
			else {
				if (abbrevRelative) {
					resId = R.plurals.abbrev_in_num_hours;
				}
				else {
					resId = R.plurals.in_num_hours;
				}
			}
		}
		else if (duration.isShorterThan(Duration.standardDays(7)) && minResolution < DateUtils.WEEK_IN_MILLIS) {
			count = Math.abs(daysBetween(time, now.withZone(time.getZone())));
			if (past) {
				if (abbrevRelative) {
					resId = R.plurals.abbrev_num_days_ago;
				}
				else {
					resId = R.plurals.num_days_ago;
				}
			}
			else {
				if (abbrevRelative) {
					resId = R.plurals.abbrev_in_num_days;
				}
				else {
					resId = R.plurals.in_num_days;
				}
			}
		}
		else {
			// We know that we won't be showing the time, so it is safe to pass
			// in a null context.
			return DateUtils.formatDateRange(null, time.getMillis(), time.getMillis(), flags);
		}

		String format = r.getQuantityString(resId, (int) count);
		return String.format(format, count);
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

	/**
	 * Note: you should first check ISODateTimeFormat for the correct format
	 * before using this.  Also, if you need to repeatedly format anything,
	 * create your own local formatter first.
	 */
	public static String format(ReadableInstant instant, String pattern) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		return fmt.print(instant);
	}

	public static String format(ReadablePartial partial, String pattern) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		return fmt.print(partial);
	}

	//////////////////////////////////////////////////////////////////////////
	// JSON

	public static void putDateTime(Bundle bundle, String key, DateTime dateTime) {
		if (bundle != null && !TextUtils.isEmpty(key) && dateTime != null) {
			bundle.putString(key, dateTime.toString());
		}
	}

	public static DateTime getDateTime(Bundle bundle, String key) {
		if (bundle != null && bundle.containsKey(key)) {
			return DateTime.parse(bundle.getString(key));
		}
		return null;
	}

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
