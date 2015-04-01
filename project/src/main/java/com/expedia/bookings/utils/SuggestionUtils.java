package com.expedia.bookings.utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;
import android.text.format.DateUtils;

import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionSort;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.server.ExpediaServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.IoUtils;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class SuggestionUtils {

	// We don't care about small airports
	// http://www.faa.gov/airports/airport_safety/part139_cert/?p1=classes
	private static final int AIRPORT_CLASSIFICATION_THRESHOLD = 3;

	private static final long MINIMUM_TIME_AGO = DateUtils.HOUR_IN_MILLIS;


	/**
	 * Retrieve nearby airports. Don't run on the UI thread.
	 *
	 * TODO make more configurable provide location from FusedProvider)
	 *
	 * @param context
	 * @param maxNumSuggestions - how many?
	 * @return
	 */
	public static List<SuggestionV2> getNearbyAirportSuggestions(Context context, int maxNumSuggestions) {
		List<SuggestionV2> airportSuggestions = new ArrayList<SuggestionV2>();

		long minTime = DateTime.now().getMillis() - MINIMUM_TIME_AGO;
		android.location.Location loc = LocationServices.getLastBestLocation(context, minTime);

		if (loc != null) {
			ExpediaServices expediaServices = new ExpediaServices(context);
			SuggestionResponse response = expediaServices.suggestionsAirportsNearby(loc.getLatitude(), loc.getLongitude(),
				SuggestionSort.POPULARITY);

			Airport airport;
			if (!response.hasErrors() && !response.getSuggestions().isEmpty()) {
				for (SuggestionV2 suggestion : response.getSuggestions()) {
					airport = FlightStatsDbUtils.getAirport(suggestion.getAirportCode());
					if (airport.mClassification <= AIRPORT_CLASSIFICATION_THRESHOLD) {
						airportSuggestions.add(suggestion);

						if (airportSuggestions.size() == maxNumSuggestions) {
							break;
						}
					}
				}
			}
		}

		return airportSuggestions;
	}

	public static void saveSuggestionHistory(final Context context, final ArrayList<Suggestion> recentSuggestions, final String file) {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Type type = new TypeToken<ArrayList<Suggestion>>() {
				}.getType();
				String suggestionJson = new Gson().toJson(recentSuggestions, type);
				try {
					IoUtils.writeStringToFile(file, suggestionJson, context);
				}
				catch (IOException e) {
					Log.e("Save History Error: ", e);
				}
			}
		})).start();
	}

	public static ArrayList<Suggestion> loadSuggestionHistory(final Context context, String file) {

		ArrayList<Suggestion> recentSuggestions = new ArrayList<Suggestion>();
		try {
			String str = IoUtils.readStringFromFile(file, context);
			Type type = new TypeToken<ArrayList<Suggestion>>() {
			}.getType();
			recentSuggestions = new Gson().fromJson(str, type);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return recentSuggestions;
	}
}
