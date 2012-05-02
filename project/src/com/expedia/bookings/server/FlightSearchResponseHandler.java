package com.expedia.bookings.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
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

		parseLegs(response.optJSONArray("inboundLegs"));
		parseLegs(response.optJSONArray("outboundLegs"));
		parsePricingInfo(response.optJSONArray("pricingInformation"));

		return mResponse;
	}

	private void parseLegs(JSONArray legsJson) {
		int len = legsJson.length();
		for (int a = 0; a < len; a++) {
			FlightLeg leg = new FlightLeg();
			JSONObject legJson = legsJson.optJSONObject(a);
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

			mLegs.put(leg.getLegId(), leg);
		}
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

	private void parsePricingInfo(JSONArray pricingJson) {
		int len = pricingJson.length();
		for (int a = 0; a < len; a++) {
			JSONObject tripJson = pricingJson.optJSONObject(a);
			FlightTrip trip = new FlightTrip();
			trip.setProductKey(tripJson.optString("productKey"));
			trip.setInboundLeg(getLeg(tripJson.optString("inboundLegId")));
			trip.setOutboundLeg(getLeg(tripJson.optString("outboundLegId")));

			// If it has rates, parse those as well
			if (tripJson.has("currency")) {
				String currencyCode = tripJson.optString("currency");

				trip.setBaseFare(ParserUtils.createMoney(tripJson.optDouble("baseFare"), currencyCode));
				trip.setTotalFare(ParserUtils.createMoney(tripJson.optDouble("totalFare"), currencyCode));
				trip.setTaxes(ParserUtils.createMoney(tripJson.optDouble("taxes"), currencyCode));
				trip.setFees(ParserUtils.createMoney(tripJson.optDouble("fees"), currencyCode));
			}

			mResponse.addTrip(trip);
		}
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
