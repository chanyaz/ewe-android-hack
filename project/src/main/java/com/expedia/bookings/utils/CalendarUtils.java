package com.expedia.bookings.utils;

import org.joda.time.DateTime;

public class CalendarUtils {

	/**
	 * Checks if a timestamp has expired, given a particular cutoff
	 * 
	 * Returns false if somehow the timestamp is in front of now (which should be impossible)
	 * or the timestamp is more than "cutoff" away
	 */
	public static boolean isExpired(long timestamp, long cutoff) {
		long now = DateTime.now().getMillis();
		return now < timestamp || timestamp + cutoff < now;
	}
}
