package com.expedia.bookings.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class SupportUtils {

	// Info numbers - these go on the info screen

	// Default is GB's number
	private static final String DEFAULT_SUPPORT_NUMBER_INFO = "0330-123-1235";

	// Which support number to use for which country code (based on locale)
	@SuppressWarnings("serial")
	private static final HashMap<String, String> SUPPORT_NUMBERS_INFO = new HashMap<String, String>() {
		{
			put("US", "1-877-829-0215");
			put("CA", "1-888-EXPEDIA");
			put("MX", "001-8003157301");
			put("AU", "13-38-10");
			put("NZ", "0800-998-799");
			put("JP", "0120-142-650");
			put("IN", "1800-419-1919");
			put("SG", "800-120-5806");
			put("MY", "1-800-815676");
			put("TH", "001-800-12-0667078");
			put("IT", "+39-02-91483685");
			put("DE", "01805-007146");
			put("NL", "0900-397-3342");
			put("ES", "901-01-01-14");
			put("AT", "0820-600630");
			put("GB", "0330-123-1235");
			put("FR", "0892-301-300");
			put("SE", "0200-810-341");
			put("DK", "80200088");
			put("NO", "800-36-401");
		}
	};

	// Confirmation numbers - these go on the confirmation screen

	// Default is US's number
	private static final String DEFAULT_SUPPORT_NUMBER_CONFIRMATION = "1-800-780-5733";

	// Which support number to use for which country code (based on locale)
	@SuppressWarnings("serial")
	private static final HashMap<String, String> SUPPORT_NUMBERS_CONFIRMATION = new HashMap<String, String>() {
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

	public static String getContactExpediaUrl() {
		Locale locale = Locale.getDefault();
		String countryCode = locale.getCountry().toUpperCase();
		String languageCode = locale.getLanguage().toUpperCase();

		if (countryCode.equals("FR")) {
			return "http://www.expedia.fr/?rfrr=app.android";
		}
		else if (countryCode.equals("ES")) {
			return "http://www.expedia.es/?rfrr=app.android";
		}
		else if (countryCode.equals("SE")) {
			return "http://www.expedia.se/?rfrr=app.android";
		}
		else if (countryCode.equals("DE")) {
			return "http://www.expedia.de/?rfrr=app.android";
		}
		else if (countryCode.equals("IT")) {
			return "http://www..expedia.it/?rfrr=app.android";
		}
		else if (countryCode.equals("BR") && languageCode.equals("PT")) {
			return "http://www.expedia.com.br/?rfrr=app.android";
		}
		else if (countryCode.equals("NO")) {
			return "http://www.expedia.no/?rfrr=app.android";
		}
		else if (countryCode.equals("GB")) {
			return "http://www.expedia.co.uk/?rfrr=app.android";
		}
		else if (countryCode.equals("IE")) {
			return "http://www.expedia.ie/?rfrr=app.android";
		}
		else if (countryCode.equals("AU")) {
			return "http://www.expedia.com.au/?rfrr=app.android";
		}
		else if (countryCode.equals("AT")) {
			return "http://www.expedia.at/?rfrr=app.android";
		}
		else if (countryCode.equals("BE")) {
			if (languageCode.equals("FR")) {
				return "http://www.expedia.be/?langid=1036&rfrr=app.android";
			}
			else {
				return "http://www.expedia.be/?langid=1043&rfrr=app.android";
			}
		}
		else if (countryCode.equals("CA")) {
			if (languageCode.equals("FR")) {
				return "http://www.expedia.ca/pub/agent.dll?qscr=chlg&lang=3084&ovrd=3&rurl=%2F&lnkcid=FRC&rfrr=app.android";
			}
			else {
				return "http://www.expedia.ca/pub/agent.dll?qscr=chlg&lang=4105&ovrd=3&rurl=%2F&lnkcid=ENC&rfrr=app.android";
			}
		}
		else if (countryCode.equals("DK")) {
			return "http://www.expedia.dk/?rfrr=app.android";
		}
		else if (countryCode.equals("IN")) {
			return "http://www.expedia.co.in/?rfrr=app.android";
		}
		else if (countryCode.equals("JP")) {
			return "http://www.expedia.co.jp/?rfrr=app.android";
		}
		else if (countryCode.equals("MY")) {
			if (languageCode.equals("MS")) {
				return "http://myfaq.expedia.com.my/?rfrr=app.android";
			}
			else {
				return "http://www.expedia.com.my/?rfrr=app.android";
			}
		}
		else if (countryCode.equals("MX")) {
			return "http://www.expedia.com.mx/?rfrr=app.android";
		}
		else if (countryCode.equals("NL")) {
			return "http://www.expedia.nl/?rfrr=app.android";
		}
		else if (countryCode.equals("NZ")) {
			return "http://www.expedia.co.nz/?rfrr=app.android";
		}
		else if (countryCode.equals("SG")) {
			return "http://www.expedia.com.sg/?rfrr=app.android";
		}
		else if (countryCode.equals("KR")) {
			return "http://faq.expedia.co.kr/?rfrr=app.android";
		}
		else if (countryCode.equals("ID")) {
			return "http://faq.expedia.co.id/?rfrr=app.android";
		}
		else if (countryCode.equals("PH")) {
			if (languageCode.equals("TL")) {
				return "http://phfaq.expedia.com.ph/?rfrr=app.android";
			}
			else {
				return "http://faq.expedia.com.ph/?rfrr=app.android";
			}
		}

		// Default to US version
		return "http://m.expedia.com/?rfrr=app.android";
	}

	public static String getInfoSupportNumber() {
		String countryCode = Locale.getDefault().getCountry().toUpperCase();
		if (SUPPORT_NUMBERS_INFO.containsKey(countryCode)) {
			return SUPPORT_NUMBERS_INFO.get(countryCode);
		}
		else {
			return DEFAULT_SUPPORT_NUMBER_INFO;
		}
	}

	public static boolean hasConfSupportNumber() {
		String countryCode = Locale.getDefault().getCountry().toUpperCase();
		return (SUPPORT_NUMBERS_CONFIRMATION.containsKey(countryCode) || Arrays.binarySearch(EUROPEAN_COUNTRY_CODES,
				countryCode) >= 0);
	}

	public static String getConfSupportNumber() {
		String countryCode = Locale.getDefault().getCountry().toUpperCase();
		boolean isEuropean = Arrays.binarySearch(EUROPEAN_COUNTRY_CODES, countryCode) >= 0;
		if (SUPPORT_NUMBERS_CONFIRMATION.containsKey(countryCode)) {
			return SUPPORT_NUMBERS_CONFIRMATION.get(countryCode);
		}
		else if (isEuropean) {
			// Many European countries share the same code
			return "00-800-11-20-11-40";
		}
		else {
			return DEFAULT_SUPPORT_NUMBER_CONFIRMATION;
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
