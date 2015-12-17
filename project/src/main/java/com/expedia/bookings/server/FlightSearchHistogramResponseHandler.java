package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.expedia.bookings.data.FlightHistogram;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.mobiata.android.Log;

public class FlightSearchHistogramResponseHandler extends JsonResponseHandler<FlightSearchHistogramResponse> {

	// Example outgoing trip url:
	// http://deals.expedia.com/beta/stats/flights.json?tripTo=LAS&tripFrom=LAX
	//
	// Exmaple return trip url:
	// http://deals.expedia.com/beta/stats/flights.json?tripTo=LAS&tripFrom=LAX&departDate=2014-10-01&key=returnDate

	private boolean mDoApply8WeekRules;

	public FlightSearchHistogramResponseHandler(Location origin, Location destination, LocalDate departureDate) {
		mDoApply8WeekRules = departureDate == null;
	}

	@Override
	public FlightSearchHistogramResponse handleJson(JSONObject response) {
		FlightSearchHistogramResponse histogramResponse = new FlightSearchHistogramResponse();

		try {
			JSONObject histogramJson = response.getJSONObject("histogram");
			JSONArray entries = histogramJson.getJSONArray("entries");

			// We add the histogram data to the response
			String currency = histogramJson.optString("currency");
			List<FlightHistogram> histograms = new ArrayList<>();
			for (int i = 0; i < entries.length(); i++) {
				JSONObject entryJson = entries.getJSONObject(i);
				FlightHistogram histogram = new FlightHistogram();

				String dateKey = entryJson.getString("key");

				histogram.setKeyDate(LocalDate.parse(dateKey));
				histogram.setCount(entryJson.getInt("count"));

				Money minPrice = new Money();
				minPrice.setAmount(entryJson.getDouble("min"));
				minPrice.setCurrency(currency);
				histogram.setMinPrice(minPrice);

				Money maxPrice = new Money();
				maxPrice.setAmount(entryJson.getDouble("max"));
				maxPrice.setCurrency(currency);
				histogram.setMaxPrice(maxPrice);

				histograms.add(histogram);
			}

			histogramResponse.setFlightHistograms(histograms);

			// Follow rules here:
			// https://expedia.mingle.thoughtworks.com/projects/eb_ad_app/cards/3281

			// 1. Remove outliers
			histogramResponse.removeUpperOutliers();

			// 4. For the 'When to return' data, remove outliers and show all data points regardless of gaps
			if (mDoApply8WeekRules) {
				histogramResponse.apply8WeekFilter();
			}
		}
		catch (JSONException e) {
			Log.e("Unable to parse Flight Search Histogram response", e);
		}

		return histogramResponse;
	}
}
