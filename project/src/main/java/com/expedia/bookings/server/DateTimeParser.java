package com.expedia.bookings.server;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;

import android.support.annotation.Nullable;

import com.expedia.bookings.utils.JodaUtils;

public class DateTimeParser {

	public static DateTime parseISO8601DateTimeString(String iso8601DateTime) {
		DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
		return parser.parseDateTime(iso8601DateTime);
	}

	@Nullable
	public static DateTime parseDateTime(Object obj) {
		if (obj == null) {
			return null;
		}
		else if (obj instanceof JSONObject) {
			JSONObject json = (JSONObject) obj;
			long millisFromEpoch = json.optLong("epochSeconds") * 1000;
			int tzOffsetMillis = json.optInt("timeZoneOffsetSeconds") * 1000;
			return JodaUtils.fromMillisAndOffset(millisFromEpoch, tzOffsetMillis);
		}

		throw new RuntimeException("Could not parse date time: " + obj);
	}

}
