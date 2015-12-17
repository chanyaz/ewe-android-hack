package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.base.AbstractInstant;
import org.joda.time.base.AbstractPartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

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

	public static boolean isBeforeOrEquals(AbstractInstant first, AbstractInstant second) {
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

	//////////////////////////////////////////////////////////////////////////
	// Formatting

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
		if (bundle != null && Strings.isNotEmpty(key) && dateTime != null) {
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
		if (obj != null && Strings.isNotEmpty(key) && localDate != null) {
			obj.put(key, localDate.toString());
		}
	}

	public static LocalDate getLocalDateFromJson(JSONObject obj, String localDateKey) {
		if (obj.has(localDateKey)) {
			return LocalDate.parse(obj.optString(localDateKey));
		}

		return null;
	}

	public static void putDateTimeInJson(JSONObject obj, String key, DateTime dateTime) throws JSONException {
		if (obj != null && Strings.isNotEmpty(key) && dateTime != null) {
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
		if (obj != null && Strings.isNotEmpty(key) && dateTimes != null) {
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
