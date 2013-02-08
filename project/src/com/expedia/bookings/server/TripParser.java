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
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.Car.Category;
import com.expedia.bookings.data.Car.Type;
import com.expedia.bookings.data.CarVendor;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.trips.BookingStatus;
import com.expedia.bookings.data.trips.FlightConfirmation;
import com.expedia.bookings.data.trips.Insurance;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.Trip.TimePeriod;
import com.expedia.bookings.data.trips.TripActivity;
import com.expedia.bookings.data.trips.TripCar;
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

	private Map<String, TripWaypoint> mWaypoints;

	public Trip parseTrip(JSONObject tripJson) {
		Trip trip = new Trip();
		trip.setTripId(tripJson.optString("tripId"));
		trip.setTripNumber(tripJson.optString("tripNumber"));
		trip.setTitle(tripJson.optString("title"));
		trip.setDescription(tripJson.optString("description"));
		trip.setDetailsUrl(tripJson.optString("webDetailsURL"));
		trip.setStartDate(parseDateTime(tripJson.opt("startTime")));
		trip.setEndDate(parseDateTime(tripJson.opt("endTime")));

		trip.setBookingStatus(parseBookingStatus(tripJson.optString("bookingStatus")));
		trip.setTimePeriod(parseTimePeriod(tripJson.optString("timePeriod")));

		// Parse waypoints (used for flights parsing, if flights details response)
		JSONArray waypoints = tripJson.optJSONArray("waypoints");
		if (waypoints != null) {
			mWaypoints = new HashMap<String, TripWaypoint>();
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

		// Parse cars
		JSONArray cars = tripJson.optJSONArray("cars");
		if (cars != null) {
			for (int b = 0; b < cars.length(); b++) {
				trip.addTripComponent(parseTripCar(cars.optJSONObject(b)));
			}
		}

		// Parse activities
		JSONArray activities = tripJson.optJSONArray("activities");
		if (activities != null) {
			for (int b = 0; b < activities.length(); b++) {
				trip.addTripComponent(parseTripActivity(activities.optJSONObject(b)));
			}
		}

		// TODO: Parse cruises (once those are available to be parsed)

		//Parse insurance
		JSONArray insurance = tripJson.optJSONArray("insurance");
		if (insurance != null) {
			for (int b = 0; b < insurance.length(); b++) {
				trip.addInsurance(parseTripInsurance(insurance.optJSONObject(b)));
			}
		}

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

			JSONObject checkInStartTimePolicyJson = propertyJson.optJSONObject("checkInStartTimePolicy");
			if (checkInStartTimePolicyJson != null) {
				hotel.setCheckInTime(checkInStartTimePolicyJson.optString("value"));
			}

			property.setLocalPhone(propertyJson.optString("localPhone"));
			property.setTollFreePhone(propertyJson.optString("tollFreePhone"));

			hotel.setProperty(property);
		}

		int guests = 0;
		JSONArray roomsJson = obj.optJSONArray("rooms");
		if (roomsJson != null) {
			for (int i = 0; i < roomsJson.length(); i++) {
				JSONObject room = roomsJson.optJSONObject(i);
				property.setDescriptionText(room.optString("roomRatePlanDescription"));

				JSONObject roomPreferences = room.optJSONObject("roomPreferences");
				JSONObject otherOccupantInfo = roomPreferences.optJSONObject("otherOccupantInfo");
				guests += otherOccupantInfo.optInt("adultCount");
				guests += otherOccupantInfo.optInt("childAndInfantCount");
			}
		}

		hotel.setGuests(guests);

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

		//Parse confirmations
		JSONArray confirmationArr = obj.optJSONArray("confirmationNumbers");
		for (int a = 0; a < confirmationArr.length(); a++) {
			JSONObject confJson = confirmationArr.optJSONObject(a);
			if (confJson != null) {
				FlightConfirmation conf = new FlightConfirmation();
				conf.setCarrier(confJson.optString("airlineName"));
				conf.setConfirmationCode(confJson.optString("number"));
				flight.addConfirmation(conf);
			}
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

				TripWaypoint origin = mWaypoints.get(segmentJson.opt("departureWaypointId"));
				TripWaypoint destination = mWaypoints.get(segmentJson.opt("arrivalWaypointId"));
				segment.mOrigin = convertTripWaypointForFlight(origin);
				segment.mDestination = convertTripWaypointForFlight(destination);

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

	private TripCar parseTripCar(JSONObject obj) {
		TripCar tripCar = new TripCar();

		parseTripCommon(obj, tripCar);

		if (obj.has("uniqueID")) {
			Car car = new Car();

			car.setId(obj.optString("uniqueID", null));
			car.setConfNumber(obj.optString("confirmationNumber", null));

			JSONObject priceJson = obj.optJSONObject("price");
			car.setPrice(ParserUtils.createMoney(priceJson.optString("base", null),
					priceJson.optString("currency", null)));

			addTripWaypointToCar(mWaypoints.get(obj.optString("pickupWaypointId")), car);
			addTripWaypointToCar(mWaypoints.get(obj.optString("dropoffWaypointId")), car);

			JSONObject vendorJson = obj.optJSONObject("carVendor");
			CarVendor vendor = new CarVendor();
			vendor.setCode(vendorJson.optString("code", null));
			vendor.setShortName(vendorJson.optString("shortName", null));
			vendor.setLongName(vendorJson.optString("longName", null));
			vendor.setLogo(new Media(vendorJson.optString("logoURL", null)));
			car.setVendor(vendor);

			car.setCategoryImage(new Media(obj.optString("carCategoryImageURL")));
			car.setCategory(parseCarCategory(obj.optString("carCategory")));

			car.setType(parseCarType(obj.optString("carType")));

			tripCar.setCar(car);
		}

		return tripCar;
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

	private Insurance parseTripInsurance(JSONObject obj) {
		Insurance retVal = new Insurance();

		if (obj.has("name")) {
			retVal.setPolicyName(obj.optString("name", null));
			retVal.setTermsUrl(obj.optString("termsURL", null));
			retVal.setInsuranceLineOfBusiness(obj.optString("lineOfBusiness", ""));
		}

		return retVal;
	}

	private void parseTripCommon(JSONObject obj, TripComponent component) {
		component.setBookingStatus(parseBookingStatus(obj.optString("bookingStatus")));
	}

	private Car.Category parseCarCategory(String category) {
		if (TextUtils.isEmpty(category)) {
			return null;
		}

		if (category.equals("Mini")) {
			return Category.MINI;
		}
		else if (category.equals("Economy")) {
			return Category.ECONOMY;
		}
		else if (category.equals("Compact")) {
			return Category.COMPACT;
		}
		else if (category.equals("Midsize")) {
			return Category.MIDSIZE;
		}
		else if (category.equals("Standard")) {
			return Category.STANDARD;
		}
		else if (category.equals("Fullsize")) {
			return Category.FULLSIZE;
		}
		else if (category.equals("Premium")) {
			return Category.PREMIUM;
		}
		else if (category.equals("Luxury")) {
			return Category.LUXURY;
		}
		else if (category.equals("Special")) {
			return Category.SPECIAL;
		}
		else if (category.equals("Mini Elite")) {
			return Category.MINI_ELITE;
		}
		else if (category.equals("Economy Elite")) {
			return Category.ECONOMY_ELITE;
		}
		else if (category.equals("Compact Elite")) {
			return Category.COMPACT_ELITE;
		}
		else if (category.equals("Midsize Elite")) {
			return Category.MIDSIZE_ELITE;
		}
		else if (category.equals("Standard Elite")) {
			return Category.STANDARD_ELITE;
		}
		else if (category.equals("Fullsize Elite")) {
			return Category.FULLSIZE_ELITE;
		}
		else if (category.equals("Premium Elite")) {
			return Category.PREMIUM_ELITE;
		}
		else if (category.equals("Luxury Elite")) {
			return Category.LUXURY_ELITE;
		}
		else if (category.equals("Oversize")) {
			return Category.OVERSIZE;
		}

		return null;
	}

	private Car.Type parseCarType(String type) {
		if (TextUtils.isEmpty(type)) {
			return null;
		}

		if (type.equals("2/4Door Car")) {
			return Type.TWO_DOOR_CAR;
		}
		else if (type.equals("2/3Door Car")) {
			return Type.THREE_DOOR_CAR;
		}
		else if (type.equals("4/5Door Car")) {
			return Type.FOUR_DOOR_CAR;
		}
		else if (type.equals("Van")) {
			return Type.VAN;
		}
		else if (type.equals("Wagon")) {
			return Type.WAGON;
		}
		else if (type.equals("Limousine")) {
			return Type.LIMOUSINE;
		}
		else if (type.equals("Recreational Vehicle")) {
			return Type.RECREATIONAL_VEHICLE;
		}
		else if (type.equals("Convertible")) {
			return Type.CONVERTIBLE;
		}
		else if (type.equals("SportsCar")) {
			return Type.SPORTS_CAR;
		}
		else if (type.equals("SUV")) {
			return Type.SUV;
		}
		else if (type.equals("Pickup Regular Cab")) {
			return Type.PICKUP_REGULAR_CAB;
		}
		else if (type.equals("Open Air All-Terrain")) {
			return Type.OPEN_AIR_ALL_TERRAIN;
		}
		else if (type.equals("Special")) {
			return Type.SPECIAL;
		}
		else if (type.equals("Commercial Van/Truck")) {
			return Type.COMMERCIAL_VAN_TRUCK;
		}
		else if (type.equals("Pickup Extended Cab")) {
			return Type.PICKUP_EXTENDED_CAB;
		}
		else if (type.equals("Special Offer Car")) {
			return Type.SPECIAL_OFFER_CAR;
		}
		else if (type.equals("Coupe")) {
			return Type.COUPE;
		}
		else if (type.equals("Monospace")) {
			return Type.MONOSPACE;
		}
		else if (type.equals("Motorhome")) {
			return Type.MOTORHOME;
		}
		else if (type.equals("2 Wheel Vehicle")) {
			return Type.TWO_WHEEL_VEHICLE;
		}
		else if (type.equals("Roadster")) {
			return Type.ROADSTER;
		}
		else if (type.equals("Crossover")) {
			return Type.CROSSOVER;
		}

		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Internal Waypoint class
	//
	// This is temporary; it will be removed from the API soon, but in the
	// meantime we have to deal with it.

	private static class TripWaypoint {
		private String mType;

		private DateTime mTime;

		private Location mLocation;

		// For flights only
		private String mAirportCode;
		private String mAirportTerminal;
	}

	public void parseWaypoint(JSONObject obj) {
		TripWaypoint waypoint = new TripWaypoint();

		waypoint.mType = obj.optString("type");

		JSONObject timeJson = obj.optJSONObject("time");
		waypoint.mTime = new DateTime(timeJson.optLong("epochSeconds") * 1000, timeJson.optInt("timeZoneOffsetSeconds"));

		JSONObject locationJson = obj.optJSONObject("location");
		if (locationJson != null) {
			waypoint.mAirportCode = locationJson.optString("airportCode", null);
			waypoint.mAirportTerminal = locationJson.optString("airportTerminal", null);

			Location location = new Location();
			location.setLatitude(locationJson.optDouble("latitude", 0));
			location.setLongitude(locationJson.optDouble("longitude", 0));
			location.setCity(locationJson.optString("city", null));
			location.setCountryCode(locationJson.optString("countryCode", null));

			String fullAddress = locationJson.optString("fullAddress", null);
			if (!TextUtils.isEmpty(fullAddress)) {
				location.addStreetAddressLine(fullAddress);
			}

			waypoint.mLocation = location;
		}

		mWaypoints.put(obj.optString("id"), waypoint);
	}

	private Waypoint convertTripWaypointForFlight(TripWaypoint tripWaypoint) {
		Waypoint waypoint = new Waypoint(tripWaypoint.mType.equals("FLIGHT_DEPARTURE") ? Waypoint.F_DEPARTURE
				: Waypoint.F_ARRIVAL);

		waypoint.addDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED,
				tripWaypoint.mTime.getMillisFromEpoch(), tripWaypoint.mTime.getTzOffsetMillis());

		waypoint.mAirportCode = tripWaypoint.mAirportCode;
		waypoint.setTerminal(tripWaypoint.mAirportTerminal);

		return waypoint;
	}

	private void addTripWaypointToCar(TripWaypoint tripWaypoint, Car car) {
		if (tripWaypoint.mType.equals("CAR_PICKUP")) {
			car.setPickupDateTime(tripWaypoint.mTime);
			car.setPickupLocation(tripWaypoint.mLocation);
		}
		else {
			car.setDropoffDateTime(tripWaypoint.mTime);
			car.setDropoffLocation(tripWaypoint.mLocation);
		}
	}
}
