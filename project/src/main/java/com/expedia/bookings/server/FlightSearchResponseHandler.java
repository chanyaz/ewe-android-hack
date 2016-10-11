package com.expedia.bookings.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightSegmentAttributes;
import com.expedia.bookings.data.FlightSegmentAttributes.CabinCode;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.PassengerCategoryPrice;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.enums.PassengerCategory;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Waypoint;

import okhttp3.Response;

/**
 * An E3 flight search response parser.
 * <p/>
 * While no longer used for flight search responses per se, parts of it are still used
 * for parsing other aspects which are repeated across the app (like flight trips).
 */
public class FlightSearchResponseHandler extends JsonResponseHandler<FlightSearchResponse> {

	private FlightSearchResponse mResponse;

	private Map<String, FlightLeg> mLegs;

	// We split this out and weight towards the name of the airline
	// when it's not an operating airline.
	private Map<String, String> mAirlineNames;
	private Map<String, String> mOperatingAirlineNames;

	@Override
	public FlightSearchResponse handleResponse(Response response) throws IOException {
		long start = System.nanoTime();
		super.handleResponse(response);
		Log.d("Flight search response parse time: " + ((System.nanoTime() - start) / 1000000) + " ms; # trips="
			+ mResponse.getTripCount() + ", # legs=" + mLegs.size());
		return mResponse;
	}

	@Override
	public FlightSearchResponse handleJson(JSONObject response) {
		mResponse = new FlightSearchResponse();
		mLegs = new HashMap<String, FlightLeg>();
		mAirlineNames = new HashMap<String, String>();
		mOperatingAirlineNames = new HashMap<String, String>();

		ParserUtils.logActivityId(response);

		// Handle errors
		try {
			mResponse.addErrors(ParserUtils.parseErrors(ApiMethod.FLIGHT_SEARCH, response));
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
		mResponse.setObFeesDetails(response.optString("obFeesDetails", null));

		mResponse.compact();

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
			if (segmentJson.has("operatingAirlineName")) {
				FlightCode opFlightCode = new FlightCode();

				// Note: The operating airline # will always be empty string, because we
				// don't get that data from the API.  However, we still parse this deprecated
				// string in case API ever gets this functionality back again.
				opFlightCode.mNumber = segmentJson.optString("operatingAirlineFlightNumber");

				if (segmentJson.has("operatingAirlineCode")) {
					opFlightCode.mAirlineCode = segmentJson.optString("operatingAirlineCode");

					if (!mOperatingAirlineNames.containsKey(opFlightCode.mAirlineCode)) {
						mOperatingAirlineNames.put(opFlightCode.mAirlineCode,
							segmentJson.optString("operatingAirlineName"));
					}
				}
				else {
					// F1246: We can sometimes *only* get operating airline name.  In that case, do special parsing
					opFlightCode.mAirlineName = segmentJson.optString("operatingAirlineName");
				}

				segment.addFlightCode(opFlightCode, Flight.F_OPERATING_AIRLINE_CODE);
			}

			// Parse departure
			segment.setOriginWaypoint(new Waypoint(Waypoint.ACTION_DEPARTURE));
			Waypoint departure = segment.getOriginWaypoint();
			departure.mAirportCode = segmentJson.optString("departureAirportCode");
			departure.addDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_UNKNOWN,
				segmentJson.optLong("departureTimeEpochSeconds") * 1000,
				segmentJson.optInt("departureTimeZoneOffsetSeconds") * 1000);

			// Parse arrival
			segment.setDestinationWaypoint(new Waypoint(Waypoint.ACTION_ARRIVAL));
			Waypoint arrival = segment.getDestinationWaypoint();
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
							+ distanceUnits
					);
				}

				// TODO: Convert from other units to miles here

				segment.mDistanceToTravel = distance;
			}
			else {
				segment.mDistanceToTravel = 0;
			}

			segment.mAircraftType = segmentJson.optString("equipmentDescription");
			segment.mOnTimePercentage = (float) segmentJson.optDouble("onTimePercentage", 0.0f);

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

			trip.setTotalPrice(new Money(tripJson.optJSONObject("totalPrice").optString("amount"), currencyCode));
			trip.setBaseFare(ParserUtils.createMoney(tripJson.optString("baseFare"), currencyCode));
			trip.setTotalFare(ParserUtils.createMoney(tripJson.optString("totalFare"), currencyCode));
			String avgTotalFare = tripJson.optJSONObject("averageTotalPricePerTicket").optString("amount");
			trip.setAverageTotalFare(ParserUtils.createMoney(avgTotalFare, currencyCode));
			trip.setTaxes(ParserUtils.createMoney(tripJson.optString("taxes"), currencyCode));
			trip.setFees(ParserUtils.createMoney(tripJson.optString("fees"), currencyCode));

			JSONArray passengerCategories = tripJson.optJSONArray("pricePerPassengerCategory");

			Money totalPrice;
			Money basePrice;
			Money taxesPrice;

			String totalPriceStr;
			String basePriceStr;
			String taxesPriceStr;
			for (int i = 0; i < passengerCategories.length(); i++) {
				JSONObject passengerJson = passengerCategories.optJSONObject(i);
				PassengerCategory category = Enum.valueOf(PassengerCategory.class, passengerJson.optString("passengerCategory"));

				totalPriceStr = passengerJson.optJSONObject("totalPrice").optString("amount");
				totalPrice = new Money(totalPriceStr, currencyCode);

				basePriceStr = passengerJson.optJSONObject("basePrice").optString("amount");
				basePrice = new Money(basePriceStr, currencyCode);

				taxesPriceStr = passengerJson.optJSONObject("taxesPrice").optString("amount");
				taxesPrice = new Money(taxesPriceStr, currencyCode);

				trip.addPassenger(new PassengerCategoryPrice(category, totalPrice, basePrice, taxesPrice));
			}
		}

		trip.setSeatsRemaining(tripJson.optInt("seatsRemaining"));
		trip.setMayChargeObFees(tripJson.optBoolean("mayChargeOBFees"));
		trip.setFareName(tripJson.optString("fareName"));

		if (tripJson.has("segmentAttributes")) {
			JSONArray legArr = tripJson.optJSONArray("segmentAttributes");
			int len = legArr.length();
			trip.initFlightSegmentAttributes(len);
			for (int a = 0; a < len; a++) {
				JSONArray segAttrs = legArr.optJSONArray(a);
				int subLen = segAttrs.length();
				FlightSegmentAttributes[] attrs = new FlightSegmentAttributes[subLen];
				for (int b = 0; b < subLen; b++) {
					JSONObject attrsJson = segAttrs.optJSONObject(b);
					String bookingCode = attrsJson.optString("bookingCode");
					String cabinCodeStr = attrsJson.optString("cabinCode");

					CabinCode cabinCode;
					if ("coach".equals(cabinCodeStr)) {
						cabinCode = CabinCode.COACH;
					}
					else if ("premium coach".equals(cabinCodeStr)) {
						cabinCode = CabinCode.PREMIUM_COACH;
					}
					else if ("business".equals(cabinCodeStr)) {
						cabinCode = CabinCode.BUSINESS;
					}
					else if ("first".equals(cabinCodeStr)) {
						cabinCode = CabinCode.FIRST;
					}
					else {
						throw new RuntimeException("Ran into unknown cabin code: " + cabinCodeStr);
					}

					attrs[b] = new FlightSegmentAttributes(bookingCode.charAt(0), cabinCode);
				}
				trip.addFlightSegmentAttributes(a, attrs);
			}
		}

		if (tripJson.has("splitFarePrice")) {
			JSONArray legSplitFarePrices = tripJson.optJSONArray("splitFarePrice");

			for (int i = 0; i < legSplitFarePrices.length(); i++) {

				JSONObject legSplitTicketFare = legSplitFarePrices.optJSONObject(i);
				String legId = legSplitTicketFare.optString("legId");
				JSONObject totalPrice = legSplitTicketFare.optJSONObject("totalPrice");
				String currencyCode = totalPrice.optString("currencyCode");
				FlightLeg flightLeg = new FlightLeg();
				flightLeg.setLegId(legId);

				flightLeg.setTotalFare(ParserUtils.createMoney(totalPrice.optString("amount"), currencyCode));

				trip.addLeg(flightLeg);
			}
		}
		trip.setSplitTicket(tripJson.optBoolean("isSplitTicket", false));

		return trip;
	}
}
