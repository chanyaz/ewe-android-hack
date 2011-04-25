package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Locale;

public class SupportUtils {

	// Default is GB's number
	private static final String DEFAULT_SUPPORT_NUMBER = "0330-123-1235";

	// Which support number to use for which country code (based on locale)
	@SuppressWarnings("serial")
	private static final HashMap<String, String> SUPPORT_NUMBERS = new HashMap<String, String>() {
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

	public static String getSupportNumber() {
		String countryCode = Locale.getDefault().getCountry().toUpperCase();
		if (SUPPORT_NUMBERS.containsKey(countryCode)) {
			return SUPPORT_NUMBERS.get(countryCode);
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
