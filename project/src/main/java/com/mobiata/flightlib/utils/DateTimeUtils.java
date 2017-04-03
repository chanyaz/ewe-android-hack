package com.mobiata.flightlib.utils;

import java.text.SimpleDateFormat;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.ReadableInstant;

import android.content.Context;
import android.content.res.Resources;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.JodaUtils;
import com.squareup.phrase.Phrase;

public class DateTimeUtils {
	public static final String FLIGHT_STATS_FORMAT = "yyyy-MM-dd'T'HH:mm";

	public static LocalDateTime parseFlightStatsDateTime(String dateTime) {
		// It turns out it's faster just to parse this by hand
		int year = Integer.parseInt(dateTime.substring(0, 4));
		int month = Integer.parseInt(dateTime.substring(5, 7));
		int day = Integer.parseInt(dateTime.substring(8, 10));
		int hour = Integer.parseInt(dateTime.substring(11, 13));
		int minute = Integer.parseInt(dateTime.substring(14, 16));

		return new LocalDateTime(year, month, day, hour, minute);
	}

	/**
	 * Returns the difference between the two times in minutes
	 *
	 * @param cal1 the first date/time
	 * @param cal2 the second date/time
	 * @return the difference between them in minutes
	 */
	public static int getMinutesBetween(ReadableInstant cal1, ReadableInstant cal2) {
		return Minutes.minutesBetween(cal1, cal2).getMinutes();
	}

	/**
	 * Formats the passed duration (given in number of minutes) as a readable String.
	 *
	 * @param r
	 * @param duration number of minutes
	 * @return the duration, or the empty string if duration <= 0
	 */
	public static String formatDuration(Resources r, int duration) {
		int minutes = Math.abs(duration % 60);
		int hours = Math.abs(duration / 60);
		if (hours > 0 && minutes > 0) {
			return r.getString(R.string.hours_minutes_template, hours, minutes);
		}
		else if (hours > 0) {
			return r.getString(R.string.hours_template, hours);
		}
		else if (minutes >= 0) {
			return r.getString(R.string.minutes_template, minutes);
		}
		else {
			return "";
		}
	}

	/**
	 * Formats the passed duration (given in number of minutes) as a readable String.
	 *
	 * @param r
	 * @param millis number of minutes
	 * @return Total Duration: 1 hr 2 min or Total Duration: 1 hr or Total Duration: 1 min
	 */
	public static String formatTotalDuration(Resources r, long millis) {
		Duration duration = new Duration(millis);
		int minutes = (int) duration.getStandardMinutes() % DateTimeConstants.MINUTES_PER_HOUR;
		int hours = (int) duration.getStandardHours();
		if (hours > 0 && minutes > 0) {
			return Phrase.from(r.getString(R.string.full_hours_minutes_template))
				.put("hour", hours)
				.put("min", minutes)
				.format()
				.toString();
		}
		else if (hours > 0) {
			return Phrase.from(r.getString(R.string.full_hours_template))
				.put("hour", hours)
				.format()
				.toString();
		}
		else if (minutes >= 0) {
			return Phrase.from(r.getString(R.string.full_minutes_template))
				.put("min", minutes)
				.format()
				.toString();
		}
		else {
			return "";
		}
	}

	public static String getDeviceTimeFormat(Context context) {
		return ((SimpleDateFormat) android.text.format.DateFormat.getTimeFormat(context)).toPattern();
	}

	public static CharSequence formatInterval(@NotNull Context context, @NotNull DateTime start,
		@NotNull DateTime end) {

		String dateFormat = DateTimeUtils.getDeviceTimeFormat(context);
		String formattedStart = JodaUtils.format(start, dateFormat);
		String formattedEnd = JodaUtils.format(end, dateFormat);
		return context.getString(R.string.date_time_range_TEMPLATE, formattedStart, formattedEnd);
	}
}
