package com.mobiata.android.time.util;

import java.util.Calendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePartial;
import org.joda.time.base.AbstractPartial;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.mobiata.android.time.R;

public class JodaUtils {

	/**
	 * @return the JDK month constant for the given Joda month constant
	 */
	public static int getJDKMonth(int jodaMonth) {
		switch (jodaMonth) {
		case DateTimeConstants.JANUARY:
			return Calendar.JANUARY;
		case DateTimeConstants.FEBRUARY:
			return Calendar.FEBRUARY;
		case DateTimeConstants.MARCH:
			return Calendar.MARCH;
		case DateTimeConstants.APRIL:
			return Calendar.APRIL;
		case DateTimeConstants.MAY:
			return Calendar.MAY;
		case DateTimeConstants.JUNE:
			return Calendar.JUNE;
		case DateTimeConstants.JULY:
			return Calendar.JULY;
		case DateTimeConstants.AUGUST:
			return Calendar.AUGUST;
		case DateTimeConstants.SEPTEMBER:
			return Calendar.SEPTEMBER;
		case DateTimeConstants.OCTOBER:
			return Calendar.OCTOBER;
		case DateTimeConstants.NOVEMBER:
			return Calendar.NOVEMBER;
		case DateTimeConstants.DECEMBER:
			return Calendar.DECEMBER;
		}

		// Should not be possible, just give up and return JANUARY
		return Calendar.JANUARY;
	}

	/**
	 * @return the constant representing the first day of the week, according to Joda Time
	 */
	public static int getFirstDayOfWeek() {
		switch (Calendar.getInstance().getFirstDayOfWeek()) {
		case Calendar.SUNDAY:
			return DateTimeConstants.SUNDAY;
		case Calendar.MONDAY:
			return DateTimeConstants.MONDAY;
		case Calendar.TUESDAY:
			return DateTimeConstants.TUESDAY;
		case Calendar.WEDNESDAY:
			return DateTimeConstants.WEDNESDAY;
		case Calendar.THURSDAY:
			return DateTimeConstants.THURSDAY;
		case Calendar.FRIDAY:
			return DateTimeConstants.FRIDAY;
		case Calendar.SATURDAY:
			return DateTimeConstants.SATURDAY;
		}

		// should never be possible to get here, but just in case, if we get lost
		// just assume Monday is the first day of the week (ISO standard)
		return Calendar.MONDAY;
	}

	/**
	 * @return a value from 0-6 which can be used to compare whether one date is before or after
	 * another one (by simply looking at a calendar)
	 */
	public static int getDayOfWeekNormalized(LocalDate date) {
		return (date.getDayOfWeek() - getFirstDayOfWeek() + 7) % 7;
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

	/**
	 * @return # of hours between, positive if start is before end, negative if end is before start
	 */
	public static int hoursBetween(DateTime start, DateTime end) {
		return Hours.hoursBetween(start, end).getHours();
	}

	/**
	 * Handles null cases while checking for equality.
	 * @return true if they are equal, or if they are both null
	 */
	public static boolean isEqual(AbstractPartial first, AbstractPartial second) {
		if (first == second) {
			return true;
		}

		if (first != null && second != null) {
			return first.isEqual(second);
		}

		return false;
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

	public static String formatLocalDate(Context context, LocalDate localDate, int flags) {
		return formatDateTime(context, localDate.toDateTimeAtStartOfDay(), flags);
	}

	public static String formatDateTime(Context context, DateTime dateTime, int flags) {
		DateTime utcDateTime = dateTime.withZoneRetainFields(DateTimeZone.UTC);
		return DateUtils.formatDateTime(context, utcDateTime.getMillis(), flags | DateUtils.FORMAT_UTC);
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
					resId = R.plurals.joda_time_android_abbrev_num_seconds_ago;
				}
				else {
					resId = R.plurals.joda_time_android_num_seconds_ago;
				}
			}
			else {
				if (abbrevRelative) {
					resId = R.plurals.joda_time_android_abbrev_in_num_seconds;
				}
				else {
					resId = R.plurals.joda_time_android_in_num_seconds;
				}
			}
		}
		else if (duration.isShorterThan(Duration.standardHours(1)) && minResolution < DateUtils.HOUR_IN_MILLIS) {
			count = duration.getStandardMinutes();
			if (past) {
				if (abbrevRelative) {
					resId = R.plurals.joda_time_android_abbrev_num_minutes_ago;
				}
				else {
					resId = R.plurals.joda_time_android_num_minutes_ago;
				}
			}
			else {
				if (abbrevRelative) {
					resId = R.plurals.joda_time_android_abbrev_in_num_minutes;
				}
				else {
					resId = R.plurals.joda_time_android_in_num_minutes;
				}
			}
		}
		else if (duration.isShorterThan(Duration.standardDays(1)) && minResolution < DateUtils.DAY_IN_MILLIS) {
			count = duration.getStandardHours();
			if (past) {
				if (abbrevRelative) {
					resId = R.plurals.joda_time_android_abbrev_num_hours_ago;
				}
				else {
					resId = R.plurals.joda_time_android_num_hours_ago;
				}
			}
			else {
				if (abbrevRelative) {
					resId = R.plurals.joda_time_android_abbrev_in_num_hours;
				}
				else {
					resId = R.plurals.joda_time_android_in_num_hours;
				}
			}
		}
		else if (duration.isShorterThan(Duration.standardDays(7)) && minResolution < DateUtils.WEEK_IN_MILLIS) {
			count = Math.abs(daysBetween(time, now.withZone(time.getZone())));
			if (past) {
				if (abbrevRelative) {
					resId = R.plurals.joda_time_android_abbrev_num_days_ago;
				}
				else {
					resId = R.plurals.joda_time_android_num_days_ago;
				}
			}
			else {
				if (abbrevRelative) {
					resId = R.plurals.joda_time_android_abbrev_in_num_days;
				}
				else {
					resId = R.plurals.joda_time_android_in_num_days;
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

}
