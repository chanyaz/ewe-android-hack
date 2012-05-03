package com.expedia.bookings.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightTrip;
import com.mobiata.android.net.JsonResponseHandler;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightSearchResponseHandler extends JsonResponseHandler<FlightSearchResponse> {

	private FlightSearchResponse mResponse;

	private Map<String, FlightLeg> mLegs;

	private DateFormat mDateTimeFormat = new SimpleDateFormat("MMM d, y h:m:s a");

	@Override
	public FlightSearchResponse handleJson(JSONObject response) {
		mResponse = new FlightSearchResponse();
		mLegs = new HashMap<String, FlightLeg>();

		// TODO: Add error handling

		if (response.has("journeys")) {
			// Parse as a linear response
			parseJourneys(response.optJSONArray("journeys"));
		}
		else {
			// Parse as a matrix response
			List<FlightLeg> inboundLegs = parseLegs(response.optJSONArray("inboundLegs"));
			List<FlightLeg> outboundLegs = parseLegs(response.optJSONArray("outboundLegs"));

			for (FlightLeg leg : inboundLegs) {
				mLegs.put(leg.getLegId(), leg);
			}
			for (FlightLeg leg : outboundLegs) {
				mLegs.put(leg.getLegId(), leg);
			}

			parsePricingInfoArray(response.optJSONArray("pricingInformation"));
		}

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
			addDateTime(departure, segmentJson.optString("departureTime"));

			// Parse arrival
			Waypoint arrival = segment.mDestination = new Waypoint(Waypoint.ACTION_ARRIVAL);
			arrival.mAirportCode = segmentJson.optString("arrivalAirportCode");
			addDateTime(arrival, segmentJson.optString("arrivalTime"));

			leg.addSegment(segment);
		}

		return leg;
	}

	private void addDateTime(Waypoint waypoint, String dateTime) {
		// For now, we're just reformatting the date times as strings that match FlightStats
		// This is very inefficient and should eventually be fixed
		try {
			String dateStr = DateTimeUtils.formatFlightStatsDateTime(mDateTimeFormat.parse(dateTime));
			waypoint.addDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_UNKNOWN, dateStr);
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void parsePricingInfoArray(JSONArray pricingJson) {
		int len = pricingJson.length();
		for (int a = 0; a < len; a++) {
			FlightTrip trip = parseTrip(pricingJson.optJSONObject(a));
			mResponse.addTrip(trip);
		}
	}

	private FlightTrip parseTrip(JSONObject tripJson) {
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

		// If we're parsing as a matrix response, get the inbound/outbound legs
		if (mLegs.size() > 0) {
			trip.addLeg(getLeg(tripJson.optString("inboundLegId")));
			trip.addLeg(getLeg(tripJson.optString("outboundLegId")));
		}

		return trip;
	}

	private FlightLeg getLeg(String legId) {
		// return mLegs.get(legId);

		// TODO: Implement without random once we have leg ids that match up 
		return getRandomLeg();
	}

	// FOR TESTING PURPOSES ONLY

	public FlightLeg getRandomLeg() {
		String[] keys = mLegs.keySet().toArray(new String[0]);
		String key = keys[(int) Math.floor(Math.random() * keys.length)];
		return mLegs.get(key);
	}
}
