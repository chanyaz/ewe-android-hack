package com.expedia.bookings.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.expedia.bookings.data.SuggestionV4;

public class SuggestionStrUtils {
	// e.g. San Francisco, CA, United States (SFO-San Francisco Int'l Airport) -> San Francisco, CA
	private static final Pattern CITY_STATE_PATTERN = Pattern.compile("^([^,]+,[^,]+)");
	// e.g. Kuantan, Malaysia (KUA-Sultan Haji Ahmad Shah) -> Kuantan, Malyasia
	private static final Pattern CITY_COUNTRY_PATTERN = Pattern.compile("^([^,]+,[^,]+(?= \\(.*\\)))");
	// e.g. Kuantan, Malaysia (KUA-Sultan Haji Ahmad Shah) -> KUA-Sultan Haji Ahmad Shah
	private static final Pattern AIRPORT_CODE_PATTERN = Pattern.compile("\\((.*?)\\)");


	public static String formatCityName(String suggestion) {
		String city = suggestion;

		Matcher cityCountryMatcher = CITY_COUNTRY_PATTERN.matcher(city);
		if (cityCountryMatcher.find()) {
			city = cityCountryMatcher.group(1);
		}
		else {
			Matcher cityStateMatcher = CITY_STATE_PATTERN.matcher(city);
			if (cityStateMatcher.find()) {
				city = cityStateMatcher.group(1);
			}
		}
		return city;
	}

	public static String formatAirport(SuggestionV4 suggestion) {
		String airportName = formatAirportName(suggestion.regionNames.fullName);
		if (suggestion.hierarchyInfo != null && suggestion.hierarchyInfo.airport != null
			&& !airportName.equals(suggestion.hierarchyInfo.airport.airportCode)) {
			return airportName;
		}
		else {
			return formatCityName(suggestion.regionNames.fullName);
		}
	}

	public static String formatAirportName(String suggestion) {
		String city = suggestion;
		Matcher cityCountryMatcher = AIRPORT_CODE_PATTERN.matcher(city);
		if (cityCountryMatcher.find()) {
			city = cityCountryMatcher.group(1);
		}
		if (city != null && city.contains("-")) {
			city = city.replace("-", " - ");
		}
		return city;
	}
}
