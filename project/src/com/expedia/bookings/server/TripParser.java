package com.expedia.bookings.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.Activity;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.trips.BookingStatus;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.Trip.TimePeriod;
import com.expedia.bookings.data.trips.TripActivity;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.data.trips.TripHotel;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Waypoint;

/**
 * Common class for parsing trips, since the trip summary and trip details
 * responses are essentially the same (just with different amounts of
 * information).
 *
 */
public class TripParser {

	private Map<String, Waypoint> mWaypoints;

	public Trip parseTrip(JSONObject tripJson) {
		Trip trip = new Trip();
		trip.setTripId(tripJson.optString("tripId"));
		trip.setTripNumber(tripJson.optString("tripNumber"));
		trip.setTitle(tripJson.optString("title"));
		trip.setDescription(tripJson.optString("description"));
		trip.setStartDate(parseDateTime(tripJson.opt("startTime")));
		trip.setEndDate(parseDateTime(tripJson.opt("endTime")));

		trip.setBookingStatus(parseBookingStatus(tripJson.optString("bookingStatus")));
		trip.setTimePeriod(parseTimePeriod(tripJson.optString("timePeriod")));

		// Parse waypoints (used for flights parsing, if flights details response)
		JSONArray waypoints = tripJson.optJSONArray("waypoints");
		if (waypoints != null) {
			mWaypoints = new HashMap<String, Waypoint>();
			for (int b = 0; b < waypoints.length(); b++) {
				parseWaypoint(waypoints.optJSONObject(b));
			}
		}

		// Parse hotels
		JSONArray hotels = tripJson.optJSONArray("hotels");
		if (hotels != null) {
			for (int b = 0; b < hotels.length(); b++) {
				trip.addTripComponent(parseTripHotel(hotels.optJSONObject(b)));
			}
		}

		// Parse flights
		JSONArray flights = tripJson.optJSONArray("flights");
		if (flights != null) {
			for (int b = 0; b < flights.length(); b++) {
				trip.addTripComponent(parseTripFlight(flights.optJSONObject(b)));
			}
		}

		// Parse activities
		JSONArray activities = tripJson.optJSONArray("activities");
		if (activities != null) {
			for (int b = 0; b < activities.length(); b++) {
				trip.addTripComponent(parseTripActivity(activities.optJSONObject(b)));
			}
		}

		// TODO: Parse more component types?

		return trip;
	}

	private DateTime parseDateTime(Object obj) {
		if (obj == null) {
			return null;
		}
		else if (obj instanceof JSONObject) {
			JSONObject json = (JSONObject) obj;
			return new DateTime(json.optLong("epochSeconds") * 1000, json.optInt("timeZoneOffsetSeconds"));
		}
		else if (obj instanceof String) {
			// TODO: DELETE ONCE OBSELETE
			//
			// Parse with no timezone.  The current timezone code is buggy and we shouldn't
			// be using this code anyways.

			try {
				String str = (String) obj;
				Date date = DATE_FORMAT.parse(str);
				return new DateTime(date.getTime(), 0);
			}
			catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException("Could not parse date time: " + obj);
	}

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	private BookingStatus parseBookingStatus(String status) {
		if ("SAVED".equals(status)) {
			return BookingStatus.SAVED;
		}
		else if ("PENDING".equals(status)) {
			return BookingStatus.PENDING;
		}
		else if ("BOOKED".equals(status)) {
			return BookingStatus.BOOKED;
		}
		else if ("CANCELLED".equals(status)) {
			return BookingStatus.CANCELLED;
		}

		return null;
	}

	private TimePeriod parseTimePeriod(String period) {
		if ("UPCOMING".equals(period)) {
			return TimePeriod.UPCOMING;
		}
		else if ("INPROGRESS".equals(period)) {
			return TimePeriod.INPROGRESS;
		}
		else if ("COMPLETED".equals(period)) {
			return TimePeriod.COMPLETED;
		}

		return null;
	}

	private TripHotel parseTripHotel(JSONObject obj) {
		TripHotel hotel = new TripHotel();

		parseTripCommon(obj, hotel);

		hotel.setStartDate(parseDateTime(obj.opt("checkInDate")));
		hotel.setEndDate(parseDateTime(obj.opt("checkOutDate")));

		Property property = new Property();
		property.setPropertyId(obj.optString("hotelId"));

		JSONObject propertyJson = obj.optJSONObject("hotelPropertyInfo");
		if (propertyJson != null) {
			property.setName(propertyJson.optString("name", null));
			property.setThumbnail(new Media(propertyJson.optString("photoThumbnailURL", null)));

			JSONObject addressJson = propertyJson.optJSONObject("address");
			if (addressJson != null) {
				Location location = new Location();
				location.addStreetAddressLine(addressJson.optString("fullAddress", null));
				location.setCity(addressJson.optString("city", null));
				location.setStateCode(addressJson.optString("countrySubdivisionCode", null));
				location.setCountryCode(addressJson.optString("countryCode", null));
				location.setLatitude(propertyJson.optDouble("latitude"));
				location.setLongitude(propertyJson.optDouble("longitude"));
				property.setLocation(location);
			}

			hotel.setProperty(property);
		}

		return hotel;
	}

	private TripFlight parseTripFlight(JSONObject obj) {
		TripFlight flight = new TripFlight();

		parseTripCommon(obj, flight);

		if (obj.has("startTime") && obj.has("endTime")) {
			flight.setStartDate(parseDateTime(obj.opt("startTime")));
			flight.setEndDate(parseDateTime(obj.opt("endTime")));
		}
		else {
			flight.setStartDate(parseDateTime(obj.opt("startDate")));
			flight.setEndDate(parseDateTime(obj.opt("endDate")));
		}

		// We're taking a lack of legs info to mean that this is a non-details call;
		// short-circuit out of the info early
		if (!obj.has("legs")) {
			return flight;
		}

		FlightTrip flightTrip = new FlightTrip();
		flight.setFlightTrip(flightTrip);

		// Parse fares
		JSONObject fareTotalJson = obj.optJSONObject("fareTotal");
		String currency = fareTotalJson.optString("currency");
		flightTrip.setBaseFare(ParserUtils.createMoney(fareTotalJson.optString("base"), currency));
		flightTrip.setTaxes(ParserUtils.createMoney(fareTotalJson.optString("taxes"), currency));
		flightTrip.setTotalFare(ParserUtils.createMoney(fareTotalJson.optString("total"), currency));

		// Parse passengers
		JSONArray passengersArr = obj.optJSONArray("passengers");
		for (int a = 0; a < passengersArr.length(); a++) {
			JSONObject passengerJson = passengersArr.optJSONObject(a);

			Traveler traveler = new Traveler();
			traveler.setFirstName(passengerJson.optString("firstName"));
			traveler.setMiddleName(passengerJson.optString("middleName"));
			traveler.setLastName(passengerJson.optString("lastName"));

			String gender = passengerJson.optString("gender");
			if ("Male".equals(gender)) {
				traveler.setGender(Gender.MALE);
			}
			else if ("Female".equals(gender)) {
				traveler.setGender(Gender.FEMALE);
			}

			// For now, just parse the first phone number
			JSONArray phoneNumbersArr = passengerJson.optJSONArray("phoneNumbers");
			if (phoneNumbersArr != null && phoneNumbersArr.length() > 0) {
				JSONObject firstPhoneJson = phoneNumbersArr.optJSONObject(0);
				traveler.setPhoneCountryCode(firstPhoneJson.optString("countryCode"));
				traveler.setPhoneNumber(firstPhoneJson.optString("phone"));
			}

			flight.addTraveler(traveler);
		}

		// Parse the legs
		JSONArray legsArr = obj.optJSONArray("legs");
		for (int a = 0; a < legsArr.length(); a++) {
			JSONObject legJson = legsArr.optJSONObject(a);
			FlightLeg leg = new FlightLeg();

			JSONArray segmentsArr = legJson.optJSONArray("segments");
			for (int b = 0; b < segmentsArr.length(); b++) {
				JSONObject segmentJson = segmentsArr.optJSONObject(b);

				Flight segment = new Flight();

				segment.mOrigin = mWaypoints.get(segmentJson.opt("departureWaypointId"));
				segment.mDestination = mWaypoints.get(segmentJson.opt("arrivalWaypointId"));

				FlightCode flightCode = new FlightCode();
				flightCode.mAirlineCode = segmentJson.optString("airlineCode");
				flightCode.mNumber = segmentJson.optString("flightNumber");
				segment.addFlightCode(flightCode, Flight.F_PRIMARY_AIRLINE_CODE);

				String operatedBy = segmentJson.optString("operatedByAirCarrierName", null);
				if (!TextUtils.isEmpty(operatedBy)) {
					FlightCode opFlightCode = new FlightCode();
					opFlightCode.mAirlineName = operatedBy;
					segment.addFlightCode(opFlightCode, Flight.F_OPERATING_AIRLINE_CODE);
				}

				segment.mAircraftType = segmentJson.optString("equipmentDescription", null);

				// We assume all distances are in miles, throw a fit if that's not the case
				if (!"mi".equals(segmentJson.optString("distanceUnits"))) {
					throw new RuntimeException("Wasn't expecting non-miles unit");
				}

				segment.mDistanceToTravel = segmentJson.optInt("distance"); // Assumes "miles" here

				leg.addSegment(segment);
			}

			flightTrip.addLeg(leg);
		}

		return flight;
	}

	private void parseTripCommon(JSONObject obj, TripComponent component) {
		component.setBookingStatus(parseBookingStatus(obj.optString("bookingStatus")));
	}

	public void parseWaypoint(JSONObject obj) {
		Waypoint waypoint = new Waypoint(obj.optString("type").equals("FLIGHT_DEPARTURE") ? Waypoint.F_DEPARTURE
				: Waypoint.F_ARRIVAL);

		JSONObject timeJson = obj.optJSONObject("time");
		waypoint.addDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED,
				timeJson.optLong("epochSeconds") * 1000, timeJson.optInt("timeZoneOffsetSeconds"));

		JSONObject airportJson = obj.optJSONObject("location");
		waypoint.mAirportCode = airportJson.optString("airportCode");
		waypoint.setTerminal(obj.optString("airportTerminal"));

		mWaypoints.put(obj.optString("id"), waypoint);
	}

	private TripActivity parseTripActivity(JSONObject obj) {
		TripActivity tripActivity = new TripActivity();

		parseTripCommon(obj, tripActivity);

		if (obj.has("uniqueID")) {
			Activity activity = new Activity();

			activity.setId(obj.optString("uniqueID", null));
			activity.setTitle(obj.optString("activityTitle", null));
			activity.setDetailsUrl(obj.optString("activityDetailsURL", null));

			JSONObject priceJson = obj.optJSONObject("price");
			activity.setPrice(ParserUtils.createMoney(priceJson.optString("total", null),
					priceJson.optString("currency", null)));

			tripActivity.setActivity(activity);
		}

		return tripActivity;
	}
}
