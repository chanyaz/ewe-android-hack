package com.expedia.bookings.test.ui.utils;

import java.util.HashMap;
import java.util.Locale;

import android.util.Pair;

public class MarketingSearchParamsUtils {

	private static final HashMap<Locale, Pair<String, String>> FLIGHTS_LOCALE_AIRPORT_MAP = new HashMap<Locale, Pair<String, String>>();

	static {
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("da", "DK"), new Pair("CPH", "LHR"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("de", "AT"), new Pair("VIE", "LHR"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("de", "DE"), new Pair("TXL", "LHR"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "AU"), new Pair("SYD", "DPS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "CA"), new Pair("YYZ", "LAS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "HK"), new Pair("HKG", "BKK"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "IE"), new Pair("DUB", "LAS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "IN"), new Pair("BOM", "SIN"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "NZ"), new Pair("AKL", "LAS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "IN"), new Pair("BOM", "SIN"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "PH"), new Pair("MNL", "PVG"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "SG"), new Pair("SIN", "BKK"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "IN"), new Pair("BOM", "SIN"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "UK"), new Pair("LHR", "AMS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("en", "US"), new Pair("SFO", "LAS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("es", "AR"), new Pair("EZE", "LAS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("es", "ES"), new Pair("MAD", "FCO"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("es", "MX"), new Pair("MEX", "LAS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("fr", "BE"), new Pair("BRU", "JFK"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("fr", "CA"), new Pair("YUL", "LAS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("fr", "FR"), new Pair("CDG", "JFK"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("id", "ID"), new Pair("CGK", "SYD"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("it", "IT"), new Pair("FCO", "CDG"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("ja", "JP"), new Pair("NRT", "ICN"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("ko", "KR"), new Pair("ICN", "MNL"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("ms", "MY"), new Pair("KUL", "SIN"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("nb", "NO"), new Pair("OSL", "LHR"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("nl", "BE"), new Pair("BRU", "JFK"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("nl", "NL"), new Pair("AMS", "FCO"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("pt", "BR"), new Pair("GIG", "LAS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("pt", "PT"), new Pair("LIS", "JFK"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("sv", "SE"), new Pair("ARN", "LHR"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("th", "TH"), new Pair("DMK", "CNX"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("zh", "ZH"), new Pair("HKG", "LAS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("zh", "HK"), new Pair("HGK", "LAS"));
		FLIGHTS_LOCALE_AIRPORT_MAP.put(new Locale("zh", "TW"), new Pair("TPE", "LAS"));
	}


	private static final HashMap<Locale, String> HOTELS_LOCALE_DESTINATION_MAP = new HashMap<Locale, String>();

	static {
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("da", "DK"), "Copenhagen, Denmark");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("de", "AT"), "Vienna, Austria");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("de", "DE"), "Berlin, Germany");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "AU"), "Sydney, Australia");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "CA"), "Toronto, Canada");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "HK"), "Hong Kong");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "IE"), "Dublin, Ireland");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "IN"), "Bombay, India");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "NZ"), "Auckland, New Zealand");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "IN"), "Bombay, India");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "PH"), "Manila");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "SG"), "Singapore");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "IN"), "Bombay, India");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "UK"), "London, England");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("en", "US"), "San Francisco, California");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("es", "AR"), "Buenos Aires, Argentina");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("es", "ES"), "Madrid, Spain");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("es", "MX"), "Mexico City, Mexico");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("fr", "BE"), "Brussels, Belgium");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("fr", "CA"), "Montreal, Canada");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("fr", "FR"), "Paris, France");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("id", "ID"), "Jakarta, Indonesia");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("it", "IT"), "Rome, Italy");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("ja", "JP"), "Tokyo, Japan");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("ko", "KR"), "Seoul, Korea");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("ms", "MY"), "Kuala Lumpur, Malaysia");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("nb", "NO"), "Oslo, Norway");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("nl", "BE"), "Brussels, Belgium");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("nl", "NL"), "Amsterdam, Netherlands");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("pt", "BR"), "Rio de Janiero, Brazil");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("pt", "PT"), "Lisbon, Portugal");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("sv", "SE"), "Stockholm, Sweden");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("th", "TH"), "Bangkok, Thailand");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("zh", "ZH"), "Hong Kong");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("zh", "HK"), "Hong Kong");
		HOTELS_LOCALE_DESTINATION_MAP.put(new Locale("zh", "TW"), "Taipei, Taiwan");
	}


	public static Pair<String, String> getAirportPairForLocale(Locale l) {
		return FLIGHTS_LOCALE_AIRPORT_MAP.get(l);
	}

	public static String getHotelDestinationForLocale(Locale l) {
		return HOTELS_LOCALE_DESTINATION_MAP.get(l);
	}

}
