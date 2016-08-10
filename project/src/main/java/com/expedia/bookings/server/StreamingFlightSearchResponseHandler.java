package com.expedia.bookings.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
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
import com.expedia.bookings.utils.ShopWithPointsFlightsUtil;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Waypoint;

import okhttp3.Response;

/**
 * A streaming flight search results parser.
 * <p/>
 * To avoid memory issues, use this!
 */
public class StreamingFlightSearchResponseHandler implements ResponseHandler<FlightSearchResponse> {

	private boolean mIsRelease = false;

	private FlightSearchResponse mResponse;

	private Map<String, FlightLeg> mLegs;

	// We split this out and weight towards the name of the airline
	// when it's not an operating airline.
	private Map<String, String> mAirlineNames;
	private Map<String, String> mOperatingAirlineNames;

	// We cache attribute parsing rows so we don't unnecessarily create a ton of new objects
	private List<List<FlightSegmentAttributes>> mAttributes = new ArrayList<List<FlightSegmentAttributes>>();

	public StreamingFlightSearchResponseHandler(Context context) {
		mIsRelease = BuildConfig.RELEASE;
	}

	@Override
	public FlightSearchResponse handleResponse(Response response) throws IOException {
		if (response == null) {
			return null;
		}

		if (Log.isLoggingEnabled()) {
			StringBuilder httpInfo = new StringBuilder();
			httpInfo.append("HTTP " + response.code());
			httpInfo.append("\n");
			httpInfo.append(response.headers().toString());
			Log.v(httpInfo.toString());
		}

		InputStream in = response.body().byteStream();
		String contentEncoding = response.headers().get("Content-Encoding");
		if (!TextUtils.isEmpty(contentEncoding) && "gzip".equalsIgnoreCase(contentEncoding)) {
			in = new GZIPInputStream(in);
		}

		return handleResponse(in);
	}

	// Split out just to make it easier to profile in tests
	public FlightSearchResponse handleResponse(InputStream in) throws IOException {
		long start = System.nanoTime();

		mResponse = new FlightSearchResponse();
		mLegs = new HashMap<String, FlightLeg>();
		mAirlineNames = new HashMap<String, String>();
		mOperatingAirlineNames = new HashMap<String, String>();

		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

		Log.d("Starting to read streaming flight search response...");

		readSearchResponse(reader);

		reader.close();

		Log.d("Streaming flight search response parse time: " + ((System.nanoTime() - start) / 1000000)
			+ " ms; # trips=" + mResponse.getTripCount() + ", # legs=" + mLegs.size());

		return mResponse;
	}

	/*
	 * Implementation note: It is assumed that "legs" appears before "offers" when parsing the JSON
	 *  If this is ever no longer the case, this code will need to be reworked
	 */
	private void readSearchResponse(JsonReader reader) throws IOException {
		String name;
		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			name = reader.nextName();

			if (name.equals("errors")) {
				ParserUtils.readServerErrors(reader, mResponse, ApiMethod.SEARCH_RESULTS);
			}
			else if (name.equals("legs")) {
				expectToken(reader, JsonToken.BEGIN_ARRAY);
				reader.beginArray();
				while (!reader.peek().equals(JsonToken.END_ARRAY)) {
					FlightLeg leg = readLeg(reader);
					mLegs.put(leg.getLegId(), leg);
				}
				reader.endArray();
			}
			else if (name.equals("offers")) {
				expectToken(reader, JsonToken.BEGIN_ARRAY);
				reader.beginArray();
				while (!reader.peek().equals(JsonToken.END_ARRAY)) {
					FlightTrip trip = readTrip(reader);
					mResponse.addTrip(trip);
				}
				reader.endArray();
			}
			else if (name.equals("searchCities")) {
				expectToken(reader, JsonToken.BEGIN_ARRAY);
				reader.beginArray();
				while (!reader.peek().equals(JsonToken.END_ARRAY)) {
					Location searchCity = readSearchCity(reader);
					mResponse.addSearchCity(searchCity);
				}
				reader.endArray();
			}
			else if (name.equals("obFeesDetails")) {
				mResponse.setObFeesDetails(reader.nextString());
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();
		// Put in all airline names, weighting towards non-operating names
		mOperatingAirlineNames.putAll(mAirlineNames);
		mResponse.setAirlineNames(mOperatingAirlineNames);

		// Compact the response
		mResponse.compact();
	}

	private FlightLeg readLeg(JsonReader reader) throws IOException {
		FlightLeg leg = new FlightLeg();

		String name;
		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			name = reader.nextName();

			if (name.equals("legId")) {
				leg.setLegId(reader.nextString());
			}
			else if (name.equals("segments")) {
				expectToken(reader, JsonToken.BEGIN_ARRAY);
				reader.beginArray();
				while (!reader.peek().equals(JsonToken.END_ARRAY)) {
					Flight segment = readSegment(reader);
					leg.addSegment(segment);
				}
				reader.endArray();
			}
			else if (name.equals("baggageFeesUrl")) {
				leg.setBaggageFeesUrl(reader.nextString());
			}
			else if (name.equals("fareType")) {
				leg.setFareType(reader.nextString());
			}
			else if (name.equals("freeCancellationBy")) {
				leg.setIsFreeCancellable(true);
				// 6130. Not parsing other data here. Just setting boolean to see if the leg is free cancellable.
				reader.skipValue();
			}
			else if (name.equals("hasBagFee")) {
				leg.setHasBagFee(reader.nextBoolean());
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return leg;
	}

	private Flight readSegment(JsonReader reader) throws IOException {
		Flight segment = new Flight();
		segment.setOriginWaypoint(new Waypoint(Waypoint.ACTION_DEPARTURE));
		Waypoint departure = segment.getOriginWaypoint();
		segment.setDestinationWaypoint(new Waypoint(Waypoint.ACTION_ARRIVAL));
		Waypoint arrival = segment.getDestinationWaypoint();
		FlightCode flightCode = new FlightCode();

		// Add a default status code
		segment.mStatusCode = Flight.STATUS_SCHEDULED;

		// Vars that we need to handle in conjuction later, after parsing the stream
		String airlineName = null;
		String opAirlineName = null;
		String opFlightNum = null;
		String opAirlineCode = null;
		long depTimeEpochSeconds = 0;
		int depTimeZoneOffsetSeconds = 0;
		long arrTimeEpochSeconds = 0;
		int arrTimeZoneOffsetSeconds = 0;

		String name;
		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			name = reader.nextName();

			if (name.equals("airlineCode")) {
				flightCode.mAirlineCode = reader.nextString();
			}
			else if (name.equals("flightNumber")) {
				flightCode.mNumber = reader.nextString();
			}
			else if (name.equals("airlineName")) {
				airlineName = reader.nextString();
			}
			else if (name.equals("operatingAirlineName")) {
				opAirlineName = reader.nextString();
			}
			else if (name.equals("operatingAirlineFlightNumber")) {
				opFlightNum = reader.nextString();
			}
			else if (name.equals("operatingAirlineCode")) {
				opAirlineCode = reader.nextString();
			}
			else if (name.equals("departureAirportCode")) {
				departure.mAirportCode = reader.nextString();
			}
			else if (name.equals("departureTimeEpochSeconds")) {
				depTimeEpochSeconds = reader.nextLong();
			}
			else if (name.equals("departureTimeZoneOffsetSeconds")) {
				depTimeZoneOffsetSeconds = reader.nextInt();
			}
			else if (name.equals("arrivalAirportCode")) {
				arrival.mAirportCode = reader.nextString();
			}
			else if (name.equals("arrivalTimeEpochSeconds")) {
				arrTimeEpochSeconds = reader.nextLong();
			}
			else if (name.equals("arrivalTimeZoneOffsetSeconds")) {
				arrTimeZoneOffsetSeconds = reader.nextInt();
			}
			else if (name.equals("distance")) {
				segment.mDistanceToTravel = reader.nextInt();
			}
			else if (name.equals("distanceUnits")) {
				String distanceUnits = reader.nextString();
				if (!TextUtils.isEmpty(distanceUnits) && !distanceUnits.equals("miles")) {
					// Need to do this since I don't know what other values are possible.  Later we can convert
					throw new RuntimeException("Parser does not yet handle non-miles distanceUnits.  Got: "
						+ distanceUnits);
				}
			}
			else if (name.equals("equipmentDescription")) {
				segment.mAircraftType = reader.nextString();
			}
			else if (name.equals("onTimePercentage")) {
				segment.mOnTimePercentage = Integer.parseInt(reader.nextString()) / 100.0f;
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		segment.addFlightCode(flightCode, Flight.F_PRIMARY_AIRLINE_CODE);

		if (!mAirlineNames.containsKey(flightCode.mAirlineCode)) {
			mAirlineNames.put(flightCode.mAirlineCode, airlineName);
		}

		// Parse possible operating flight code
		if (!TextUtils.isEmpty(opAirlineName)) {
			FlightCode opFlightCode = new FlightCode();

			// Note: The operating airline # will always be empty string, because we
			// don't get that data from the API.  However, we still parse this deprecated
			// string in case API ever gets this functionality back again.
			opFlightCode.mNumber = opFlightNum;

			if (!TextUtils.isEmpty(opAirlineCode)) {
				opFlightCode.mAirlineCode = opAirlineCode;

				if (!mOperatingAirlineNames.containsKey(opFlightCode.mAirlineCode)) {
					mOperatingAirlineNames.put(opFlightCode.mAirlineCode, opAirlineName);
				}
			}
			else {
				// F1246: We can sometimes *only* get operating airline name.  In that case, do special parsing
				opFlightCode.mAirlineName = opAirlineName;
			}

			segment.addFlightCode(opFlightCode, Flight.F_OPERATING_AIRLINE_CODE);
		}

		departure.addDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_UNKNOWN,
			depTimeEpochSeconds * 1000, depTimeZoneOffsetSeconds * 1000);

		arrival.addDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_UNKNOWN,
			arrTimeEpochSeconds * 1000, arrTimeZoneOffsetSeconds * 1000);

		return segment;
	}

	private FlightTrip readTrip(JsonReader reader) throws IOException {
		FlightTrip trip = new FlightTrip();

		// Vars that we need to handle in conjunction later, after parsing the stream
		String currencyCode = null;
		String baseFare = null;
		String totalFare = null;
		String avgTotalFare = null;
		String taxes = null;
		String fees = null;

		String name;
		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			name = reader.nextName();

			if (name.equals("productKey")) {
				trip.setProductKey(reader.nextString());
			}
			else if (name.equals("currency")) {
				currencyCode = reader.nextString();
			}
			else if (name.equals("baseFare")) {
				baseFare = reader.nextString();
			}
			else if (name.equals("totalFare")) {
				totalFare = reader.nextString();
			}
			else if (name.equals("taxes")) {
				taxes = reader.nextString();
			}
			else if (name.equals("fees")) {
				fees = reader.nextString();
			}
			else if (name.equals("seatsRemaining")) {
				trip.setSeatsRemaining(reader.nextInt());
			}
			else if (name.equals("mayChargeOBFees")) {
				trip.setMayChargeObFees(reader.nextBoolean());
			}
			else if (name.equals("fareName")) {
				trip.setFareName(reader.nextString());
			}
			else if (name.equals("segmentAttributes")) {
				trip.setFlightSegmentAttributes(readSegmentAttributesArray(reader));
			}
			else if (name.equals("legIds")) {
				expectToken(reader, JsonToken.BEGIN_ARRAY);
				reader.beginArray();
				while (!reader.peek().equals(JsonToken.END_ARRAY)) {
					String legId = reader.nextString();
					trip.addLeg(mLegs.get(legId));
				}
				reader.endArray();
			}
			else if (name.equals("pricePerPassengerCategory")) {
				expectToken(reader, JsonToken.BEGIN_ARRAY);
				reader.beginArray();
				while (!reader.peek().equals(JsonToken.END_ARRAY)) {
					trip.addPassenger(readPricePerPassengerCategory(reader, currencyCode));
				}
				reader.endArray();
			}
			else if (name.equals("averageTotalPricePerTicket")) {
				expectToken(reader, JsonToken.BEGIN_OBJECT);
				reader.beginObject();
				avgTotalFare = null;
				while (!reader.peek().equals(JsonToken.END_OBJECT)) {
					name = reader.nextName();
					if (name.equals("amount")) {
						avgTotalFare = reader.nextString();
					}
					else {
						reader.skipValue();
					}
				}
				reader.endObject();
			}
			else if (name.equals("isPassportNeeded")) {
				trip.setPassportNeeded(reader.nextBoolean());
			}
			else if (name.equals("isSplitTicket")) {
				trip.setSplitTicket(reader.nextBoolean());
			}
			else if (name.equals("loyaltyInfo")) {
				trip.setEarnInfo(ShopWithPointsFlightsUtil.getLoyaltyEarnInfo(reader));
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		if (!TextUtils.isEmpty(currencyCode)) {
			trip.setBaseFare(ParserUtils.createMoney(baseFare, currencyCode));
			Money totalFareMoney = ParserUtils.createMoney(totalFare, currencyCode);
			trip.setTotalFare(totalFareMoney);
			// Setting total price equal to total fare for search response.
			trip.setTotalPrice(totalFareMoney);
			trip.setAverageTotalFare(ParserUtils.createMoney(avgTotalFare, currencyCode));
			trip.setTaxes(ParserUtils.createMoney(taxes, currencyCode));
			trip.setFees(ParserUtils.createMoney(fees, currencyCode));
		}

		return trip;
	}


	// Due to the sheer number of times this is called (once per FlightTrip, which is up to 1600 rows)
	// we try to reduce object creation overhead by caching the dynamically-sized Lists.
	private FlightSegmentAttributes[][] readSegmentAttributesArray(JsonReader reader) throws IOException {
		int size = mAttributes.size();
		int index = 0;

		expectToken(reader, JsonToken.BEGIN_ARRAY);
		reader.beginArray();
		while (!reader.peek().equals(JsonToken.END_ARRAY)) {
			List<FlightSegmentAttributes> attrsRow;
			if (index == size) {
				attrsRow = new ArrayList<FlightSegmentAttributes>();
				mAttributes.add(attrsRow);
				size++;
			}
			else {
				attrsRow = mAttributes.get(index);
				attrsRow.clear();
			}

			expectToken(reader, JsonToken.BEGIN_ARRAY);
			reader.beginArray();
			while (!reader.peek().equals(JsonToken.END_ARRAY)) {
				attrsRow.add(readSegmentAttributes(reader));
			}
			reader.endArray();

			index++;
		}
		reader.endArray();

		// Convert to array
		FlightSegmentAttributes[][] attrArray = new FlightSegmentAttributes[index][];
		for (int a = 0; a < index; a++) {
			List<FlightSegmentAttributes> attrsRow = mAttributes.get(a);
			attrArray[a] = attrsRow.toArray(new FlightSegmentAttributes[attrsRow.size()]);
		}

		return attrArray;
	}

	private PassengerCategoryPrice readPricePerPassengerCategory(JsonReader reader, String currencyCode)
		throws IOException {
		PassengerCategory passengerCategory = null;
		Money totalPrice = new Money();
		Money basePrice = new Money();
		Money taxesPrice = new Money();

		String name;
		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			name = reader.nextName();

			if (name.equals("passengerCategory")) {
				passengerCategory = Enum.valueOf(PassengerCategory.class, reader.nextString());
			}
			else if (name.equals("totalPrice")) {
				totalPrice.setCurrency(currencyCode);
				totalPrice.setAmount(getRawAmount(reader));
			}
			else if (name.equals("basePrice")) {
				basePrice.setCurrency(currencyCode);
				basePrice.setAmount(getRawAmount(reader));
			}
			else if (name.equals("taxesPrice")) {
				taxesPrice.setCurrency(currencyCode);
				taxesPrice.setAmount(getRawAmount(reader));
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return new PassengerCategoryPrice(passengerCategory, totalPrice, basePrice, taxesPrice);
	}

	// The MobilePrice object does not include its currency type. Instead,
	// we can just grab the amount as a String, and format into a money object
	// as needed.

	private String getRawAmount(JsonReader reader) throws IOException {
		String amount = null;
		String name;
		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			name = reader.nextName();
			if (name.equals("amount")) {
				amount = reader.nextString();
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return amount;
	}

	private FlightSegmentAttributes readSegmentAttributes(JsonReader reader) throws IOException {
		char bookingCode = 0;
		CabinCode cabinCode = null;

		String name;
		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			name = reader.nextName();

			if (name.equals("bookingCode")) {
				bookingCode = reader.nextString().charAt(0);
			}
			else if (name.equals("cabinCode")) {
				// Because each cabin code has a unique first letter, we parse
				// based on that for speed.  If that ever becomes no longer the
				// case, we should switch back to a slower, equals() based parser
				char firstChar = reader.nextString().charAt(0);
				switch (firstChar) {
				case 'c': // "coach"
					cabinCode = CabinCode.COACH;
					break;
				case 'p': // "premium coach"
					cabinCode = CabinCode.PREMIUM_COACH;
					break;
				case 'b': // "business"
					cabinCode = CabinCode.BUSINESS;
					break;
				case 'f': // "first"
					cabinCode = CabinCode.FIRST;
					break;
				default:
					throw new RuntimeException("Ran into unknown cabin code: " + reader.nextString());
				}
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return new FlightSegmentAttributes(bookingCode, cabinCode);
	}

	private Location readSearchCity(JsonReader reader) throws IOException {
		Location location = new Location();

		String name;
		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			name = reader.nextName();

			if (name.equals("city")) {
				location.setCity(reader.nextString());
			}
			else if (name.equals("province")) {
				location.setStateCode(reader.nextString());
			}
			else if (name.equals("code")) {
				location.setDestinationId(reader.nextString());
			}
			else if (name.equals("searchType")) {
				location.setSearchType(reader.nextString());
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return location;
	}

	private void expectToken(JsonReader reader, JsonToken expectedToken) throws IOException {
		if (!reader.peek().equals(expectedToken)) {
			throw new RuntimeException("Expected " + expectedToken + ", got " + reader.peek());
		}
	}
}
