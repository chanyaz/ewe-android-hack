package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.FlightHistogram;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.mobiata.android.Log;

public class FlightSearchHistogramResponseHandler extends JsonResponseHandler<FlightSearchHistogramResponse> {

	// http://deals.expedia.fr/beta/stats/flights.json?tripTo=LHR&tripFrom=CDG&pos=fr

	@Override
	public FlightSearchHistogramResponse handleJson(JSONObject response) {
		FlightSearchHistogramResponse histogramResponse = new FlightSearchHistogramResponse();

		try {
			JSONObject histogramJson = response.getJSONObject("histogram");
			JSONArray entries = histogramJson.getJSONArray("entries");

			List<FlightHistogram> histograms = new ArrayList<FlightHistogram>();
			for (int i = 0; i < entries.length(); i++) {
				JSONObject entryJson = entries.getJSONObject(i);
				FlightHistogram histogram = new FlightHistogram();

				histogram.setDate(LocalDate.parse(entryJson.getString("key")));
				histogram.setCount(entryJson.getInt("count"));

				// TODO better money parsing once API sends currency information
				String minStr = entryJson.getString("min");
				if (minStr.contains(".")) {
					minStr = minStr.split("\\.", 0)[0];
				}
				histogram.setPriceAsStr(minStr);

				histograms.add(histogram);
			}

			histogramResponse.setFlightHistograms(histograms);
		}
		catch (JSONException e) {
			Log.e("Unable to parse Flight Search Histogram response", e);
		}

		return histogramResponse;
	}
}
