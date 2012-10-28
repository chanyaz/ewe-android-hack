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
import com.expedia.bookings.data.FlightSegmentAttributes;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.net.JsonResponseHandler;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Waypoint;

public class FlightSearchResponseHandler extends JsonResponseHandler<FlightSearchResponse> {

	private Context mContext;

	private FlightSearchResponse mResponse;

	private Map<String, FlightLeg> mLegs;

	// We split this out and weight towards the name of the airline
	// when it's not an operating airline.
	private Map<String, String> mAirlineNames;
	private Map<String, String> mOperatingAirlineNames;

	public FlightSearchResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public FlightSearchResponse handleJson(JSONObject response) {
		long start = System.nanoTime();

		mResponse = new FlightSearchResponse();
		mLegs = new HashMap<String, FlightLeg>();
		mAirlineNames = new HashMap<String, String>();
		mOperatingAirlineNames = new HashMap<String, String>();

		ParserUtils.logActivityId(response);

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

		// Parse each individual leg
		JSONArray legsJson = response.optJSONArray("legs");
		int len = legsJson.length();
		for (int a = 0; a < len; a++) {
			FlightLeg leg = parseLeg(legsJson.optJSONObject(a));
			mLegs.put(leg.getLegId(), leg);
		}

		// Parse offers and associate them with legs
		JSONArray offersJson = response.optJSONArray("offers");
		len = offersJson.length();
		for (int a = 0; a < len; a++) {
			JSONObject tripJson = offersJson.optJSONObject(a);
			FlightTrip trip = parseTrip(tripJson);

			if (tripJson.has("legIds")) {
				JSONArray legIdsJson = tripJson.optJSONArray("legIds");
				int len2 = legIdsJson.length();
				for (int b = 0; b < len2; b++) {
					trip.addLeg(mLegs.get(legIdsJson.optString(b)));
				}
			}

			mResponse.addTrip(trip);
		}

		// Parse the searchCities
		JSONArray searchCities = response.optJSONArray("searchCities");
		if (searchCities != null) {
			for (int a = 0; a < searchCities.length(); a++) {
				JSONObject cityJson = searchCities.optJSONObject(a);
				Location location = new Location();
				location.setCity(cityJson.optString("city"));
				location.setStateCode(cityJson.optString("province"));
				location.setDestinationId(cityJson.optString("code"));
				mResponse.addSearchCity(location);
			}
		}

		// Put in all airline names, weighting towards non-operating names
		mOperatingAirlineNames.putAll(mAirlineNames);
		mResponse.setAirlineNames(mOperatingAirlineNames);

		Log.d("Flight search response parse time: " + ((System.nanoTime() - start) / 1000000) + " ms");

		return mResponse;
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

			if (!mAirlineNames.containsKey(flightCode.mAirlineCode)) {
				mAirlineNames.put(flightCode.mAirlineCode, segmentJson.optString("airlineName"));
			}

			// Parse possible operating flight code
			if (segmentJson.has("operatingAirlineCode")) {
				FlightCode opFlightCode = new FlightCode();
				opFlightCode.mAirlineCode = segmentJson.optString("operatingAirlineCode");
				// Note: The operating airline # will always be empty string, because we
				// don't get that data from the API.  However, we still parse this deprecated
				// string in case API ever gets this functionality back again.
				opFlightCode.mNumber = segmentJson.optString("operatingAirlineFlightNumber");
				segment.addFlightCode(opFlightCode, Flight.F_OPERATING_AIRLINE_CODE);

				if (!mOperatingAirlineNames.containsKey(opFlightCode.mAirlineCode)) {
					mOperatingAirlineNames
							.put(opFlightCode.mAirlineCode, segmentJson.optString("operatingAirlineName"));
				}
			}

			// Parse departure
			Waypoint departure = segment.mOrigin = new Waypoint(Waypoint.ACTION_DEPARTURE);
			departure.mAirportCode = segmentJson.optString("departureAirportCode");
			departure.addDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_UNKNOWN,
					segmentJson.optLong("departureTimeEpochSeconds") * 1000,
					segmentJson.optInt("departureTimeZoneOffsetSeconds") * 1000);

			// Parse arrival
			Waypoint arrival = segment.mDestination = new Waypoint(Waypoint.ACTION_ARRIVAL);
			arrival.mAirportCode = segmentJson.optString("arrivalAirportCode");
			arrival.addDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_UNKNOWN,
					segmentJson.optLong("arrivalTimeEpochSeconds") * 1000,
					segmentJson.optInt("arrivalTimeZoneOffsetSeconds") * 1000);

			// Add a default status code
			segment.mStatusCode = Flight.STATUS_SCHEDULED;

			// Parse some other flight info
			if (segmentJson.has("distance") && segmentJson.has("distanceUnits")) {
				int distance = segmentJson.optInt("distance");
				String distanceUnits = segmentJson.optString("distanceUnits");
				if (!distanceUnits.equals("miles")) {
					// Need to do this since I don't know what other values are possible.
					throw new RuntimeException(
							"DEVELOPER FIX THIS: Parser does not yet handle non-miles distanceUnits.  Got: "
									+ distanceUnits);
				}

				// TODO: Convert from other units to miles here

				segment.mDistanceToTravel = distance;
			}
			else {
				segment.mDistanceToTravel = 0;
			}

			segment.mAircraftType = segmentJson.optString("equipmentDescription");

			leg.addSegment(segment);
		}

		return leg;
	}

	public static FlightTrip parseTrip(JSONObject tripJson) {
		FlightTrip trip = new FlightTrip();
		trip.setProductKey(tripJson.optString("productKey"));

		// If it has rates, parse those as well
		if (tripJson.has("currency")) {
			String currencyCode = tripJson.optString("currency");

			trip.setBaseFare(ParserUtils.createMoney(tripJson.optString("baseFare"), currencyCode));
			trip.setTotalFare(ParserUtils.createMoney(tripJson.optString("totalFare"), currencyCode));
			trip.setTaxes(ParserUtils.createMoney(tripJson.optString("taxes"), currencyCode));
			trip.setFees(ParserUtils.createMoney(tripJson.optString("fees"), currencyCode));
		}

		trip.setSeatsRemaining(tripJson.optInt("seatsRemaining"));

		if (tripJson.has("segmentAttributes")) {
			JSONArray legArr = tripJson.optJSONArray("segmentAttributes");
			for (int a = 0; a < legArr.length(); a++) {
				List<FlightSegmentAttributes> attrs = new ArrayList<FlightSegmentAttributes>();
				JSONArray segAttrs = legArr.optJSONArray(a);
				for (int b = 0; b < segAttrs.length(); b++) {
					attrs.add(JSONUtils.getJSONable(segAttrs, b, FlightSegmentAttributes.class));
				}
				trip.addFlightSegmentAttributes(attrs);
			}
		}

		return trip;
	}
}
