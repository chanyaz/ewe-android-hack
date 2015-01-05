package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.Activity;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.Car.Category;
import com.expedia.bookings.data.Car.Type;
import com.expedia.bookings.data.CarVendor;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.trips.BookingStatus;
import com.expedia.bookings.data.trips.CustomerSupport;
import com.expedia.bookings.data.trips.FlightConfirmation;
import com.expedia.bookings.data.trips.Insurance;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.Trip.LevelOfDetail;
import com.expedia.bookings.data.trips.Trip.TimePeriod;
import com.expedia.bookings.data.trips.TripActivity;
import com.expedia.bookings.data.trips.TripCar;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripCruise;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.data.trips.TripPackage;
import com.mobiata.android.Log;
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

	private LevelOfDetail mLevelOfDetail;

	public Trip parseTrip(JSONObject tripJson) {
		Trip trip = new Trip();
		String levelOfDetail = tripJson.optString("levelOfDetail", null);
		if ("FULL".equals(levelOfDetail)) {
			trip.setLevelOfDetail(LevelOfDetail.FULL);
		}
		else if ("SUMMARY_FALLBACK".equals(levelOfDetail)) {
			trip.setLevelOfDetail(LevelOfDetail.SUMMARY_FALLBACK);
		}
		else {
			trip.setLevelOfDetail(LevelOfDetail.SUMMARY);
		}

		mLevelOfDetail = trip.getLevelOfDetail();

		trip.setTripId(tripJson.optString("tripId"));
		trip.setTripNumber(tripJson.optString("tripNumber"));
		trip.setTitle(tripJson.optString("title"));
		trip.setDescription(tripJson.optString("description"));
		trip.setDetailsUrl(tripJson.optString("webDetailsURL"));
		trip.setStartDate(DateTimeParser.parseDateTime(tripJson.opt("startTime")));
		trip.setEndDate(DateTimeParser.parseDateTime(tripJson.opt("endTime")));

		trip.setBookingStatus(parseBookingStatus(tripJson.optString("bookingStatus")));
		trip.setTimePeriod(parseTimePeriod(tripJson.optString("timePeriod")));

		/*
		 *  The api returns the sharableUrl in the form of /api/trips/shared. But this is NOT the link that is to be shared to any users.
		 *  We will instead replace it with /m/trips/shared, so that if the user clicks on the link without the app installed it will get them to the mobileWeb's shared itin landing page.
		 */
		trip.getShareInfo().setSharableDetailsUrl(tripJson.optString("sharableDetailsURL").replace("/api/", "/m/"));

		trip.setCustomerSupport(parseCustomerSupport(tripJson.optJSONObject("customerSupport")));

		trip.addTripComponents(parseTripComponents(tripJson));

		// Parse insurance
		JSONArray insurance = tripJson.optJSONArray("insurance");
		if (insurance != null) {
			for (int b = 0; b < insurance.length(); b++) {
				trip.addInsurance(parseTripInsurance(insurance.optJSONObject(b)));
			}
		}

		return trip;
	}

	private List<TripComponent> parseTripComponents(JSONObject obj) {
		List<TripComponent> tripComponents = new ArrayList<TripComponent>();

		tripComponents.addAll(parseType(obj, "activities", TripComponent.Type.ACTIVITY));
		tripComponents.addAll(parseType(obj, "cars", TripComponent.Type.CAR));
		tripComponents.addAll(parseType(obj, "cruises", TripComponent.Type.CRUISE));
		tripComponents.addAll(parseType(obj, "flights", TripComponent.Type.FLIGHT));
		tripComponents.addAll(parseType(obj, "hotels", TripComponent.Type.HOTEL));
		tripComponents.addAll(parseType(obj, "packages", TripComponent.Type.PACKAGE));

		return tripComponents;
	}

	private List<TripComponent> parseType(JSONObject obj, String key, TripComponent.Type type) {
		List<TripComponent> tripComponents = new ArrayList<TripComponent>();

		JSONArray arr = obj.optJSONArray(key);
		if (arr != null) {
			for (int a = 0; a < arr.length(); a++) {
				JSONObject componentJson = arr.optJSONObject(a);
				TripComponent component = null;

				switch (type) {
				case ACTIVITY:
					component = parseTripActivity(componentJson);
					break;
				case CAR:
					component = parseTripCar(componentJson);
					break;
				case CRUISE:
					component = parseTripCruise(componentJson);
					break;
				case FLIGHT:
					component = parseTripFlight(componentJson);
					break;
				case HOTEL:
					component = parseTripHotel(componentJson);
					break;
				case PACKAGE:
					component = parseTripPackage(componentJson);
					break;
				default:
					component = null;
					break;
				}

				if (component != null && !BookingStatus.filterOut(component.getBookingStatus())) {
					tripComponents.add(component);
				}
			}
		}

		return tripComponents;
	}

	private CustomerSupport parseCustomerSupport(JSONObject customerSupportJson) {
		CustomerSupport support = new CustomerSupport();
		if (customerSupportJson != null) {
			support.setSupportUrl(customerSupportJson.optString("customerSupportURL"));
			support.setSupportPhoneInfo(customerSupportJson.optString("customerSupportPhoneInfo"));
			support.setSupportPhoneNumberDomestic(customerSupportJson.optString("customerSupportPhoneNumberDomestic"));
			support.setSupportPhoneNumberInternational(customerSupportJson
					.optString("customerSupportPhoneNumberInternational"));
		}
		return support;
	}

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
		hotel.getShareInfo().setSharableDetailsUrl(obj.optString("sharableItemDetailURL").replace("/api/", "/m/"));

		if (obj.has("checkInDateTime") && obj.has("checkOutDateTime")) {
			hotel.setStartDate(DateTimeParser.parseDateTime(obj.opt("checkInDateTime")));
			hotel.setEndDate(DateTimeParser.parseDateTime(obj.opt("checkOutDateTime")));
		}
		else {
			// Old version of code, kept because I'm not sure which servers support newer version yet
			hotel.setStartDate(DateTimeParser.parseDateTime(obj.opt("checkInDate")));
			hotel.setEndDate(DateTimeParser.parseDateTime(obj.opt("checkOutDate")));
		}

		Property property = new Property();
		property.setPropertyId(obj.optString("hotelId"));
		property.setInfoSiteUrl(obj.optString("infositeURL"));

		JSONObject propertyJson = obj.optJSONObject("hotelPropertyInfo");
		if (propertyJson != null) {
			property.setName(propertyJson.optString("name", null));
			property.setThumbnail(ParserUtils.parseUrl(propertyJson.optString("photoThumbnailURL")));

			JSONObject addressJson = propertyJson.optJSONObject("address");
			if (addressJson != null) {
				Location location = parseHotelLocation(propertyJson, addressJson);
				property.setLocation(location);
			}

			JSONObject checkInStartTimePolicyJson = propertyJson.optJSONObject("checkInStartTimePolicy");
			if (checkInStartTimePolicyJson != null) {
				hotel.setCheckInTime(checkInStartTimePolicyJson.optString("value"));
			}

			JSONObject checkOutTimePolicyJson = propertyJson.optJSONObject("checkOutTimePolicy");
			if (checkOutTimePolicyJson != null) {
				hotel.setCheckOutTime(checkOutTimePolicyJson.optString("value"));
			}

			property.setLocalPhone(propertyJson.optString("localPhone"));
			property.setTollFreePhone(propertyJson.optString("tollFreePhone"));

			double starRating = propertyJson.optDouble("starRating", 0);
			property.setHotelRating(starRating / 10);

			hotel.setProperty(property);
		}

		int guests = 0;
		Traveler primaryTraveler = null;
		JSONArray roomsJson = obj.optJSONArray("rooms");
		if (roomsJson != null) {
			for (int i = 0; i < roomsJson.length(); i++) {
				JSONObject room = roomsJson.optJSONObject(i);
				String conf = room.optString("hotelConfirmationNumber");
				if (!TextUtils.isEmpty(conf)) {
					hotel.addConfirmationNumber(conf);
				}

				property.setItinRoomType(room.optString("roomRatePlanDescription"));

				JSONObject roomPreferences = room.optJSONObject("roomPreferences");
				if (roomPreferences != null) {
					JSONObject otherOccupantInfo = roomPreferences.optJSONObject("otherOccupantInfo");
					if (otherOccupantInfo != null) {
						guests += otherOccupantInfo.optInt("adultCount");
						guests += otherOccupantInfo.optInt("childAndInfantCount");
					}

					JSONObject occupantSelectedRoomOptions = roomPreferences
							.optJSONObject("occupantSelectedRoomOptions");
					if (occupantSelectedRoomOptions != null) {
						property.setItinBedType(occupantSelectedRoomOptions.optString("bedTypeName"));
					}
					// Used only when importing a shared Itin
					JSONObject primaryOccupantInfo = roomPreferences.optJSONObject("primaryOccupant");
					if (primaryOccupantInfo != null) {
						primaryTraveler = new Traveler();
						primaryTraveler.setFirstName(primaryOccupantInfo.optString("firstName"));
						primaryTraveler.setFullName(primaryOccupantInfo.optString("fullName"));
					}
				}
			}
		}

		hotel.setGuests(guests);
		hotel.setPrimaryTraveler(primaryTraveler);

		return hotel;
	}

	private TripFlight parseTripFlight(JSONObject obj) {
		TripFlight flight = new TripFlight();

		parseTripCommon(obj, flight);

		if (obj.has("startTime") && obj.has("endTime")) {
			flight.setStartDate(DateTimeParser.parseDateTime(obj.opt("startTime")));
			flight.setEndDate(DateTimeParser.parseDateTime(obj.opt("endTime")));
		}
		else {
			flight.setStartDate(DateTimeParser.parseDateTime(obj.opt("startDate")));
			flight.setEndDate(DateTimeParser.parseDateTime(obj.opt("endDate")));
		}

		// We're taking a lack of legs info to mean that this is a non-details call;
		// short-circuit out of the info early
		if (!obj.has("legs")) {
			return flight;
		}

		//Parse confirmations
		JSONArray confirmationArr = obj.optJSONArray("confirmationNumbers");
		if (confirmationArr != null) {
			for (int a = 0; a < confirmationArr.length(); a++) {
				JSONObject confJson = confirmationArr.optJSONObject(a);
				if (confJson != null) {
					FlightConfirmation conf = new FlightConfirmation();
					conf.setCarrier(confJson.optString("airlineName"));
					conf.setConfirmationCode(confJson.optString("number"));
					flight.addConfirmation(conf);
				}
			}
		}

		FlightTrip flightTrip = new FlightTrip();
		flight.setFlightTrip(flightTrip);

		// Parse fares
		JSONObject fareTotalJson = obj.optJSONObject("fareTotal");
		if (fareTotalJson != null) {
			String currency = fareTotalJson.optString("currency");
			flightTrip.setBaseFare(ParserUtils.createMoney(fareTotalJson.optString("base"), currency));
			flightTrip.setTaxes(ParserUtils.createMoney(fareTotalJson.optString("taxes"), currency));
			flightTrip.setTotalFare(ParserUtils.createMoney(fareTotalJson.optString("total"), currency));
		}

		// Parse passengers
		JSONArray passengersArr = obj.optJSONArray("passengers");
		for (int a = 0; a < passengersArr.length(); a++) {
			flight.addTraveler(parseTraveler(passengersArr.optJSONObject(a)));
		}

		// Parse the legs
		JSONArray legsArr = obj.optJSONArray("legs");
		for (int a = 0; a < legsArr.length(); a++) {
			JSONObject legJson = legsArr.optJSONObject(a);
			FlightLeg leg = new FlightLeg();

			// Fetch the sharable link at the flight leg level.
			leg.getShareInfo().setSharableDetailsUrl(legJson.optString("sharableFlightLegURL").replace("/api/", "/m/"));

			JSONArray segmentsArr = legJson.optJSONArray("segments");
			for (int b = 0; b < segmentsArr.length(); b++) {
				JSONObject segmentJson = segmentsArr.optJSONObject(b);

				Flight segment = new Flight();

				//required for flight map
				segment.mStatusCode = Flight.STATUS_UNKNOWN;

				segment.setOriginWaypoint(parseWaypoint(segmentJson, Waypoint.F_DEPARTURE));
				segment.setDestinationWaypoint(parseWaypoint(segmentJson, Waypoint.F_ARRIVAL));

				FlightCode flightCode = new FlightCode();
				flightCode.mAirlineCode = segmentJson.optString("externalAirlineCode");
				flightCode.mNumber = segmentJson.optString("flightNumber").trim();
				segment.addFlightCode(flightCode, Flight.F_PRIMARY_AIRLINE_CODE);

				String operatedBy = segmentJson.optString("operatedByAirCarrierName", null);
				if (!TextUtils.isEmpty(operatedBy)) {
					FlightCode opFlightCode = new FlightCode();
					opFlightCode.mAirlineName = operatedBy;
					segment.addFlightCode(opFlightCode, Flight.F_OPERATING_AIRLINE_CODE);
				}

				segment.mAircraftType = segmentJson.optString("equipmentDescription", null);

				// We assume all distances are in miles, throw a fit if that's not the case
				String distanceUnits = segmentJson.optString("distanceUnits");
				DistanceUnit unit = DistanceUnit.MILES; // Assume miles by default
				if ("km".equals(distanceUnits)) {
					unit = DistanceUnit.KILOMETERS;
				}
				else if (!"mi".equals(distanceUnits)) {
					Log.w("Did not get a distance unit we recognize - what is a \"" + distanceUnits + "\"");
				}
				Distance distance = new Distance(segmentJson.optInt("distance"), unit);

				segment.mDistanceToTravel = (int) Math.round(distance.getDistance(DistanceUnit.MILES));

				leg.addSegment(segment);
			}

			flightTrip.addLeg(leg);
		}

		return flight;
	}

	private TripCar parseTripCar(JSONObject obj) {
		TripCar tripCar = new TripCar();

		parseTripCommon(obj, tripCar);

		tripCar.setStartDate(DateTimeParser.parseDateTime(obj.optJSONObject("pickupTime")));
		tripCar.setEndDate(DateTimeParser.parseDateTime(obj.optJSONObject("dropOffTime")));

		if (obj.has("uniqueID")) {
			Car car = new Car();

			car.setId(obj.optString("uniqueID", null));
			car.setConfNumber(obj.optString("confirmationNumber", null));

			JSONObject priceJson = obj.optJSONObject("price");
			if (priceJson != null) {
				car.setPrice(ParserUtils.createMoney(priceJson.optString("base", null),
						priceJson.optString("currency", null)));
			}

			car.setPickUpDateTime(DateTimeParser.parseDateTime(obj.optJSONObject("pickupTime")));
			car.setDropOffDateTime(DateTimeParser.parseDateTime(obj.optJSONObject("dropOffTime")));

			car.setPickUpLocation(parseCarLocation(obj.optJSONObject("pickupLocation")));
			car.setDropOffLocation(parseCarLocation(obj.optJSONObject("dropOffLocation")));

			JSONObject vendorJson = obj.optJSONObject("carVendor");
			CarVendor vendor = new CarVendor();
			vendor.setCode(vendorJson.optString("code", null));
			vendor.setShortName(vendorJson.optString("shortName", null));
			vendor.setLongName(vendorJson.optString("longName", null));
			vendor.setLogo(ParserUtils.parseUrl(vendorJson.optString("logoURL")));
			vendor.setTollFreePhone(vendorJson.optString("phoneNumber"));
			vendor.setLocalPhone(vendorJson.optString("localPhoneNumber"));
			car.setVendor(vendor);

			car.setCategoryImage(ParserUtils.parseUrl(obj.optString("carCategoryImageURL")));
			car.setCategory(parseCarCategory(obj.optString("carCategory")));

			car.setType(parseCarType(obj.optString("carType")));

			tripCar.setCar(car);
		}

		return tripCar;
	}

	private TripCruise parseTripCruise(JSONObject obj) {
		TripCruise tripCruise = new TripCruise();

		parseTripCommon(obj, tripCruise);

		tripCruise.setStartDate(DateTimeParser.parseDateTime(obj.optJSONObject("startTime")));
		tripCruise.setEndDate(DateTimeParser.parseDateTime(obj.optJSONObject("endTime")));

		return tripCruise;
	}

	private TripActivity parseTripActivity(JSONObject obj) {
		TripActivity tripActivity = new TripActivity();

		parseTripCommon(obj, tripActivity);

		tripActivity.setStartDate(DateTimeParser.parseDateTime(obj.optJSONObject("startTime")));
		tripActivity.setEndDate(DateTimeParser.parseDateTime(obj.optJSONObject("endTime")));

		if (obj.has("uniqueID")) {
			Activity activity = new Activity();

			activity.setId(obj.optString("uniqueID", null));
			activity.setTitle(obj.optString("activityTitle", null));
			activity.setDetailsUrl(obj.optString("activityDetailsURL", null));
			activity.setGuestCount(obj.optInt("travelerCount"));
			activity.setVoucherPrintUrl(obj.optString("voucherPrintURL"));

			JSONObject priceJson = obj.optJSONObject("price");
			if (priceJson != null) {
				activity.setPrice(ParserUtils.createMoney(priceJson.optString("total", null),
						priceJson.optString("currency", null)));
			}

			// Parse travelers
			JSONArray travelersArr = obj.optJSONArray("travelers");
			for (int i = 0; i < travelersArr.length(); i++) {
				activity.addTraveler(parseTraveler(travelersArr.optJSONObject(i)));
			}

			tripActivity.setActivity(activity);
		}

		return tripActivity;
	}

	private TripPackage parseTripPackage(JSONObject obj) {
		TripPackage tripPackage = new TripPackage();

		parseTripCommon(obj, tripPackage);

		JSONObject priceJson = obj.optJSONObject("price");
		if (priceJson != null) {
			tripPackage
					.setTotal(ParserUtils.createMoney(priceJson.optString("total"), priceJson.optString("currency")));
		}

		tripPackage.addTripComponents(parseTripComponents(obj));

		return tripPackage;
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

	private Traveler parseTraveler(JSONObject obj) {
		Traveler traveler = new Traveler();
		traveler.setFirstName(obj.optString("firstName"));
		traveler.setMiddleName(obj.optString("middleName"));
		traveler.setLastName(obj.optString("lastName"));
		traveler.setFullName(obj.optString("fullName"));
		traveler.setAge(obj.optInt("age"));

		String gender = obj.optString("gender");
		if ("Male".equals(gender)) {
			traveler.setGender(Gender.MALE);
		}
		else if ("Female".equals(gender)) {
			traveler.setGender(Gender.FEMALE);
		}

		// For now, just parse the first phone number
		JSONArray phoneNumbersArr = obj.optJSONArray("phoneNumbers");
		if (phoneNumbersArr != null && phoneNumbersArr.length() > 0) {
			JSONObject firstPhoneJson = phoneNumbersArr.optJSONObject(0);
			traveler.setPhoneCountryCode(firstPhoneJson.optString("countryCode"));
			traveler.setPhoneNumber(firstPhoneJson.optString("phone"));
		}

		traveler.setIsRedeemer(obj.optBoolean("isRedeemer"));

		return traveler;
	}

	private void parseTripCommon(JSONObject obj, TripComponent component) {
		component.setBookingStatus(parseBookingStatus(obj.optString("bookingStatus")));

		// If it doesn't have a unique id (for whatever dumb reason), generate our own (so we at least
		// can key off of it)
		String uniqueId = obj.optString("uniqueID", null);
		if (TextUtils.isEmpty(uniqueId)) {
			if (mLevelOfDetail == LevelOfDetail.FULL) {
				// Only log if we were expecting an id
				Log.w("No unique ID on trip component: " + obj.toString());
			}
			uniqueId = UUID.randomUUID().toString();
		}

		component.setUniqueId(uniqueId);

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
		else if (category.equals("MiniElite")) {
			return Category.MINI_ELITE;
		}
		else if (category.equals("EconomyElite")) {
			return Category.ECONOMY_ELITE;
		}
		else if (category.equals("CompactElite")) {
			return Category.COMPACT_ELITE;
		}
		else if (category.equals("MidsizeElite")) {
			return Category.MIDSIZE_ELITE;
		}
		else if (category.equals("StandardElite")) {
			return Category.STANDARD_ELITE;
		}
		else if (category.equals("FullsizeElite")) {
			return Category.FULLSIZE_ELITE;
		}
		else if (category.equals("PremiumElite")) {
			return Category.PREMIUM_ELITE;
		}
		else if (category.equals("LuxuryElite")) {
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

		if (type.equals("TwoDoorCar")) {
			return Type.TWO_DOOR_CAR;
		}
		else if (type.equals("ThreeDoorCar")) {
			return Type.THREE_DOOR_CAR;
		}
		else if (type.equals("FourDoorCar")) {
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
		else if (type.equals("RecreationalVehicle")) {
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
		else if (type.equals("PickupRegularCab")) {
			return Type.PICKUP_REGULAR_CAB;
		}
		else if (type.equals("OpenAirAllTerrain")) {
			return Type.OPEN_AIR_ALL_TERRAIN;
		}
		else if (type.equals("Special")) {
			return Type.SPECIAL;
		}
		else if (type.equals("CommercialVanTruck")) {
			return Type.COMMERCIAL_VAN_TRUCK;
		}
		else if (type.equals("PickupExtendedCab")) {
			return Type.PICKUP_EXTENDED_CAB;
		}
		else if (type.equals("SpecialOfferCar")) {
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
		else if (type.equals("TwoWheelVehicle")) {
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

	private Location parseHotelLocation(JSONObject propertyJson, JSONObject addressJson) {
		Location location = new Location();
		if (addressJson.has("addressLine1")) {
			location.addStreetAddressLine(addressJson.optString("addressLine1", null));
		}
		if (addressJson.has("addressLine2")) {
			location.addStreetAddressLine(addressJson.optString("addressLine2", null));
		}
		location.setCity(addressJson.optString("city", null));
		location.setStateCode(addressJson.optString("countrySubdivisionCode", null));
		location.setCountryCode(addressJson.optString("countryCode", null));
		location.setPostalCode(addressJson.optString("postalCode", null));
		location.setLatitude(propertyJson.optDouble("latitude"));
		location.setLongitude(propertyJson.optDouble("longitude"));
		return location;
	}

	private Location parseCarLocation(JSONObject obj) {
		if (obj != null) {
			Location location = new Location();
			location.setLatitude(obj.optDouble("latitude", 0));
			location.setLongitude(obj.optDouble("longitude", 0));
			location.addStreetAddressLine(obj.optString("addressLine1"));
			location.addStreetAddressLine(obj.optString("addressLine2"));
			location.setCity(obj.optString("cityName", obj.optString("addressLine3")));
			location.setStateCode(obj.optString("provinceStateName"));
			location.setPostalCode(obj.optString("postalCode"));
			location.setCountryCode(obj.optString("countryCode"));

			return location;
		}

		return null;
	}

	public Waypoint parseWaypoint(JSONObject obj, int type) {
		if (obj != null && (type == Waypoint.F_ARRIVAL || type == Waypoint.F_DEPARTURE)) {
			Waypoint waypoint = new Waypoint(type);
			String locationName = type == Waypoint.F_ARRIVAL ? "arrivalLocation" : "departureLocation";
			String timeName = type == Waypoint.F_ARRIVAL ? "arrivalTime" : "departureTime";

			JSONObject locationJson = obj.optJSONObject(locationName);
			if (locationJson != null) {
				waypoint.mAirportCode = locationJson.optString("airportCode");
			}

			JSONObject timeJson = obj.optJSONObject(timeName);
			if (timeJson != null) {
				DateTime time = DateTimeParser.parseDateTime(timeJson);
				waypoint.addDateTime(Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED,
						time.getMillis(), time.getZone().getStandardOffset(0));
			}

			return waypoint;
		}

		return null;
	}
}
