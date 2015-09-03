package com.expedia.bookings.utils;

public class NumberUtils {

	public static Double parseDoubleSafe(String str) {
		if (Strings.isEmpty(str)) {
			return null;
		}

		try {
			return Double.parseDouble(str);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	public static double roundOff(float number, int dp) {
		double factor = Math.pow(10, dp);
		return Math.round(number * factor) / factor;
	}
}
