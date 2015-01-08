package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Locale;

public class LocaleUtils {

	private static HashMap<String, Locale> mLocaleMap = initMap();

	public static String convertCountryCode(String ccode) {
		if (ccode == null || ccode.length() > 2) {
			return ccode;
		}
		else {
			Locale loc = new Locale("", ccode);
			return loc.getISO3Country();
		}
	}

	private static HashMap<String, Locale> initMap() {
		String[] countries = Locale.getISOCountries();
		HashMap<String, Locale> map = new HashMap<String, Locale>(countries.length);
		for (String country : countries) {
			Locale locale = new Locale("", country);
			map.put(locale.getISO3Country().toUpperCase(Locale.US), locale);
		}
		return map;
	}

	//Returns ISO2 county code
	public static String convertISO3ToISO2CountryCode(String iso3CountryCode) {
		return mLocaleMap.get(iso3CountryCode).getCountry();
	}

	public static Locale localeFromISO3CountryCode(String code) {
		return new Locale("", LocaleUtils.convertISO3ToISO2CountryCode(code));
	}
}
