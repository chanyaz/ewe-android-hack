package com.expedia.bookings.utils;

import java.util.Locale;

public class LocaleUtils {

	public static String convertCountryCode(String ccode) {
		if (ccode == null || ccode.length() > 2) {
			return ccode;
		}
		else {
			Locale loc = new Locale("", ccode);
			return loc.getISO3Country();
		}
	}

}
