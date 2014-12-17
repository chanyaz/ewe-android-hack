package com.expedia.bookings.utils;

import org.joda.time.LocalDate;

public class DateUtils {
	/**
	 * Formats the given date in yyyyMMDD format and returns as int
	 */
	public static int convertDatetoInt(LocalDate date) {
		int dateInteger = 0;
		if (date != null) {
			String year = "" + date.getYear();
			String month = "";
			String day = "";
			if (date.getMonthOfYear() < 10) {
				month = "0" + date.getMonthOfYear();
			}
			else {
				month = "" + date.getMonthOfYear();
			}
			if (date.getDayOfMonth() < 10) {
				day = "0" + date.getDayOfMonth();
			}
			else {
				day = "" + date.getDayOfMonth();
			}
			String dateString = year + month + day;
			dateInteger = Integer.valueOf(dateString);
		}
		return dateInteger;
	}

}
