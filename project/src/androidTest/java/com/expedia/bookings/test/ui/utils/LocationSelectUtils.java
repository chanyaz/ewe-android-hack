package com.expedia.bookings.test.ui.utils;

import java.util.Random;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Pair;

import com.expedia.bookings.activity.RouterActivity;
import com.mobiata.android.Log;

public class LocationSelectUtils extends ActivityInstrumentationTestCase2<RouterActivity> {

	public LocationSelectUtils() {
		super("com.expedia.bookings", RouterActivity.class);
	}

	//20 busiest US Airports
	public static final String[] AIRPORT_LIST_USA = new String[] {
			"ATL", "ORD", "LAX", "DFW", "DEN",
			"JFK", "SFO", "LAS", "PHX", "IAH",
			"CLT", "MIA", "MCO", "EWR", "SEA",
			"MSP", "DTW", "PHL", "BOS", "LGA",
	};

	//20 big international airports
	public static final String[] AIRPORT_LIST_INTL = new String[] {
			"LHR", "CDG", "HND", "FRA", "CGK",
			"DXB", "HKG", "BKK", "AMS", "SIN",
			"CAN", "MAD", "PVG", "IST", "ICN",
	};

	//Arbitrarily selected international cities
	public static final String[] HOTEL_CITIES_INTL = new String[] {
			"Tokyo, JP", "London, UK", "Barcelona, Spain", "Paris, FR",
			"Vancouver, CA", "Montreal, CA", "Prague", "Rio de Janeiro, Brazil",
			"Budapest, Hungary", "Bahamas", "Beijing", "Tel Aviv", "Hong Kong",
			"Dublin, Ireland", "Seoul, South Korea", "Munich, Germany",
			"Zurich, Switzerland", "Bangkok, Thailand",
	};

	//Arbitrarily selected US cities
	public static final String[] HOTEL_CITIES_USA = new String[] {
			"Washington D.C.", "Detroit, MI", "Chicago, IL", "New York, NY",
			"Boston, MA", "Philadelphia, PA", "Pittsburgh, PA", "Atlanta, GA",
			"Miami, FL", "Tampa, FL", "Jacksonville, FL", "Houston, TX",
			"Dallas, TX", "San Diego, CA", "Los Angeles, CA", "San Francisco, CA",
			"Portland, OR", "Seattle, WA", "Honolulu, HI", "Phoenix, AZ",
	};

	public Pair<String, String> getTwoRandomInternationalCities() {
		return getTwoRandomItems(HOTEL_CITIES_INTL);
	}

	public Pair<String, String> getTwoRandomAmericanCities() {
		return getTwoRandomItems(HOTEL_CITIES_USA);
	}

	public Pair<String, String> getRandomAmericanAndInternationalCity() {
		return getTwoRandomItems(HOTEL_CITIES_USA, HOTEL_CITIES_INTL);
	}

	public Pair<String, String> getTwoRandomInternationalAirports() {
		return getTwoRandomItems(AIRPORT_LIST_INTL);
	}

	public Pair<String, String> getTwoRandomAmericanAirports() {
		return getTwoRandomItems(AIRPORT_LIST_USA);
	}

	public Pair<String, String> getRandomAmericanAndInternationalAirport() {
		return getTwoRandomItems(AIRPORT_LIST_USA, AIRPORT_LIST_INTL);
	}

	public Pair<String, String> getTwoRandomItems(String[] airportList) {
		return getTwoRandomItems(airportList, airportList);
	}

	public Pair<String, String> getTwoRandomItems(String[] airportList1, String[] airportList2) {
		Random numberGen = new Random();
		String airport1 = airportList1[numberGen.nextInt(airportList1.length)];
		String airport2 = airportList2[numberGen.nextInt(airportList2.length)];
		Log.d("LocationSelectUtils", "Airports/hotels selected: " + airport1 + " " + airport2);

		while (airport1.equals(airport2)) {
			airport2 = airportList2[numberGen.nextInt(airportList2.length)];
		}

		Pair<String, String> airports = new Pair<String, String>(airport1, airport2);
		return airports;

	}

}
