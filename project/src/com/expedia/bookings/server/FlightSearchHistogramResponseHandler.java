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
import com.expedia.bookings.data.Money;
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
			HashMap<String, HashMap<String, FlightHistogram>> returnFlightPrices = new HashMap<>();
			for (int i = 0; i < flightsJson.length(); i++) {
				//Parse the values we care about
				JSONObject fjson = flightsJson.getJSONObject(i);
				String depDateStr = fjson.optString("departDate");
				String retDateStr = fjson.optString("returnDate");
				Double perPsgPrice = fjson.optDouble("perPsgrPrice", 0);
				String currency = fjson.optString("currency");

				Money perPsgMoney = new Money();
				perPsgMoney.setAmount(perPsgPrice);
				perPsgMoney.setCurrency(currency);

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
						gram.setMinPrice(perPsgMoney);
						gram.setMaxPrice(perPsgMoney);
					}
					else {
						if (perPsgMoney.compareTo(gram.getMinPrice()) < 0) {
							gram.setMinPrice(perPsgMoney);
						}
						if (perPsgMoney.compareTo(gram.getMaxPrice()) > 0) {
							gram.setMaxPrice(perPsgMoney);
						}
					}
					gram.setCount(gram.getCount() + 1);
				}
			}

			// We add the histogram data to the response
			String currency = histogramJson.optString("currency");
			List<FlightHistogram> histograms = new ArrayList<>();
			for (int i = 0; i < entries.length(); i++) {
				JSONObject entryJson = entries.getJSONObject(i);
				FlightHistogram histogram = new FlightHistogram();

				String dateKey = entryJson.getString("key");
				histogram.setReturnFlightDateHistograms(returnFlightPrices.get(dateKey));

				histogram.setKeyDate(LocalDate.parse(dateKey));
				histogram.setCount(entryJson.getInt("count"));

				Money minPrice = new Money();
				minPrice.setAmount(entryJson.getDouble("min"));
				minPrice.setCurrency(currency);
				histogram.setMinPrice(minPrice);

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
