package com.expedia.bookings.test.utils;

import java.util.Random;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Pair;

import com.expedia.bookings.activity.SearchActivity;

public class AirportSelectUtils extends ActivityInstrumentationTestCase2<SearchActivity> {

	public AirportSelectUtils() {
		super("com.expedia.bookings", SearchActivity.class);
	}

	//20 busiest US Airports
	public static String[] AIRPORT_LIST_USA = new String[] {
			"ATL", "ORD", "LAX", "DFW", "DEN",
			"JFK", "SFO", "LAS", "PHX", "IAH",
			"CLT", "MIA", "MCO", "EWR", "SEA",
			"MSP", "DTW", "PHL", "BOS", "LGA",
	};

	public static String[] AIRPORT_LIST_INTL = new String[] {
			"LHR", "CDG", "HND", "FRA", "CGK",
			"DXB", "HKG", "BKK", "AMS", "SIN",
			"CAN", "MAD", "PVG", "IST", "ICN",
	};

	public Pair<String, String> getTwoAirports(String[] airportList) {
		return getTwoAirports(airportList, airportList);
	}

	public Pair<String, String> getTwoAirports(String[] airportList1, String[] airportList2) {
		Random numberGen = new Random();
		String airport1 = airportList1[numberGen.nextInt(airportList1.length)];
		String airport2 = airportList2[numberGen.nextInt(airportList2.length)];

		while (airport1.equals(airport2)) {
			airport2 = airportList2[numberGen.nextInt(airportList2.length)];
		}

		Pair<String, String> airports = new Pair<String, String>(airport1, airport2);
		return airports;

	}

}
