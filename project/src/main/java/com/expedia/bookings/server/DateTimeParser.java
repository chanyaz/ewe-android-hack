package com.expedia.bookings.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.json.JSONObject;

import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.StrUtils;

public class DateTimeParser {

	// Until all date formats are normalized, we must support all of them.
	private static final DateFormat[] DATE_FORMATS = {
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"),
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
	};

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
		else if (obj instanceof String) {
			// TODO: DELETE ONCE OBSELETE
			String str = (String) obj;

			for (DateFormat df : DATE_FORMATS) {
				try {
					Date date = df.parse(str);

					// We are going to do this hacky way of parsing the timezone for fun and profit
					String sign = StrUtils.slice(str, -6, -5);
					String hourStr = StrUtils.slice(str, -5, -3);
					String minuteStr = StrUtils.slice(str, -2);
					int offset = (60 * 60 * Integer.parseInt(hourStr)) + (60 * Integer.parseInt(minuteStr));
					if (sign.equals("-")) {
						offset *= -1;
					}

					return JodaUtils.fromMillisAndOffset(date.getTime(), offset * 1000);
				}
				catch (ParseException e) {
					// Ignore
				}
			}
		}

		throw new RuntimeException("Could not parse date time: " + obj);
	}

}
