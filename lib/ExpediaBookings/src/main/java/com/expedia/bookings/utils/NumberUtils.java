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
}
