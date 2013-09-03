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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.data.Date;
import com.mobiata.android.json.JSONUtils;

public class JodaUtils {

	private static DateTimeZone sThenTimeZone = null;

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

	/**
	 * @return # of days between, positive if start is before end, negative if end is before start
	 */
	public static int daysBetween(ReadablePartial start, ReadablePartial end) {
		return Days.daysBetween(start, end).getDays();
	}

	/**
	 * @return # of days between, positive if start is before end, negative if end is before start
	 */
	public static int daysBetween(DateTime start, DateTime end) {
		return daysBetween(start.toLocalDate(), end.toLocalDate());
	}

	public static String formatTimeZone(DateTime dateTime) {
		return dateTime.getZone().getShortName(dateTime.getMillis());
	}

	/**
	 * Use this in place of DateUtils.getRelativeTimeSpanString() to handle any
	 * weirdnesses with timezones.
	 * 
	 * The way that DateUtils.getRelativeTimeSpanString() works is as follows (when
	 * comparing # of days passed):
	 * 
	 * 1. Create a Time in the current Timezone (mistake!)
	 * 2. Set the millis for each, and get the Julian day
	 * 3. Compare julian days 
	 * 
	 * This works great...  if you remember to use the correct timezone for the millis passed
	 * in (the current system's timezone) AND you make sure to cache the original system
	 * timezone (in case the user switches their timezone while your app is still in memory).
	 * 
	 * This should smooth over that problem by caching the time zone and casting it to the right
	 * one for you.
	 */
	public static CharSequence getRelativeTimeSpanString(DateTime time, DateTime now, long minResolution, int flags) {
		// getRelativeTimeSpanString() was changed in API 18 such that it *doesn't*
		// cache the timezone info anymore, so we should always use the local timezone
		if (Build.VERSION.SDK_INT >= 18 || sThenTimeZone == null) {
			sThenTimeZone = DateTimeZone.getDefault();
		}

		return DateUtils.getRelativeTimeSpanString(time.withZoneRetainFields(sThenTimeZone).getMillis(),
				now.withZoneRetainFields(sThenTimeZone).getMillis(), minResolution, flags);
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

	//////////////////////////////////////////////////////////////////////////
	// Parcelable

	public static void writeLocalDate(Parcel parcel, LocalDate localDate) {
		if (localDate == null) {
			parcel.writeString(null);
		}
		else {
			parcel.writeString(localDate.toString());
		}
	}

	public static LocalDate readLocalDate(Parcel parcel) {
		String str = parcel.readString();
		if (TextUtils.isEmpty(str)) {
			return null;
		}
		return LocalDate.parse(str);
	}

	public static void writeDateTime(Parcel parcel, DateTime dateTime) {
		if (dateTime == null) {
			parcel.writeString(null);
		}
		else {
			parcel.writeString(dateTime.toString());
		}
	}

	public static DateTime readDateTime(Parcel parcel) {
		String str = parcel.readString();
		if (TextUtils.isEmpty(str)) {
			return null;
		}
		return DateTime.parse(str);
	}

}
