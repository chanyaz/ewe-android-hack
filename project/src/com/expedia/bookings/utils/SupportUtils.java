package com.expedia.bookings.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class SupportUtils {

	// Default is US's number
	private static final String DEFAULT_SUPPORT_NUMBER = "1-800-780-5733";

	// Which support number to use for which country code (based on locale)
	@SuppressWarnings("serial")
	private static final HashMap<String, String> SUPPORT_NUMBERS = new HashMap<String, String>() {
		{
			put("US", "1-800-780-5733");
			put("CA", "1-800-780-5733");
			put("JP", "81-3-5539-2655");
			put("TW", "00801-13-6098");
			put("TH", "001-800-12-0666828");
			put("HK", "800-905-165");
			put("KR", "00798-14-800-7901");
			put("NZ", "0800-456-084");
			put("SG", "800-120-5484");
			put("MY", "1-800-815110");
			put("AU", "1-800-188315");
		}
	};

	private static final String[] EUROPEAN_COUNTRY_CODES = new String[] { "EU", "AD", "AL", "AT", "BA", "BE", "BG",
			"BY", "CH", "CZ", "DE", "DK", "EE", "ES", "FI", "FO", "FR", "FX", "GB", "GI", "GR", "HR", "HU", "IE", "IS",
			"IT", "LI", "LT", "LU", "LV", "MC", "MD", "MK", "MT", "NL", "NO", "PL", "PT", "RO", "SE", "SI", "SJ", "SK",
			"SM", "UA", "VA", "CS"
	};

	// Ensure that EUROPEAN_COUNTRY_CODES is sorted for later binary search usage
	static {
		Arrays.sort(EUROPEAN_COUNTRY_CODES);
	}

	// Default is the US website
	private static final String DEFAULT_WEBSITE = "http://www.expedia.com/?rfrr=app.android";

	// Which website to use for which country code (based on locale)
	@SuppressWarnings("serial")
	private static final HashMap<String, String> WEBSITE_URLS = new HashMap<String, String>() {
		{
			put("AU", "http://www.expedia.com.au/?rfrr=app.android");
			put("CA", "http://www.expedia.ca/?rfrr=app.android");
			put("GB", "http://www.expedia.co.uk/?rfrr=app.android");
			put("NZ", "http://www.expedia.co.nz/?rfrr=app.android");
			put("US", "http://www.expedia.com/?rfrr=app.android");
		}
	};

	public static String getSupportUrl() {
		return "http://m.expedia.com/mt/support.expedia.com/app/home/p/532/?rfrr=app.android";
	}

	public static boolean hasSupportNumber() {
		String countryCode = Locale.getDefault().getCountry().toUpperCase();
		return (SUPPORT_NUMBERS.containsKey(countryCode) || Arrays.binarySearch(EUROPEAN_COUNTRY_CODES, countryCode) >= 0);
	}

	public static String getSupportNumber() {
		String countryCode = Locale.getDefault().getCountry().toUpperCase();
		boolean isEuropean = Arrays.binarySearch(EUROPEAN_COUNTRY_CODES, countryCode) >= 0;
		if (SUPPORT_NUMBERS.containsKey(countryCode)) {
			return SUPPORT_NUMBERS.get(countryCode);
		}
		else if (isEuropean) {
			// Many European countries share the same code
			return "00-800-11-20-11-40";
		}
		else {
			return DEFAULT_SUPPORT_NUMBER;
		}
	}

	public static String getWebsiteUrl() {
		String countryCode = Locale.getDefault().getCountry().toUpperCase();
		if (WEBSITE_URLS.containsKey(countryCode)) {
			return WEBSITE_URLS.get(countryCode);
		}
		else {
			return DEFAULT_WEBSITE;
		}
	}

}
