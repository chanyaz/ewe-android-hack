package com.expedia.bookings.utils;

import org.joda.time.LocalDate;
import org.joda.time.base.AbstractPartial;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Date;
import com.mobiata.android.json.JSONUtils;

import android.text.TextUtils;

public class JodaUtils {

	public static boolean isAfterOrEquals(AbstractPartial first, AbstractPartial second) {
		return first.isAfter(second) || first.isEqual(second);
	}

	public static boolean isBeforeOrEquals(AbstractPartial first, AbstractPartial second) {
		return first.isBefore(second) || first.isEqual(second);
	}

	public static void putLocalDateInJson(JSONObject obj, String key, LocalDate localDate) throws JSONException {
		if (obj != null && !TextUtils.isEmpty(key) && localDate != null) {
			obj.put(key, localDate.toString());
		}
	}

	public static LocalDate getLocalDateFromJson(JSONObject obj, String key) {
		if (obj != null && obj.has(key)) {
			return LocalDate.parse(obj.optString(key));
		}
		return null;
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
}
