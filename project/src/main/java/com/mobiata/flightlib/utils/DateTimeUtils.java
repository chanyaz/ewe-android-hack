package com.mobiata.flightlib.utils;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.ReadableInstant;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.JodaUtils;
import com.squareup.phrase.Phrase;

import java.text.SimpleDateFormat;

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
	 * @param durationMins number of minutes
	 * @return the duration, or the empty string if duration <= 0
	 */
	public static String formatDuration(Resources r, int durationMins) {
		int minutes = Math.abs(durationMins % 60);
		int hours = Math.abs(durationMins / 60);
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

	@NonNull
	public static String formatDurationDaysHoursMinutes(Context context, int durationMins) {
		if (durationMins < 0) {
			return "";
		}
		int minutes = Math.abs(durationMins % 60);
		int hours = Math.abs(durationMins / 60 % 24);
		int days = Math.abs(durationMins / 24 / 60);
		if (days > 0 && hours > 0 && minutes > 0) {
			return Phrase.from(context, R.string.flight_duration_days_hours_minutes_TEMPLATE)
				.put("days", days)
				.put("hours", hours)
				.put("minutes", minutes).format().toString();
		}
		else if (days > 0 && hours > 0) {
			return Phrase.from(context, R.string.flight_duration_days_hours_TEMPLATE)
				.put("days", days)
				.put("hours", hours).format().toString();
		}
		else if (days > 0 && minutes > 0) {
			return Phrase.from(context, R.string.flight_duration_days_minutes_TEMPLATE)
				.put("days", days)
				.put("minutes", minutes).format().toString();
		}
		else if (days > 0) {
			return Phrase.from(context, R.string.flight_duration_days_TEMPLATE)
				.put("days", days).format().toString();
		}
		else if (hours > 0 && minutes > 0) {
			return Phrase.from(context, R.string.flight_duration_hours_minutes_TEMPLATE)
				.put("hours", hours)
				.put("minutes", minutes).format().toString();
		}
		else if (hours > 0) {
			return Phrase.from(context, R.string.flight_duration_hours_TEMPLATE)
				.put("hours", hours).format().toString();
		}
		else if (minutes > 0) {
			return Phrase.from(context, R.string.flight_duration_minutes_TEMPLATE)
				.put("minutes", minutes).format().toString();
		}
		else {
			return "";
		}
	}

	@Nullable
	public static String getDurationContDescDaysHoursMins(Context context, int durationMinutes) {
		if (durationMinutes <= 0) {
			return null;
		}
		int minutes = Math.abs(durationMinutes % 60);
		int hours = Math.abs(durationMinutes / 60 % 24);
		int days = Math.abs(durationMinutes / 24 / 60);
		String contDesc = "";
		if (days > 0 && hours > 0 && minutes > 0) {
			contDesc = Phrase.from(context, R.string.flight_duration_days_hours_minutes_cont_desc_TEMPLATE)
				.put("days", days)
				.put("hours", hours)
				.put("minutes", minutes).format().toString();
		}
		else if (days > 0 && hours > 0) {
			contDesc = Phrase.from(context, R.string.flight_duration_days_hours_cont_desc_TEMPLATE)
				.put("days", days)
				.put("hours", hours).format().toString();
		}
		else if (days > 0 && minutes > 0) {
			contDesc = Phrase.from(context, R.string.flight_duration_days_minutes_cont_desc_TEMPLATE)
				.put("days", days)
				.put("minutes", minutes).format().toString();
		}
		else if (days > 0) {
			contDesc = Phrase.from(context, R.string.flight_duration_days_cont_desc_TEMPLATE)
				.put("days", days).format().toString();
		}
		else if (hours > 0 && minutes > 0) {
			contDesc = Phrase.from(context, R.string.flight_duration_hours_minutes_cont_desc_TEMPLATE)
				.put("hours", hours)
				.put("minutes", minutes).format().toString();
		}
		else if (hours > 0) {
			contDesc = Phrase.from(context, R.string.flight_duration_hours_cont_desc_TEMPLATE)
				.put("hours", hours).format().toString();
		}
		else if (minutes > 0) {
			contDesc = Phrase.from(context, R.string.flight_duration_minutes_cont_desc_TEMPLATE)
				.put("minutes", minutes).format().toString();
		}
		return contDesc;
	}

	public static String getDeviceTimeFormat(Context context) {
		return ((SimpleDateFormat) android.text.format.DateFormat.getTimeFormat(context)).toPattern();
	}

	public static CharSequence formatInterval(@NotNull Context context, @NotNull DateTime start,
		@NotNull DateTime end) {

		String dateFormat = DateTimeUtils.getDeviceTimeFormat(context);
		String formattedStart = JodaUtils.format(start, dateFormat);
		String formattedEnd = JodaUtils.format(end, dateFormat);
		return Phrase.from(context, R.string.date_time_range_TEMPLATE)
			.put("from_date_time", formattedStart)
			.put("to_date_time", formattedEnd)
			.format().toString();
	}
}
