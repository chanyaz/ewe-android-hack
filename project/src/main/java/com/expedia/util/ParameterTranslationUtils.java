package com.expedia.util;


import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.net.Uri;

import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.Log;


/**
 * Translate Query Parameters in URLs to be consistent with those in URIs
 */
public class ParameterTranslationUtils {

	/**
	 * Query Parameters in URI
	 */
	static final class CustomLinkKeys {
		public static final String CHECK_IN_DATE = "checkInDate";
		public static final String CHECK_OUT_DATE = "checkOutDate";
		public static final String NUM_ADULTS = "numAdults";
		public static final String LOCATION = "location";
		public static final String SORT_TYPE = "sortType";

		public static final String PRICE = "Price";
		public static final String RATING = "Rating";
		public static final String DISCOUNTS = "Discounts";
		public static final String RECOMMENDED = "Recommended";

		public static final String ORIGIN = "origin";
		public static final String DESTINATION = "destination";

		public static final String DEPARTURE_DATE = "departureDate";
		public static final String RETURN_DATE = "returnDate";

		public static final String PICKUP_DATE_TIME = "pickupDateTime";
		public static final String DROPOFF_DATE_TIME = "dropoffDateTime";
		public static final String PICKUP_LOCATION = "pickupLocation";

		public static final String START_DATE = "startDate";
		public static final String END_DATE = "endDate";
		public static final String FILTERS = "filters";
	}

	/**
	 * Query Parameters in URL
	 */
	static final class UniversalLinkKeys {
		public static final String START_DATE = "startDate";
		public static final String END_DATE = "endDate";
		public static final String ADULTS = "adults";
		public static final String REGION_ID = "regionId";
		public static final String SORT = "sort";

		public static final String PRICE = "price";
		public static final String QUEST_RATING = "guestRating";
		public static final String DEALS = "deals";
		public static final String RECOMMENDED = "recommended";
		public static final String MOST_POPULAR = "mostPopular";

		public static final String LEG1 = "leg1";
		public static final String LEG2 = "leg2";
		public static final String TRIP = "trip";

		public static final String PASSENGERS = "passengers";

		public static final String DATE1 = "date1";
		public static final String DATE2 = "date2";
		public static final String TIME1 = "time1";
		public static final String TIME2 = "time2";

		public static final String LOCN = "locn";

		public static final String LOCATION = "location";

		public static final String CATEGORIES = "categories";
	}

	private static final String ONE_WAY = "oneway";
	private static final String ROUND_TRIP = "roundtrip";

	/** from:Seattle, WA (SEA-Seattle - Tacoma Intl.),to:DTW,departure:09/27/2017TANYT */
	private static final Pattern LEG_PATTERN = Pattern.compile("from:(.+),to:(.+),departure:(.+)");
	/** SFO  only 3 digit airport code*/
	private static final Pattern AIRPORT_CODE = Pattern.compile("[^A-Z]?([A-Z]{3})[^A-Z]?");
	/** 12/07/2016TANYT */
	private static final Pattern DATETIME = Pattern.compile("([^a-zA-Z]+)T?(.+)");
	/** adults:5 */
	private static final Pattern NUM_ADULTS = Pattern.compile("adults:([0-9])+,");
	/** 700AM */
	private static final Pattern TIME = Pattern.compile("([0-9]{1,2})([0-9]{2})(AM|PM)");

	private static final String TAG = "ParameterTranslationUtils";

	public static final DateTimeFormatter customLinkDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

	public static final DateTimeFormatter universalLinkDateFormatter = DateTimeFormat.forPattern("MM/dd/yyyy");


	public static Uri hotelSearchLink(Uri data) {
		Set<String> queryData = StrUtils.getQueryParameterNames(data);
		Set<String> toRemove = new HashSet<>();
		Uri.Builder formattedData = Uri.parse(data.toString().split("\\?")[0]).buildUpon();

		if (queryData.contains(UniversalLinkKeys.START_DATE)) {
			LocalDate date = LocalDate.parse(data.getQueryParameter(UniversalLinkKeys.START_DATE),
				universalLinkDateFormatter);
			formattedData.appendQueryParameter(CustomLinkKeys.CHECK_IN_DATE, date.toString());
		}
		if (queryData.contains(UniversalLinkKeys.END_DATE)) {
			LocalDate date = LocalDate.parse(data.getQueryParameter(UniversalLinkKeys.END_DATE),
				universalLinkDateFormatter);
			formattedData.appendQueryParameter(CustomLinkKeys.CHECK_OUT_DATE, date.toString());
		}
		if (queryData.contains(UniversalLinkKeys.ADULTS)) {
			formattedData.appendQueryParameter(CustomLinkKeys.NUM_ADULTS, data.getQueryParameter(UniversalLinkKeys.ADULTS));
		}
		if (queryData.contains(UniversalLinkKeys.REGION_ID)) {
			formattedData.appendQueryParameter(CustomLinkKeys.LOCATION, "ID".concat(data.getQueryParameter(
				UniversalLinkKeys.REGION_ID)));
		}
		if (queryData.contains(UniversalLinkKeys.SORT)) {
			formattedData.appendQueryParameter(CustomLinkKeys.SORT_TYPE, sortTypeTranslation(data.getQueryParameter(
				UniversalLinkKeys.SORT)));
		}

		toRemove.add(UniversalLinkKeys.START_DATE);
		toRemove.add(UniversalLinkKeys.END_DATE);
		toRemove.add(UniversalLinkKeys.ADULTS);
		toRemove.add(UniversalLinkKeys.REGION_ID);
		toRemove.add(UniversalLinkKeys.SORT);

		for (String s: queryData) {
			if (!s.equals("") && !toRemove.contains(s)) {
				formattedData.appendQueryParameter(s, data.getQueryParameter(s));
			}
		}

		Log.d(TAG, "formatted url: " + formattedData.build().toString());

		return formattedData.build();
	}

	private static String sortTypeTranslation(String sortType) {
		switch (sortType) {
			case UniversalLinkKeys.PRICE:
				return CustomLinkKeys.PRICE;
			case UniversalLinkKeys.QUEST_RATING:
				return CustomLinkKeys.RATING;
			case UniversalLinkKeys.DEALS:
				return CustomLinkKeys.DISCOUNTS;
			case UniversalLinkKeys.RECOMMENDED:
				return CustomLinkKeys.RECOMMENDED;
			case UniversalLinkKeys.MOST_POPULAR:
				return CustomLinkKeys.RECOMMENDED;
			default:
				return CustomLinkKeys.RECOMMENDED;
		}
	}

	public static Uri flightSearchLink(Uri data) {

		Set<String> queryData = StrUtils.getQueryParameterNames(data);
		Set<String> toRemove = new HashSet<>();
		Uri.Builder formattedData = Uri.parse(data.toString().split("\\?")[0]).buildUpon();

		if (queryData.contains(UniversalLinkKeys.TRIP) &&
			(data.getQueryParameter(UniversalLinkKeys.TRIP).equals(ONE_WAY) || data.getQueryParameter(UniversalLinkKeys.TRIP).equals(ROUND_TRIP))) {

			String tripType = data.getQueryParameter(UniversalLinkKeys.TRIP);

			if (queryData.contains(UniversalLinkKeys.LEG1)) {
				String leg1 = data.getQueryParameter(UniversalLinkKeys.LEG1);
				Matcher leg1Matcher = LEG_PATTERN.matcher(leg1);

				if (leg1Matcher.find()) {
					String from = leg1Matcher.group(1);
					String to = leg1Matcher.group(2);
					String departure = leg1Matcher.group(3);

					Matcher airportCodeFromMatcher = AIRPORT_CODE.matcher(from);
					Matcher airportCodeToMatcher = AIRPORT_CODE.matcher(to);
					Matcher departureMatcher = DATETIME.matcher(departure);

					if (airportCodeFromMatcher.find() && airportCodeToMatcher.find() && departureMatcher.find()) {
						formattedData.appendQueryParameter(CustomLinkKeys.ORIGIN, airportCodeFromMatcher.group(1));
						formattedData.appendQueryParameter(CustomLinkKeys.DESTINATION, airportCodeToMatcher.group(1));

						LocalDate localDate = LocalDate.parse(departureMatcher.group(1), universalLinkDateFormatter);
						formattedData.appendQueryParameter(CustomLinkKeys.DEPARTURE_DATE, localDate.toString());
					}
				}
			}

			if (tripType.equals(ROUND_TRIP) && queryData.contains(UniversalLinkKeys.LEG2)) {
				String leg2 = data.getQueryParameter(UniversalLinkKeys.LEG2);
				Matcher leg2Matcher = LEG_PATTERN.matcher(leg2);

				if (leg2Matcher.find()) {
					String departure = leg2Matcher.group(3);
					Matcher departureMatcher = DATETIME.matcher(departure);
					if (departureMatcher.find()) {
						LocalDate localDate = LocalDate.parse(departureMatcher.group(1), universalLinkDateFormatter);
						formattedData.appendQueryParameter(CustomLinkKeys.RETURN_DATE, localDate.toString());
					}
				}

			}

			if (queryData.contains(UniversalLinkKeys.PASSENGERS)) {
				Matcher numAdultsMatcher = NUM_ADULTS.matcher(data.getQueryParameter(UniversalLinkKeys.PASSENGERS));
				if (numAdultsMatcher.find()) {
					formattedData.appendQueryParameter(CustomLinkKeys.NUM_ADULTS, numAdultsMatcher.group(1));
				}

			}
		}

		toRemove.add(UniversalLinkKeys.TRIP);
		toRemove.add(UniversalLinkKeys.LEG1);
		toRemove.add(UniversalLinkKeys.LEG2);
		toRemove.add(UniversalLinkKeys.PASSENGERS);

		for (String s: queryData) {
			if (!toRemove.contains(s)) {
				formattedData.appendQueryParameter(s, data.getQueryParameter(s));
			}
		}

		Log.d(TAG, "formatted url: " + formattedData.build().toString());

		return formattedData.build();
	}

	public static Uri carSearchLink(Uri data) {
		Set<String> queryData = StrUtils.getQueryParameterNames(data);
		Set<String> toRemove = new HashSet<>();
		Uri.Builder formattedData = Uri.parse(data.toString().split("\\?")[0]).buildUpon();

		if (queryData.contains(UniversalLinkKeys.DATE1) && queryData.contains(UniversalLinkKeys.TIME1)) {
			LocalDate date = LocalDate.parse(data.getQueryParameter(UniversalLinkKeys.DATE1),
				universalLinkDateFormatter);
			Matcher timeMatcher = TIME.matcher(data.getQueryParameter(UniversalLinkKeys.TIME1));

			if (timeMatcher.find()) {
				StringBuilder pickUpDateTime = new StringBuilder();
				pickUpDateTime.append(date.toString());
				pickUpDateTime.append("T" + timeFormatting(timeMatcher.group(1), timeMatcher.group(2), timeMatcher.group(3)));
				formattedData.appendQueryParameter(CustomLinkKeys.PICKUP_DATE_TIME, pickUpDateTime.toString());
			}

		}

		if (queryData.contains(UniversalLinkKeys.DATE2) && queryData.contains(UniversalLinkKeys.TIME2)) {
			LocalDate date = LocalDate.parse(data.getQueryParameter(UniversalLinkKeys.DATE2),
				universalLinkDateFormatter);
			Matcher timeMatcher = TIME.matcher(data.getQueryParameter(UniversalLinkKeys.TIME2));

			if (timeMatcher.find()) {
				StringBuilder dropOffDateTime = new StringBuilder();
				dropOffDateTime.append(date.toString());
				dropOffDateTime.append("T" + timeFormatting(timeMatcher.group(1), timeMatcher.group(2), timeMatcher.group(3)));
				formattedData.appendQueryParameter(CustomLinkKeys.DROPOFF_DATE_TIME, dropOffDateTime.toString());
			}

		}

		if (queryData.contains(UniversalLinkKeys.LOCN)) {
			Matcher airportCodeMatcher = AIRPORT_CODE.matcher(data.getQueryParameter(UniversalLinkKeys.LOCN));
			if (airportCodeMatcher.find()) {
				formattedData.appendQueryParameter(CustomLinkKeys.PICKUP_LOCATION, airportCodeMatcher.group(1));
			}
		}

		toRemove.add(UniversalLinkKeys.DATE1);
		toRemove.add(UniversalLinkKeys.TIME1);
		toRemove.add(UniversalLinkKeys.DATE2);
		toRemove.add(UniversalLinkKeys.TIME2);
		toRemove.add(UniversalLinkKeys.LOCN);

		for (String s: queryData) {
			if (!toRemove.contains(s)) {
				formattedData.appendQueryParameter(s, data.getQueryParameter(s));
			}
		}

		Log.d(TAG, "formatted url: " + formattedData.build().toString());

		return formattedData.build();
	}

	private static String timeFormatting(String hour, String minute, String ampm) {
		StringBuilder time = new StringBuilder();
		if (ampm.equals("AM")) {
			if (Integer.parseInt(hour) < 10) {
				hour = "0".concat(hour);
			}
			time.append(hour).append(":").append(minute).append(":").append("00").toString();
		}
		else if (ampm.equals("PM")) {
			int hourPM = Integer.parseInt(hour) + 12;
			time.append(hourPM).append(":").append(minute).append(":").append("00").toString();
		}
		return time.toString();
	}

	public static Uri lxSearchLink(Uri data) {
		Set<String> queryData = StrUtils.getQueryParameterNames(data);
		Set<String> toRemove = new HashSet<>();
		Uri.Builder formattedData = Uri.parse(data.toString().split("\\?")[0]).buildUpon();

		if (queryData.contains(UniversalLinkKeys.START_DATE)) {
			LocalDate date = LocalDate.parse(data.getQueryParameter(UniversalLinkKeys.START_DATE),
				universalLinkDateFormatter);
			formattedData.appendQueryParameter(CustomLinkKeys.START_DATE, date.toString());
		}

		if (queryData.contains(UniversalLinkKeys.END_DATE)) {
			LocalDate date = LocalDate.parse(data.getQueryParameter(UniversalLinkKeys.END_DATE),
				universalLinkDateFormatter);
			formattedData.appendQueryParameter(CustomLinkKeys.END_DATE, date.toString());
		}

		if (queryData.contains(UniversalLinkKeys.LOCATION)) {
			String city = StrUtils.formatCityName(data.getQueryParameter(UniversalLinkKeys.LOCATION));
			formattedData.appendQueryParameter(CustomLinkKeys.LOCATION, city);
		}

		if (queryData.contains(UniversalLinkKeys.CATEGORIES)) {
			formattedData.appendQueryParameter(CustomLinkKeys.FILTERS, data.getQueryParameter(UniversalLinkKeys.CATEGORIES));
		}

		toRemove.add(UniversalLinkKeys.START_DATE);
		toRemove.add(UniversalLinkKeys.END_DATE);
		toRemove.add(UniversalLinkKeys.LOCATION);
		toRemove.add(UniversalLinkKeys.CATEGORIES);

		for (String s: queryData) {
			if (!toRemove.contains(s)) {
				formattedData.appendQueryParameter(s, data.getQueryParameter(s));
			}
		}

		Log.d(TAG, "formatted url: " + formattedData.build().toString());

		return formattedData.build();
	}
}
