package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Waypoint;

public class FlightSearchResponseHandler extends JsonResponseHandler<FlightSearchResponse> {

	private Context mContext;

	private FlightSearchResponse mResponse;

	private Map<String, FlightLeg> mLegs;

	public FlightSearchResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public FlightSearchResponse handleJson(JSONObject response) {
		long start = System.currentTimeMillis();

		mResponse = new FlightSearchResponse();
		mLegs = new HashMap<String, FlightLeg>();

		// Handle errors
		try {
			mResponse.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.FLIGHT_SEARCH, response));
			if (!mResponse.isSuccess()) {
				return mResponse;
			}
		}
		catch (JSONException e) {
			Log.e("Error parsing flight search response JSON", e);
			return null;
		}

		if (response.has("journeys")) {
			// Parse as a linear response
			parseJourneys(response.optJSONArray("journeys"));
		}
		else {
			// Parse as a matrix response
			List<FlightLeg> legs = parseLegs(response.optJSONArray("legs"));

			for (FlightLeg leg : legs) {
				mLegs.put(leg.getLegId(), leg);
			}

			parsePricingInfoArray(response.optJSONArray("offers"));
		}

		Log.d("Flight search response parse time: " + (System.currentTimeMillis() - start) + " ms");

		return mResponse;
	}

	private void parseJourneys(JSONArray journeysJson) {
		int len = journeysJson.length();
		for (int a = 0; a < len; a++) {
			JSONObject tripJson = journeysJson.optJSONObject(a);
			FlightTrip trip = parseTrip(tripJson);

			List<FlightLeg> legs = parseLegs(tripJson.optJSONArray("legs"));
			for (FlightLeg leg : legs) {
				trip.addLeg(leg);
			}

			mResponse.addTrip(trip);
		}
	}

	private List<FlightLeg> parseLegs(JSONArray legsJson) {
		List<FlightLeg> legs = new ArrayList<FlightLeg>();
		int len = legsJson.length();
		for (int a = 0; a < len; a++) {
			FlightLeg leg = parseLeg(legsJson.optJSONObject(a));
			legs.add(leg);
		}

		return legs;
	}

	private FlightLeg parseLeg(JSONObject legJson) {
		FlightLeg leg = new FlightLeg();
		leg.setLegId(legJson.optString("legId"));

		JSONArray segmentsJson = legJson.optJSONArray("segments");
		int segmentsLen = segmentsJson.length();
		for (int b = 0; b < segmentsLen; b++) {
			Flight segment = new Flight();
			JSONObject segmentJson = segmentsJson.optJSONObject(b);

			// Parse primary flight code
			FlightCode flightCode = new FlightCode();
			flightCode.mAirlineCode = segmentJson.optString("airlineCode");
			flightCode.mNumber = segmentJson.optString("flightNumber");
			segment.addFlightCode(flightCode, Flight.F_PRIMARY_AIRLINE_CODE);

			// Parse departure
			Waypoint departure = segment.mOrigin = new Waypoint(Waypoint.ACTION_DEPARTURE);
			departure.mAirportCode = segmentJson.optString("departureAirportCode");
			addDateTime(departure, segmentJson.optLong("departureTimeEpochSeconds"),
					segmentJson.optInt("departureTimeZoneOffsetSeconds"));

			// Parse arrival
			Waypoint arrival = segment.mDestination = new Waypoint(Waypoint.ACTION_ARRIVAL);
			arrival.mAirportCode = segmentJson.optString("arrivalAirportCode");
			addDateTime(arrival, segmentJson.optLong("arrivalTimeEpochSeconds"),
					segmentJson.optInt("arrivalTimeZoneOffsetSeconds"));

			// Add a default status code
			segment.mStatusCode = Flight.STATUS_SCHEDULED;

			leg.addSegment(segment);
		}

		return leg;
	}

	private void addDateTime(Waypoint waypoint, long millisFromEpoch, int tzOffsetSeconds) {
		waypoint.addDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_UNKNOWN, millisFromEpoch,
				tzOffsetSeconds * 1000);
	}

	private void parsePricingInfoArray(JSONArray pricingJson) {
		int len = pricingJson.length();
		for (int a = 0; a < len; a++) {
			JSONObject tripJson = pricingJson.optJSONObject(a);
			FlightTrip trip = parseTrip(tripJson);

			// If we're parsing as a matrix response, get the legs
			if (tripJson.has("legIds")) {
				JSONArray legsJson = tripJson.optJSONArray("legIds");
				for (int b = 0; b < legsJson.length(); b++) {
					trip.addLeg(getLeg(legsJson.optString(b)));
				}
			}

			mResponse.addTrip(trip);
		}
	}

	public static FlightTrip parseTrip(JSONObject tripJson) {
		FlightTrip trip = new FlightTrip();
		trip.setProductKey(tripJson.optString("productKey"));

		// If it has rates, parse those as well
		if (tripJson.has("currency")) {
			String currencyCode = tripJson.optString("currency");

			trip.setBaseFare(ParserUtils.createMoney(tripJson.optDouble("baseFare"), currencyCode));
			trip.setTotalFare(ParserUtils.createMoney(tripJson.optDouble("totalFare"), currencyCode));
			trip.setTaxes(ParserUtils.createMoney(tripJson.optDouble("taxes"), currencyCode));
			trip.setFees(ParserUtils.createMoney(tripJson.optDouble("fees"), currencyCode));
		}

		trip.setSeatsRemaining(tripJson.optInt("seatsRemaining"));

		return trip;
	}

	private FlightLeg getLeg(String legId) {
		return mLegs.get(legId);
	}

	// FOR TESTING PURPOSES ONLY

	public FlightLeg getRandomLeg() {
		String[] keys = mLegs.keySet().toArray(new String[0]);
		String key = keys[(int) Math.floor(Math.random() * keys.length)];
		return mLegs.get(key);
	}
}
