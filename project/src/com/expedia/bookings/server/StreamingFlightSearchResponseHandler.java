package com.expedia.bookings.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.Context;
import android.text.TextUtils;

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
import com.expedia.bookings.utils.LoggingInputStream;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Waypoint;
import com.squareup.okhttp.Response;

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
		mIsRelease = AndroidUtils.isRelease(context);
	}

	@Override
	public FlightSearchResponse handleResponse(Response response) throws IOException {
		if (response == null) {
			return null;
		}

		if (Log.isLoggingEnabled()) {
			StringBuilder httpInfo = new StringBuilder();
			httpInfo.append(response.statusLine());
			httpInfo.append("\n");
			httpInfo.append(response.headers().toString());
			Log.v(httpInfo.toString());
		}

		InputStream in = response.body().byteStream();
		String contentEncoding = response.headers().get("Content-Encoding");
		if (!TextUtils.isEmpty(contentEncoding) && "gzip".equalsIgnoreCase(contentEncoding)) {
			in = new GZIPInputStream(in);
		}

		if (!mIsRelease) {
			// Only wire this up on debug builds
			in = new LoggingInputStream(in);
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

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createJsonParser(in);

		Log.d("Starting to read streaming flight search response...");

		readSearchResponse(parser);

		parser.close();

		Log.d("Streaming flight search response parse time: " + ((System.nanoTime() - start) / 1000000)
			+ " ms; # trips=" + mResponse.getTripCount() + ", # legs=" + mLegs.size());

		return mResponse;
	}

	/*
	 * Implementation note: It is assumed that "legs" appears before "offers" when parsing the JSON
	 *  If this is ever no longer the case, this code will need to be reworked
	 */
	private void readSearchResponse(JsonParser parser) throws IOException {
		String name;
		expectToken(parser, JsonToken.START_OBJECT);
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			parser.nextToken();

			if (name.equals("errors")) {
				ParserUtils.readServerErrors(parser, mResponse, ApiMethod.SEARCH_RESULTS);
			}
			else if (name.equals("activityId")) {
				ParserUtils.logActivityId(parser.getText());
			}
			else if (name.equals("legs")) {
				expectToken(parser, JsonToken.START_ARRAY);
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					FlightLeg leg = readLeg(parser);
					mLegs.put(leg.getLegId(), leg);
				}
			}
			else if (name.equals("offers")) {
				expectToken(parser, JsonToken.START_ARRAY);
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					FlightTrip trip = readTrip(parser);
					mResponse.addTrip(trip);
				}
			}
			else if (name.equals("searchCities")) {
				expectToken(parser, JsonToken.START_ARRAY);
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					Location searchCity = readSearchCity(parser);
					mResponse.addSearchCity(searchCity);
				}
			}
			else if (name.equals("obFeesDetails")) {
				mResponse.setObFeesDetails(parser.getText());
			}
			else {
				parser.skipChildren();
			}
		}

		// Put in all airline names, weighting towards non-operating names
		mOperatingAirlineNames.putAll(mAirlineNames);
		mResponse.setAirlineNames(mOperatingAirlineNames);

		// Compact the response
		mResponse.compact();
	}

	private FlightLeg readLeg(JsonParser parser) throws IOException {
		FlightLeg leg = new FlightLeg();

		String name;
		expectToken(parser, JsonToken.START_OBJECT);
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			parser.nextToken();

			if (name.equals("legId")) {
				leg.setLegId(parser.getText());
			}
			else if (name.equals("segments")) {
				expectToken(parser, JsonToken.START_ARRAY);
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					Flight segment = readSegment(parser);
					leg.addSegment(segment);
				}
			}
			else {
				parser.skipChildren();
			}
		}

		return leg;
	}

	private Flight readSegment(JsonParser parser) throws IOException {
		Flight segment = new Flight();
		Waypoint departure = segment.mOrigin = new Waypoint(Waypoint.ACTION_DEPARTURE);
		Waypoint arrival = segment.mDestination = new Waypoint(Waypoint.ACTION_ARRIVAL);
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
		expectToken(parser, JsonToken.START_OBJECT);
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			parser.nextToken();

			if (name.equals("airlineCode")) {
				flightCode.mAirlineCode = parser.getText();
			}
			else if (name.equals("flightNumber")) {
				flightCode.mNumber = parser.getText();
			}
			else if (name.equals("airlineName")) {
				airlineName = parser.getText();
			}
			else if (name.equals("operatingAirlineName")) {
				opAirlineName = parser.getText();
			}
			else if (name.equals("operatingAirlineFlightNumber")) {
				opFlightNum = parser.getText();
			}
			else if (name.equals("operatingAirlineCode")) {
				opAirlineCode = parser.getText();
			}
			else if (name.equals("departureAirportCode")) {
				departure.mAirportCode = parser.getText();
			}
			else if (name.equals("departureTimeEpochSeconds")) {
				depTimeEpochSeconds = parser.getValueAsLong(0);
			}
			else if (name.equals("departureTimeZoneOffsetSeconds")) {
				depTimeZoneOffsetSeconds = parser.getValueAsInt(0);
			}
			else if (name.equals("arrivalAirportCode")) {
				arrival.mAirportCode = parser.getText();
			}
			else if (name.equals("arrivalTimeEpochSeconds")) {
				arrTimeEpochSeconds = parser.getValueAsLong(0);
			}
			else if (name.equals("arrivalTimeZoneOffsetSeconds")) {
				arrTimeZoneOffsetSeconds = parser.getValueAsInt(0);
			}
			else if (name.equals("distance")) {
				segment.mDistanceToTravel = parser.getValueAsInt(0);
			}
			else if (name.equals("distanceUnits")) {
				String distanceUnits = parser.getText();
				if (!TextUtils.isEmpty(distanceUnits) && !distanceUnits.equals("miles")) {
					// Need to do this since I don't know what other values are possible.  Later we can convert
					throw new RuntimeException("Parser does not yet handle non-miles distanceUnits.  Got: "
						+ distanceUnits);
				}
			}
			else if (name.equals("equipmentDescription")) {
				segment.mAircraftType = parser.getText();
			}
			else {
				parser.skipChildren();
			}
		}

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

	private FlightTrip readTrip(JsonParser parser) throws IOException {
		FlightTrip trip = new FlightTrip();

		// Vars that we need to handle in conjuction later, after parsing the stream
		String currencyCode = null;
		String baseFare = null;
		String totalFare = null;
		String taxes = null;
		String fees = null;

		String name;
		expectToken(parser, JsonToken.START_OBJECT);
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			parser.nextToken();
			if (name.equals("productKey")) {
				trip.setProductKey(parser.getText());
			}
			else if (name.equals("currency")) {
				currencyCode = parser.getText();
			}
			else if (name.equals("baseFare")) {
				baseFare = parser.getText();
			}
			else if (name.equals("totalFare")) {
				totalFare = parser.getText();
			}
			else if (name.equals("taxes")) {
				taxes = parser.getText();
			}
			else if (name.equals("fees")) {
				fees = parser.getText();
			}
			else if (name.equals("seatsRemaining")) {
				trip.setSeatsRemaining(parser.getValueAsInt(0));
			}
			else if (name.equals("baggageFeesUrl")) {
				trip.setBaggageFeesUrl(parser.getText());
			}
			else if (name.equals("mayChargeOBFees")) {
				trip.setMayChargeObFees(parser.getValueAsBoolean(false));
			}
			else if (name.equals("hasBagFee")) {
				trip.setHasBagFee(parser.getValueAsBoolean(false));
			}
			else if (name.equals("fareName")) {
				trip.setFareName(parser.getText());
			}
			else if (name.equals("segmentAttributes")) {
				trip.setFlightSegmentAttributes(readSegmentAttributesArray(parser));
			}
			else if (name.equals("legIds")) {
				expectToken(parser, JsonToken.START_ARRAY);
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					String legId = parser.getText();
					trip.addLeg(mLegs.get(legId));
				}
			}
			else if (name.equals("pricePerPassengerCategory")) {
				expectToken(parser, JsonToken.START_ARRAY);
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					trip.addPassenger(readPricePerPassengerCategory(parser, currencyCode));
				}
			}
			else {
				parser.skipChildren();
			}
		}

		if (!TextUtils.isEmpty(currencyCode)) {
			trip.setBaseFare(ParserUtils.createMoney(baseFare, currencyCode));
			trip.setTotalFare(ParserUtils.createMoney(totalFare, currencyCode));
			trip.setTaxes(ParserUtils.createMoney(taxes, currencyCode));
			trip.setFees(ParserUtils.createMoney(fees, currencyCode));
		}

		return trip;
	}

	// Due to the sheer number of times this is called (once per FlightTrip, which is up to 1600 rows)
	// we try to reduce object creation overhead by caching the dynamically-sized Lists.
	private FlightSegmentAttributes[][] readSegmentAttributesArray(JsonParser parser) throws IOException {
		int size = mAttributes.size();
		int index = 0;

		expectToken(parser, JsonToken.START_ARRAY);
		while (parser.nextToken() != JsonToken.END_ARRAY) {
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

			expectToken(parser, JsonToken.START_ARRAY);
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				attrsRow.add(readSegmentAttributes(parser));
			}

			index++;
		}

		// Convert to array
		FlightSegmentAttributes[][] attrArray = new FlightSegmentAttributes[index][];
		for (int a = 0; a < index; a++) {
			List<FlightSegmentAttributes> attrsRow = mAttributes.get(a);
			attrArray[a] = attrsRow.toArray(new FlightSegmentAttributes[attrsRow.size()]);
		}

		return attrArray;
	}

	private PassengerCategoryPrice readPricePerPassengerCategory(JsonParser parser, String currencyCode) throws IOException {
		PassengerCategory passengerCategory = null;
		Money totalPrice = new Money();
		Money basePrice = new Money();
		Money taxesPrice = new Money();

		String name;
		expectToken(parser, JsonToken.START_OBJECT);
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			parser.nextToken();
			if (name.equals("passengerCategory")) {
				passengerCategory = Enum.valueOf(PassengerCategory.class, parser.getText());
			}
			else if (name.equals("totalPrice")) {
				totalPrice.setCurrency(currencyCode);
				totalPrice.setAmount(getRawAmount(parser));
			}
			else if (name.equals("basePrice")) {
				basePrice.setCurrency(currencyCode);
				basePrice.setAmount(getRawAmount(parser));
			}
			else if (name.equals("taxesPrice")) {
				taxesPrice.setCurrency(currencyCode);
				taxesPrice.setAmount(getRawAmount(parser));
			}
			else {
				parser.skipChildren();
			}
		}
		return new PassengerCategoryPrice(passengerCategory, totalPrice, basePrice, taxesPrice);
	}

	// The MobilePrice object does not include its currency type. Instead,
	// we can just grab the amount as a String, and format into a money object
	// as needed.

	private String getRawAmount(JsonParser parser) throws IOException {
		String amount = null;
		String name;
		expectToken(parser, JsonToken.START_OBJECT);
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			parser.nextToken();
			name = parser.getCurrentName();
			if (name.equals("amount")) {
				amount = parser.getText();
			}
			else {
				parser.skipChildren();
			}
		}
		return amount;
	}

	private FlightSegmentAttributes readSegmentAttributes(JsonParser parser) throws IOException {
		char bookingCode = 0;
		CabinCode cabinCode = null;

		String name;
		expectToken(parser, JsonToken.START_OBJECT);
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			parser.nextToken();

			if (name.equals("bookingCode")) {
				bookingCode = parser.getTextCharacters()[0];
			}
			else if (name.equals("cabinCode")) {
				// Because each cabin code has a unique first letter, we parse
				// based on that for speed.  If that ever becomes no longer the
				// case, we should switch back to a slower, equals() based parser
				char firstChar = parser.getTextCharacters()[0];
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
					throw new RuntimeException("Ran into unknown cabin code: " + parser.getText());
				}
			}
			else {
				parser.skipChildren();
			}
		}

		return new FlightSegmentAttributes(bookingCode, cabinCode);
	}

	private Location readSearchCity(JsonParser parser) throws IOException {
		Location location = new Location();

		String name;
		expectToken(parser, JsonToken.START_OBJECT);
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			name = parser.getCurrentName();
			parser.nextToken();

			if (name.equals("city")) {
				location.setCity(parser.getText());
			}
			else if (name.equals("province")) {
				location.setStateCode(parser.getText());
			}
			else if (name.equals("code")) {
				location.setDestinationId(parser.getText());
			}
			else if (name.equals("searchType")) {
				location.setSearchType(parser.getText());
			}
			else {
				parser.skipChildren();
			}
		}

		return location;
	}

	private void expectToken(JsonParser parser, JsonToken expectedToken) throws IOException {
		if (parser.getCurrentToken() != expectedToken && parser.nextToken() != expectedToken) {
			throw new RuntimeException("Expected " + expectedToken + ", got " + parser.getCurrentToken());
		}
	}
}
