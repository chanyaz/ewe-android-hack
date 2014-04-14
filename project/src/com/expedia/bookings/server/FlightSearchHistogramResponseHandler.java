package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.FlightHistogram;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.mobiata.android.Log;

public class FlightSearchHistogramResponseHandler extends JsonResponseHandler<FlightSearchHistogramResponse> {

	// http://deals.expedia.fr/beta/stats/flights.json?tripTo=LHR&tripFrom=CDG&pos=fr

	@Override
	public FlightSearchHistogramResponse handleJson(JSONObject response) {
		FlightSearchHistogramResponse histogramResponse = new FlightSearchHistogramResponse();

		try {

			JSONArray flightsJson = response.getJSONArray("flights");
			JSONObject histogramJson = response.getJSONObject("histogram");
			JSONArray entries = histogramJson.getJSONArray("entries");

			//We parse out the flights list and create FlightHistogram objects for each departureDate return date pair
			HashMap<String, HashMap<String, FlightHistogram>> returnFlightPrices = new HashMap<String, HashMap<String, FlightHistogram>>();
			for (int i = 0; i < flightsJson.length(); i++) {
				//Parse the values we care about
				JSONObject fjson = flightsJson.getJSONObject(i);
				String depDateStr = fjson.optString("departDate");
				String retDateStr = fjson.optString("returnDate");
				Double perPsgPrice = fjson.optDouble("perPsgrPrice", 0);
				if (!TextUtils.isEmpty(depDateStr) && !TextUtils.isEmpty(retDateStr) && perPsgPrice != 0) {
					//If we dont have any return date/histograms for this start date, lets add a new entry
					if (!returnFlightPrices.containsKey(depDateStr)) {
						returnFlightPrices.put(depDateStr, new HashMap<String, FlightHistogram>());
					}

					//If we dont yet have a histogram for this start and return date, lets add one
					if (!returnFlightPrices.get(depDateStr).containsKey(retDateStr)) {
						returnFlightPrices.get(depDateStr).put(retDateStr, new FlightHistogram());
					}

					//Update the Histogram stats
					FlightHistogram gram = returnFlightPrices.get(depDateStr).get(retDateStr);
					if (gram.getCount() == 0) {
						gram.setKeyDate(LocalDate.parse(retDateStr));
						gram.setPriceAsStr("" + perPsgPrice);
						gram.setMinPrice(perPsgPrice.floatValue());
						gram.setMaxPrice(perPsgPrice.floatValue());
					}
					else {
						if (perPsgPrice < gram.getMinPrice()) {
							gram.setMinPrice(perPsgPrice);
							gram.setPriceAsStr("" + perPsgPrice);
						}
						if (perPsgPrice > gram.getMaxPrice()) {
							gram.setMaxPrice(perPsgPrice.floatValue());
						}
					}
					gram.setCount(gram.getCount() + 1);
				}
			}

			//We add the histogram data to the response
			List<FlightHistogram> histograms = new ArrayList<FlightHistogram>();
			for (int i = 0; i < entries.length(); i++) {
				JSONObject entryJson = entries.getJSONObject(i);
				FlightHistogram histogram = new FlightHistogram();

				String dateKey = entryJson.getString("key");
				histogram.setReturnFlightDateHistograms(returnFlightPrices.get(dateKey));

				histogram.setKeyDate(LocalDate.parse(dateKey));
				histogram.setCount(entryJson.getInt("count"));

				// TODO better money parsing once API sends currency information
				String minStr = entryJson.getString("min");
				if (minStr.contains(".")) {
					minStr = minStr.split("\\.", 0)[0];
				}
				histogram.setPriceAsStr(minStr);

				histogram.setMinPrice(entryJson.getDouble("min"));

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
