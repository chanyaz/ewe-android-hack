package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.expedia.bookings.data.Activity;
import com.expedia.bookings.data.AirAttach;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.CarVendor;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rule;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarType;
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership;
import com.expedia.bookings.data.trips.BookingStatus;
import com.expedia.bookings.data.trips.CustomerSupport;
import com.expedia.bookings.data.trips.FlightConfirmation;
import com.expedia.bookings.data.trips.Insurance;
import com.expedia.bookings.data.trips.ItinFlightLegTime;
import com.expedia.bookings.data.trips.OccupantSelectedRoomOptions;
import com.expedia.bookings.data.trips.OtherOccupantInfo;
import com.expedia.bookings.data.trips.PrimaryOccupant;
import com.expedia.bookings.data.trips.TicketingStatus;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.Trip.LevelOfDetail;
import com.expedia.bookings.data.trips.TripActivity;
import com.expedia.bookings.data.trips.TripCar;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripCruise;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.data.trips.TripHotelRoom;
import com.expedia.bookings.data.trips.TripPackage;
import com.expedia.bookings.data.trips.TripRails;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Seat;
import com.mobiata.flightlib.data.Waypoint;

import static com.activeandroid.Cache.getContext;

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
		if ("FULL".equals(levelOfDetail) || tripJson.optJSONArray("rails") != null) {
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
		trip.setStartDate(DateTimeParser.parseDateTime(tripJson.optJSONObject("startTime")));
		trip.setEndDate(DateTimeParser.parseDateTime(tripJson.optJSONObject("endTime")));

		trip.setBookingStatus(parseBookingStatus(tripJson.optString("bookingStatus")));

		/*
		 *  The api returns the sharableUrl in the form of /api/trips/shared. But this is NOT the link that is to be shared to any users.
		 *  We will instead replace it with /m/trips/shared, so that if the user clicks on the link without the app installed it will get them to the mobileWeb's shared itin landing page.
		 */
		trip.getShareInfo().setSharableDetailsUrl(tripJson.optString("sharableDetailsURL").replace("/api/", "/m/"));

		trip.setCustomerSupport(parseCustomerSupport(tripJson.optJSONObject("customerSupport")));

		trip.addTripComponents(parseTripComponents(tripJson));
		trip.setIsTripUpgradable(tripJson.optBoolean("isTripUpgradable"));

		// Parse insurance
		JSONArray insurance = tripJson.optJSONArray("insurance");
		if (insurance != null) {
			for (int b = 0; b < insurance.length(); b++) {
				trip.addInsurance(parseTripInsurance(insurance.optJSONObject(b)));
			}
		}

		// Parse air attach qualification
		if (tripJson.has("airAttachQualificationInfo")) {
			trip.setAirAttach(new AirAttach(tripJson.optJSONObject("airAttachQualificationInfo")));
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
		tripComponents.addAll(parseType(obj, "rails", TripComponent.Type.RAILS));

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
					component = parseTripActivity(componentJson, obj);
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
				case RAILS:
					component = parseTripRails(componentJson);
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

	private TicketingStatus parseTicketingStatus(String status) {
		if ("INPROGRESS".equals(status)) {
			return TicketingStatus.INPROGRESS;
		}
		else if ("CANCELLED".equals(status)) {
			return TicketingStatus.CANCELLED;
		}
		else if ("COMPLETE".equals(status)) {
			return TicketingStatus.COMPLETE;
		}
		else if ("VOIDED".equals(status)) {
			return TicketingStatus.VOIDED;
		}

		return TicketingStatus.NONE;
	}

	private TripHotel parseTripHotel(JSONObject obj) {
		TripHotel hotel = new TripHotel();

		parseTripCommon(obj, hotel);
		hotel.getShareInfo().setSharableDetailsUrl(obj.optString("sharableItemDetailURL").replace("/api/", "/m/"));

		if (obj.has("checkInDateTime") && obj.has("checkOutDateTime")) {
			hotel.setStartDate(DateTimeParser.parseDateTime(obj.optJSONObject("checkInDateTime")));
			hotel.setEndDate(DateTimeParser.parseDateTime(obj.optJSONObject("checkOutDateTime")));
		}
		else {
			// Old version of code, kept because I'm not sure which servers support newer version yet
			hotel.setStartDate(DateTimeParser.parseISO8601DateTimeString(obj.optString("checkInDate")));
			hotel.setEndDate(DateTimeParser.parseISO8601DateTimeString(obj.optString("checkOutDate")));
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

			boolean isVipHotel = propertyJson.optBoolean("isVipAccess", false);
			property.setIsVipAccess(isVipHotel);

			List<String> checkInPolicies = JSONUtils.getStringList(propertyJson, "checkInPolicies");
			if (checkInPolicies != null && !checkInPolicies.isEmpty()) {
				property.setCheckInPolicies(checkInPolicies);
			}

			hotel.setProperty(property);
		}

		JSONObject rulesJson = obj.optJSONObject("rules");
		if (rulesJson != null) {
			List<String> rules = new ArrayList<>();
			if (rulesJson.has("cancelChangeRulesIntroduction") && !rulesJson.optString("cancelChangeRulesIntroduction").isEmpty()) {
				rules.add(rulesJson.optString("cancelChangeRulesIntroduction"));
			}
			List<String> cancelChangeRules = JSONUtils.getStringList(rulesJson, "cancelChangeRules");
			if (cancelChangeRules != null && !cancelChangeRules.isEmpty()) {
				rules.addAll(cancelChangeRules);
			}
			hotel.setChangeAndCancelRules(rules);
		}

		parseHotelRooms(obj, hotel, property);

		return hotel;
	}

	private void parseHotelRooms(JSONObject obj, TripHotel hotel, Property property) {
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

				String roomType = room.optString("roomRatePlanDescription");
				property.setItinRoomType(roomType);

				JSONObject nonPricePromotionData = room.optJSONObject("nonPricePromotionData");
				if (nonPricePromotionData != null) {
					property.setItinNonPricePromotionText(nonPricePromotionData.optString("text"));
				}

				JSONObject roomPreferences = room.optJSONObject("roomPreferences");
				PrimaryOccupant primaryOccupant = null;
				OccupantSelectedRoomOptions occupantSelectedRoomOptions = null;
				OtherOccupantInfo otherOccupantInfo = null;
				if (roomPreferences != null) {
					JSONObject otherOccupantInfoObj = roomPreferences.optJSONObject("otherOccupantInfo");
					if (otherOccupantInfoObj != null) {
						int adultCount = otherOccupantInfoObj.optInt("adultCount");
						guests += adultCount;
						int childAndInfantCount = otherOccupantInfoObj.optInt("childAndInfantCount");
						guests += childAndInfantCount;
						otherOccupantInfo = new OtherOccupantInfo(
							adultCount,
							otherOccupantInfoObj.optInt("childCount"),
							otherOccupantInfoObj.optInt("infantCount"),
							childAndInfantCount,
							otherOccupantInfoObj.optInt("maxGuestCount"),
							parseListOfIntegers(otherOccupantInfoObj, "childAndInfantAges")
						);
					}

					JSONObject occupantSelectedRoomOptionsObj = roomPreferences
						.optJSONObject("occupantSelectedRoomOptions");
					if (occupantSelectedRoomOptionsObj != null) {
						String bedType = occupantSelectedRoomOptionsObj.optString("bedTypeName");
						property.setItinBedType(bedType);

						occupantSelectedRoomOptions = new OccupantSelectedRoomOptions(
							bedType,
							occupantSelectedRoomOptionsObj.optString("defaultBedTypeName"),
							occupantSelectedRoomOptionsObj.optString("smokingPreference"),
							occupantSelectedRoomOptionsObj.optString("specialRequest"),
							parseListOfStrings(occupantSelectedRoomOptionsObj, "accessibilityOptions"),
							occupantSelectedRoomOptionsObj.optBoolean("hasExtraBedAdult"),
							occupantSelectedRoomOptionsObj.optBoolean("hasExtraBedChild"),
							occupantSelectedRoomOptionsObj.optBoolean("hasExtraBedInfant"),
							occupantSelectedRoomOptionsObj.optBoolean("isSmokingPreferenceSelected"),
							occupantSelectedRoomOptionsObj.optBoolean("isRoomOptionsAvailable")
						);
					}
					// Used only when importing a shared Itin
					JSONObject primaryOccupantInfo = roomPreferences.optJSONObject("primaryOccupant");
					if (primaryOccupantInfo != null) {
						primaryTraveler = new Traveler();
						String firstName = primaryOccupantInfo.optString("firstName");
						primaryTraveler.setFirstName(firstName);
						String fullName = primaryOccupantInfo.optString("fullName");
						primaryTraveler.setFullName(fullName);
						primaryOccupant = new PrimaryOccupant(firstName, fullName, primaryOccupantInfo.optString("email"), primaryOccupantInfo.optString("phone"));
					}
				}

				TripHotelRoom tripHotelRoom = new TripHotelRoom(
					conf,
					roomType,
					room.optString("bookingStatus"),
					primaryOccupant,
					occupantSelectedRoomOptions,
					otherOccupantInfo,
					parseListOfStrings(room, "amenities"),
					parseListOfIntegers(room, "amenityIds")
				);

				hotel.addRoom(tripHotelRoom);
			}

			if (!roomsJson.isNull(0)) {
				JSONObject firstRoom = roomsJson.optJSONObject(0);
				property.setRoomCancelLink(firstRoom.optString("roomCancelLink"));
				boolean isNotMultiRoomBooking = roomsJson.length() == 1;
				if (isNotMultiRoomBooking) {
					property.setBookingChangeWebUrl(firstRoom.optString("roomChangeLinkForMobileWebView"));
					property.setRoomUpgradeWebViewUrl(firstRoom.optString("roomUpgradeLink"));
					property.setRoomUpgradeOffersApiUrl(firstRoom.optString("roomUpgradeOfferApiUrl"));
				}
			}
		}

		hotel.setGuests(guests);
		hotel.setPrimaryTraveler(primaryTraveler);
	}

	private List<String> parseListOfStrings(JSONObject obj, String key) {
		JSONArray values = obj.optJSONArray(key);
		List<String> list = new ArrayList<>();
		if (values != null) {
			for (int value = 0; value < values.length(); ++value) {
				list.add(values.optString(value));
			}
		}
		return list;
	}

	private List<Integer> parseListOfIntegers(JSONObject obj, String key) {
		JSONArray values = obj.optJSONArray(key);
		List<Integer> list = new ArrayList<>();
		if (values != null) {
			for (int value = 0; value < values.length(); ++value) {
				list.add(values.optInt(value));
			}
		}
		return list;
	}

	public TripFlight parseTripFlight(JSONObject obj) {
		TripFlight flight = new TripFlight();
		flight.setTicketingStatus(parseTicketingStatus(obj.optString("ticketingStatus")));

		parseTripCommon(obj, flight);

		if (obj.has("startTime") && obj.has("endTime")) {
			flight.setStartDate(DateTimeParser.parseDateTime(obj.optJSONObject("startTime")));
			flight.setEndDate(DateTimeParser.parseDateTime(obj.optJSONObject("endTime")));
		}
		else {
			flight.setStartDate(DateTimeParser.parseDateTime(obj.optJSONObject("startDate")));
			flight.setEndDate(DateTimeParser.parseDateTime(obj.optJSONObject("endDate")));
		}

		// We're taking a lack of legs info to mean that this is a non-details call;
		// short-circuit out of the info early
		if (!obj.has("legs")) {
			return flight;
		}

		// Parse confirmations
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

		//Parse order number also called travelRecordLocator(TRL)
		flight.setOrderNumber(obj.optString("orderNumber"));

		// Parse destination regionId
		flight.setDestinationRegionId(obj.optString("destinationRegionId"));

		//Parse check in link
		flight.setCheckInLink(obj.optString("airlineCheckInURL"));

		FlightTrip flightTrip = new FlightTrip();
		flight.setFlightTrip(flightTrip);

		parseRulesObject(obj,flightTrip);

		flightTrip.setSplitTicket(obj.optBoolean("isSplitTicket"));

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

		//Parse rules
		JSONObject rulesJson = obj.optJSONObject("rules");
		if (rulesJson != null) {
			JSONObject additionalAirlineFeesJson = rulesJson.optJSONObject("additionalAirlineFees");
			if (additionalAirlineFeesJson != null && additionalAirlineFeesJson.has("url")) {
				flight.setAdditionalAirlineFees(additionalAirlineFeesJson.optString("url"));
			}
		}

		// Parse the legs
		JSONArray legsArr = obj.optJSONArray("legs");
		for (int a = 0; a < legsArr.length(); a++) {
			JSONObject legJson = legsArr.optJSONObject(a);
			FlightLeg leg = new FlightLeg();

			// Fetch the sharable link at the flight leg level.
			leg.getShareInfo().setSharableDetailsUrl(legJson.optString("sharableFlightLegURL").replace("/api/", "/m/"));
			leg.setLegDuration(legJson.optString("duration"));
			leg.setNumberOfStops(legJson.optString("numberOfStops"));
			leg.setAirlineLogoURL(legJson.optString("airlineLogoURL"));
			leg.setLegArrivalTime(parseItinFlightLegTime(legJson.optJSONObject("legArrivaltime")));
			leg.setLegDepartureTime(parseItinFlightLegTime(legJson.optJSONObject("legDepartureTime")));
			JSONArray segmentsArr = legJson.optJSONArray("segments");
			for (int b = 0; b < segmentsArr.length(); b++) {
				JSONObject segmentJson = segmentsArr.optJSONObject(b);

				Flight segment = new Flight();

				//required for flight map
				segment.mStatusCode = Flight.STATUS_UNKNOWN;

				segment.setOriginWaypoint(parseWaypoint(segmentJson, Waypoint.F_DEPARTURE));
				segment.setDestinationWaypoint(parseWaypoint(segmentJson, Waypoint.F_ARRIVAL));

				segment.setCabinCode(segmentJson.optString("cabinCodeLocalized"));
				segment.setIsSeatMapAvailable(segmentJson.optBoolean("isSeatMapAvailable"));
				JSONArray seatsArr = segmentJson.optJSONArray("seatList");
				if (seatsArr != null && seatsArr.length() > 0) {
					for (int c = 0; c < seatsArr.length(); c++) {
						JSONObject seatJson = seatsArr.optJSONObject(c);
						Seat seat = new Seat();
						seat.setAssigned(seatJson.optString("assigned"));
						seat.setPassenger(seatJson.optString("passengerName"));
						segment.addSeat(seat);
					}
				}
				JSONObject departureTimeJson = segmentJson.optJSONObject("departureTime");
				if (departureTimeJson != null) {
					if (!departureTimeJson.optString("raw").isEmpty()) {
						segment.setSegmentDepartureTime(departureTimeJson.optString("raw"));
					}
				}
				JSONObject arrivalTimeJson = segmentJson.optJSONObject("arrivalTime");
				if (arrivalTimeJson != null) {
					if (!arrivalTimeJson.optString("raw").isEmpty()) {
						segment.setSegmentArrivalTime(arrivalTimeJson.optString("raw"));
					}
				}
				FlightCode flightCode = new FlightCode();
				flightCode.mAirlineCode = segmentJson.optString("externalAirlineCode");
				flightCode.mNumber = segmentJson.optString("flightNumber").trim();
				flightCode.mAirlineName = segmentJson.optString("airlineName");
				segment.addFlightCode(flightCode, Flight.F_PRIMARY_AIRLINE_CODE);

				String operatedBy = segmentJson.optString("operatedByAirCarrierName", null);
				if (!TextUtils.isEmpty(operatedBy)) {
					FlightCode opFlightCode = new FlightCode();
					opFlightCode.mAirlineName = operatedBy;
					segment.addFlightCode(opFlightCode, Flight.F_OPERATING_AIRLINE_CODE);
				}

				segment.mAircraftType = segmentJson.optString("equipmentDescription", null);
				segment.setDepartureTerminal(segmentJson.optString("departureTerminal", null));
				segment.setArrivalTerminal(segmentJson.optString("arrivalTerminal", null));
				segment.setLayoverDuration(segmentJson.optString("layoverDuration", null));

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

	private TripActivity parseTripActivity(JSONObject obj, JSONObject tripJson) {
		TripActivity tripActivity = new TripActivity();

		parseTripCommon(obj, tripActivity);

		tripActivity.setStartDate(DateTimeParser.parseDateTime(obj.optJSONObject("startTime")));
		tripActivity.setEndDate(DateTimeParser.parseDateTime(obj.optJSONObject("endTime")));

		if (obj.has("uniqueID")) {
			Activity activity = new Activity();
			String imageUrl = null;
			if (obj.optJSONObject("highResImage") != null && obj.optJSONObject("highResImage").optString("url") != null) {
				imageUrl = obj.optJSONObject("highResImage").optString("url");
			}
			else if (obj.optJSONObject("image") != null && obj.optJSONObject("image").optString("url") != null) {
				imageUrl = obj.optJSONObject("image").optString("url");
			}
			int guestCount = 0;
			activity.setId(obj.optString("uniqueID", null));
			activity.setTitle(obj.optString("activityTitle", null));

			if (obj.optJSONObject("price") != null && obj.optJSONObject("price").optJSONObject("pricePerCategory") != null) {
				JSONObject travelerCategories = obj.optJSONObject("price").optJSONObject("pricePerCategory");
				Iterator<?> iterator = travelerCategories.keys();

				while (iterator.hasNext()) {
					try {
						guestCount += ((JSONObject) travelerCategories.get((String)iterator.next())).optInt("numberOfPassengers");
					}
					catch (JSONException e) {
						Log.e("Exception parsing traveler from travelerCategories", e);
					}
				}
			}
			activity.setGuestCount(guestCount);

			String voucherPrintURL = "";
			String e3EndpointUrl = Ui.getApplication(getContext()).appComponent().endpointProvider().getE3EndpointUrl();
			if (obj.optInt("activityCategoryID") == 2) {
				voucherPrintURL = e3EndpointUrl + "things-to-do/voucher/?tripid=" + tripJson.optString("tripId") + "&id=" + obj.optString("uniqueID");
			}
			else {
				voucherPrintURL = e3EndpointUrl + "itinerary-print?tripid=" + tripJson.optString("tripId") + "&itineraryNumber=" + tripJson.optString("tripNumber") + "&id=" + obj.optString("uniqueID");
			}
			activity.setVoucherPrintUrl(voucherPrintURL);
			activity.setImageUrl(imageUrl);

			// Parse travelers
			JSONArray travelersArr = obj.optJSONArray("travelers");
			if (travelersArr != null) {
				for (int i = 0; i < travelersArr.length(); i++) {
					activity.addTraveler(parseTraveler(travelersArr.optJSONObject(i)));
				}
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

	private TripRails parseTripRails(JSONObject obj) {
		TripRails tripRails = new TripRails();

		parseTripCommon(obj, tripRails);
		tripRails.setStartDate(DateTimeParser.parseDateTime(obj.optJSONObject("startTime")));
		tripRails.setEndDate(DateTimeParser.parseDateTime(obj.optJSONObject("endTime")));
		/**
		 * Currently, api only returns SUMMARY details for RAILS trip.
		 * One we get FULL details, we need to parse rail products and show actual details
		 * https://eiwork.mingle.thoughtworks.com/projects/eb_ad_app/cards/9374
		 */
		return tripRails;
	}

	private Insurance parseTripInsurance(JSONObject obj) {
		Insurance retVal = new Insurance();

		if (obj.has("displayName")) {
			retVal.setPolicyName(obj.optString("displayName", null));
			retVal.setTermsUrl(obj.optString("termsURL", null));
			retVal.setInsuranceLineOfBusiness(obj.optString("lineOfBusiness", ""));
		}

		return retVal;
	}

	@VisibleForTesting
	public Traveler parseTraveler(JSONObject obj) {
		Traveler traveler = new Traveler();
		traveler.setFirstName(obj.optString("firstName"));
		traveler.setMiddleName(obj.optString("middleName"));
		traveler.setLastName(obj.optString("lastName"));
		traveler.setFullName(obj.optString("fullName"));
		traveler.setAge(obj.optInt("age"));
		traveler.setKnownTravelerNumber(obj.optString("TSAKnownTravelerNumber"));
		traveler.setRedressNumber(obj.optString("TSARedressNumber"));
		traveler.setTicketNumbers(JSONUtils.getStringList(obj, "ticketNumbers"));
		traveler.setSpecialAssistanceOptions(JSONUtils.getStringList(obj, "specialAssistanceOptions"));
		traveler.setEmail(obj.optString("emailAddress"));
		JSONArray frequentFlyerArray = obj.optJSONArray("frequentFlyerPlans");
		if (frequentFlyerArray != null && frequentFlyerArray.length() > 0) {
			for (int i = 0; i < frequentFlyerArray.length(); i++) {
				JSONObject currentFrequentFlyer = frequentFlyerArray.optJSONObject(i);
				TravelerFrequentFlyerMembership frequentFlyerMembership = new TravelerFrequentFlyerMembership();
				frequentFlyerMembership.setMembershipNumber(currentFrequentFlyer.optString("membershipNumber"));
				frequentFlyerMembership.setPlanCode(currentFrequentFlyer.optString("programCode"));
				frequentFlyerMembership.setAirlineCode(currentFrequentFlyer.optString("airlineCode"));
				frequentFlyerMembership.setProgramName(currentFrequentFlyer.optString("programName"));
				traveler.addFrequentFlyerMembership(frequentFlyerMembership);
			}
		}
		String typeCode = obj.optString("typeCode");
			if (typeCode.equals(PassengerCategory.INFANT_IN_LAP.name())) {
				traveler.setPassengerCategory(PassengerCategory.INFANT_IN_LAP);
			}

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

	private CarCategory parseCarCategory(String category) {
		if (TextUtils.isEmpty(category)) {
			return null;
		}

		if (category.equals("Mini")) {
			return CarCategory.MINI;
		}
		else if (category.equals("Economy")) {
			return CarCategory.ECONOMY;
		}
		else if (category.equals("Compact")) {
			return CarCategory.COMPACT;
		}
		else if (category.equals("Midsize")) {
			return CarCategory.MIDSIZE;
		}
		else if (category.equals("Standard")) {
			return CarCategory.STANDARD;
		}
		else if (category.equals("Fullsize")) {
			return CarCategory.FULLSIZE;
		}
		else if (category.equals("Premium")) {
			return CarCategory.PREMIUM;
		}
		else if (category.equals("Luxury")) {
			return CarCategory.LUXURY;
		}
		else if (category.equals("Special")) {
			return CarCategory.SPECIAL;
		}
		else if (category.equals("MiniElite")) {
			return CarCategory.MINI_ELITE;
		}
		else if (category.equals("EconomyElite")) {
			return CarCategory.ECONOMY_ELITE;
		}
		else if (category.equals("CompactElite")) {
			return CarCategory.COMPACT_ELITE;
		}
		else if (category.equals("MidsizeElite")) {
			return CarCategory.MIDSIZE_ELITE;
		}
		else if (category.equals("StandardElite")) {
			return CarCategory.STANDARD_ELITE;
		}
		else if (category.equals("FullsizeElite")) {
			return CarCategory.FULLSIZE_ELITE;
		}
		else if (category.equals("PremiumElite")) {
			return CarCategory.PREMIUM_ELITE;
		}
		else if (category.equals("LuxuryElite")) {
			return CarCategory.LUXURY_ELITE;
		}
		else if (category.equals("Oversize")) {
			return CarCategory.OVERSIZE;
		}

		return null;
	}

	private CarType parseCarType(String type) {
		if (TextUtils.isEmpty(type)) {
			return null;
		}

		if (type.equals("TwoDoorCar")) {
			return CarType.TWO_DOOR_CAR;
		}
		else if (type.equals("ThreeDoorCar")) {
			return CarType.THREE_DOOR_CAR;
		}
		else if (type.equals("FourDoorCar")) {
			return CarType.FOUR_DOOR_CAR;
		}
		else if (type.equals("Van")) {
			return CarType.VAN;
		}
		else if (type.equals("Wagon")) {
			return CarType.WAGON;
		}
		else if (type.equals("Limousine")) {
			return CarType.LIMOUSINE;
		}
		else if (type.equals("RecreationalVehicle")) {
			return CarType.RECREATIONAL_VEHICLE;
		}
		else if (type.equals("Convertible")) {
			return CarType.CONVERTIBLE;
		}
		else if (type.equals("SportsCar")) {
			return CarType.SPORTS_CAR;
		}
		else if (type.equals("SUV")) {
			return CarType.SUV;
		}
		else if (type.equals("PickupRegularCab")) {
			return CarType.PICKUP_REGULAR_CAB;
		}
		else if (type.equals("OpenAirAllTerrain")) {
			return CarType.OPEN_AIR_ALL_TERRAIN;
		}
		else if (type.equals("Special")) {
			return CarType.SPECIAL;
		}
		else if (type.equals("CommercialVanTruck")) {
			return CarType.COMMERCIAL_VAN_TRUCK;
		}
		else if (type.equals("PickupExtendedCab")) {
			return CarType.PICKUP_EXTENDED_CAB;
		}
		else if (type.equals("SpecialOfferCar")) {
			return CarType.SPECIAL_OFFER_CAR;
		}
		else if (type.equals("Coupe")) {
			return CarType.COUPE;
		}
		else if (type.equals("Monospace")) {
			return CarType.MONOSPACE;
		}
		else if (type.equals("Motorhome")) {
			return CarType.MOTORHOME;
		}
		else if (type.equals("TwoWheelVehicle")) {
			return CarType.TWO_WHEEL_VEHICLE;
		}
		else if (type.equals("Roadster")) {
			return CarType.ROADSTER;
		}
		else if (type.equals("Crossover")) {
			return CarType.CROSSOVER;
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
		location.setCountryName(addressJson.optString("countryName", null));
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

	private ItinFlightLegTime parseItinFlightLegTime(JSONObject obj) {
		if (obj != null) {
			ItinFlightLegTime flightLegTime = new ItinFlightLegTime();
			flightLegTime.fromJson(obj);
			return flightLegTime;
		}
		return null;
	}

	private Rule getRule(JSONObject rulesObj, String key) {
		Rule rule = new Rule();
		rule.setName(key);
		JSONObject object = rulesObj.optJSONObject(key);
		if (object == null) {
			rule.setText(rulesObj.optString(key));
		}
		else {
			rule.setText(object.optString("text"));
			rule.setUrl(object.optString("url"));
			rule.setTextAndURL(object.optString("textAndURL"));
		}
		return rule;
	}

	private void parseRulesObject(JSONObject obj, FlightTrip flightTrip) {
		JSONObject rulesObj = obj.optJSONObject("rules");
		if (rulesObj != null) {
			flightTrip.addRule(getRule(rulesObj, "cancellationFeeLegalText"));
			flightTrip.addRule(getRule(rulesObj, "cancelChangeIntroductionText"));
			flightTrip.addRule(getRule(rulesObj, "feeChangeRefundIntroductionText"));
			flightTrip.addRule(getRule(rulesObj, "refundabilityText"));
			flightTrip.addRule(getRule(rulesObj, "refundableStatus"));
			flightTrip.addRule(getRule(rulesObj, "completePenaltyRules"));
			flightTrip.addRule(getRule(rulesObj, "additionalAirlineFees"));
			flightTrip.addRule(getRule(rulesObj, "airlineLiabilityLimitations"));
		}
	}
}
