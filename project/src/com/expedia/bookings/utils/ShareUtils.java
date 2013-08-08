package com.expedia.bookings.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.CarVendor;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripFlight;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Layover;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

public class ShareUtils {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	// Flight

	private static final int MAX_TIMEZONE_LENGTH = 6;

	// Hotel
	private static final int LONG_SHARE_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
			| DateUtils.FORMAT_SHOW_WEEKDAY;
	private static final int SHARE_CHECK_IN_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
			| DateUtils.FORMAT_ABBREV_WEEKDAY;
	private static final int SHARE_CHECK_OUT_FLAGS = LONG_SHARE_DATE_FLAGS | DateUtils.FORMAT_ABBREV_WEEKDAY;

	// Car
	private static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_TIME;
	private static final int SHORT_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR
			| DateUtils.FORMAT_ABBREV_MONTH;

	// Activity
	private static final int SHARE_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
			| DateUtils.FORMAT_SHOW_WEEKDAY;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Context mContext;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ShareUtils(Context context) {
		mContext = context;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public String getShareSubject(ItinCardData itinCardData) {
		if (itinCardData instanceof ItinCardDataFlight) {
			return getFlightShareSubject(((ItinCardDataFlight) itinCardData).getFlightLeg());
		}
		else if (itinCardData instanceof ItinCardDataHotel) {
			return getHotelShareSubject((ItinCardDataHotel) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataCar) {
			return getCarShareSubject((ItinCardDataCar) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataActivity) {
			return getActivityShareSubject((ItinCardDataActivity) itinCardData);
		}

		return null;
	}

	public String getShareTextShort(ItinCardData itinCardData) {
		if (itinCardData instanceof ItinCardDataFlight) {
			return getFlightShareTextShort((ItinCardDataFlight) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataHotel) {
			return getHotelShareTextShort((ItinCardDataHotel) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataCar) {
			return getCarShareTextShort((ItinCardDataCar) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataActivity) {
			return getActivityShareTextShort((ItinCardDataActivity) itinCardData);
		}

		return null;
	}

	public String getShareTextLong(ItinCardData itinCardData) {
		if (itinCardData instanceof ItinCardDataFlight) {
			return getFlightShareTextLong((ItinCardDataFlight) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataHotel) {
			return getHotelShareTextLong((ItinCardDataHotel) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataCar) {
			return getCarShareTextLong((ItinCardDataCar) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataActivity) {
			return getActivityShareTextLong((ItinCardDataActivity) itinCardData);
		}

		return null;
	}

	// SHARE SUBJECT

	public String getFlightShareSubject(FlightTrip trip) {
		return getFlightShareSubject(trip.getLeg(0), trip.getLeg(trip.getLegCount() - 1));
	}

	public String getFlightShareSubject(FlightLeg leg) {
		return getFlightShareSubject(leg, leg);
	}

	public String getHotelShareSubject(ItinCardDataHotel itinCardData) {
		return getHotelShareSubject(itinCardData.getPropertyCity(), itinCardData.getStartDate().toLocalDate(),
				itinCardData.getEndDate().toLocalDate());
	}

	public String getCarShareSubject(ItinCardDataCar itinCardData) {
		return getCarShareSubject(itinCardData.getPickUpDate(), itinCardData.getDropOffDate());
	}

	public String getActivityShareSubject(ItinCardDataActivity itinCardData) {
		return getActivityShareSubject(itinCardData.getTitle());
	}

	// SHARE TEXT SHORT

	public String getFlightShareTextShort(ItinCardDataFlight itinCardData) {
		if (itinCardData != null) {
			return getFlightShareTextShort(itinCardData.getFlightLeg());
		}
		return null;
	}

	public String getHotelShareTextShort(ItinCardDataHotel itinCardData) {
		return getHotelShareTextShort(itinCardData.getPropertyName(), itinCardData.getStartDate(),
				itinCardData.getEndDate(), itinCardData.getDetailsUrl());
	}

	public String getCarShareTextShort(ItinCardDataCar itinCardData) {
		Car.Category category = itinCardData.getCar().getCategory();
		DateTime pickUpDate = itinCardData.getPickUpDate();
		DateTime dropOffDate = itinCardData.getDropOffDate();
		String vendorName = itinCardData.getVendorName();
		String vendorAddress = itinCardData.getRelevantVendorLocation().toLongFormattedString();

		return getCarShareTextShort(category, pickUpDate, dropOffDate, vendorName, vendorAddress);
	}

	public String getActivityShareTextShort(ItinCardDataActivity itinCardData) {
		return getActivityShareTextShort(itinCardData.getTitle(), itinCardData.getValidDate(),
				itinCardData.getExpirationDate());
	}

	// SHARE TEXT LONG

	public String getFlightShareTextLong(ItinCardDataFlight itinCardData) {
		TripFlight tripFlight = (TripFlight) itinCardData.getTripComponent();
		return getFlightShareEmail(itinCardData.getFlightLeg(), tripFlight.getTravelers());
	}

	public String getFlightShareEmail(FlightTrip trip, List<Traveler> travelers) {
		return getFlightShareEmail(trip, trip.getLeg(0), trip.getLeg(trip.getLegCount() - 1), travelers);
	}

	public String getFlightShareEmail(FlightLeg leg, List<Traveler> travelers) {
		return getFlightShareEmail(null, leg, leg, travelers);
	}

	public String getHotelShareTextLong(ItinCardDataHotel itinCardData) {
		String hotelName = itinCardData.getPropertyName();
		String address = itinCardData.getAddressString();
		String phone = itinCardData.getRelevantPhone();
		DateTime startDate = itinCardData.getStartDate();
		DateTime endDate = itinCardData.getEndDate();
		String detailsUrl = itinCardData.getDetailsUrl();

		return getHotelShareTextLong(hotelName, address, phone, startDate.toLocalDate(), endDate.toLocalDate(),
				detailsUrl);
	}

	public String getCarShareTextLong(ItinCardDataCar itinCardData) {
		Car.Category category = itinCardData.getCar().getCategory();
		DateTime pickUpDate = itinCardData.getPickUpDate();
		DateTime dropOffDate = itinCardData.getDropOffDate();
		CarVendor vendor = itinCardData.getCar().getVendor();
		Location pickUpLocation = itinCardData.getPickUpLocation();
		Location dropOffLocation = itinCardData.getDropOffLocation();

		return getCarShareTextLong(category, pickUpDate, dropOffDate, vendor, pickUpLocation, dropOffLocation);
	}

	public String getActivityShareTextLong(ItinCardDataActivity itinCardData) {
		return getActivityShareTextLong(itinCardData.getTitle(), itinCardData.getValidDate(),
				itinCardData.getExpirationDate(), itinCardData.getTravelers());
	}

	// Share methods

	// Flights

	public String getFlightShareSubject(FlightLeg firstLeg, FlightLeg lastLeg) {
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());

		long start = DateTimeUtils.getTimeInLocalTimeZone(firstLeg.getFirstWaypoint().getMostRelevantDateTime())
				.getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(
				lastLeg.getLastWaypoint().getMostRelevantDateTime()).getTime();
		String dateRange = DateUtils.formatDateRange(mContext, start, end, DateUtils.FORMAT_NUMERIC_DATE
				| DateUtils.FORMAT_SHOW_DATE);

		return mContext.getString(R.string.share_template_subject_flight, destinationCity, dateRange);
	}

	public String getFlightShareTextShort(FlightLeg leg) {
		if (leg == null || leg.getLastWaypoint() == null || leg.getLastWaypoint().getAirport() == null) {
			return null;
		}

		String airlineAndFlightNumber = FormatUtils.formatFlightNumberShort(
				leg.getSegment(leg.getSegmentCount() - 1), mContext);
		String destinationCity = leg.getLastWaypoint().getAirport().mCity;
		String destinationAirportCode = leg.getLastWaypoint().getAirport().mAirportCode;
		String originAirportCode = leg.getFirstWaypoint().getAirport().mAirportCode;
		String destinationGateTerminal = FlightUtils.getTerminalGateString(mContext, leg.getLastWaypoint());

		Calendar departureCal = leg.getFirstWaypoint().getBestSearchDateTime();
		Calendar arrivalCal = leg.getLastWaypoint().getBestSearchDateTime();

		Date departureDate = DateTimeUtils.getTimeInLocalTimeZone(departureCal);
		Date arrivalDate = DateTimeUtils.getTimeInLocalTimeZone(arrivalCal);

		String departureTzString = FormatUtils.formatTimeZone(leg.getFirstWaypoint().getAirport(), departureDate,
				MAX_TIMEZONE_LENGTH);
		String arrivalTzString = FormatUtils.formatTimeZone(leg.getLastWaypoint().getAirport(), arrivalDate,
				MAX_TIMEZONE_LENGTH);

		//single day
		if (leg.getDaySpan() == 0) {
			String template = mContext.getString(R.string.share_template_short_flight_sameday);

			String departureDateStr = DateUtils.formatDateTime(mContext, departureDate.getTime(),
					DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
			String departureTimeStr = DateUtils.formatDateTime(mContext, departureDate.getTime(),
					DateUtils.FORMAT_SHOW_TIME) + " " + departureTzString;
			String arrivalStr = DateUtils.formatDateTime(mContext, arrivalDate.getTime(), DateUtils.FORMAT_SHOW_TIME)
					+ " " + arrivalTzString;

			return String.format(template, airlineAndFlightNumber, destinationCity, departureDateStr,
					originAirportCode, departureTimeStr, destinationAirportCode, arrivalStr, destinationGateTerminal);
		}
		//multi day
		else {
			String template = mContext.getString(R.string.share_template_short_flight_multiday);

			int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
					| DateUtils.FORMAT_NUMERIC_DATE;
			String departureDateTimeStr = DateUtils.formatDateTime(mContext, departureDate.getTime(), flags) + " "
					+ departureTzString;
			String arrivalDateTimeStr = DateUtils.formatDateTime(mContext, arrivalDate.getTime(), flags) + " "
					+ arrivalTzString;

			return String.format(template, airlineAndFlightNumber, destinationCity, originAirportCode,
					departureDateTimeStr, destinationAirportCode, arrivalDateTimeStr, destinationGateTerminal);
		}
	}

	public String getFlightShareEmail(FlightTrip trip, FlightLeg firstLeg, FlightLeg lastLeg, List<Traveler> travelers) {
		int numTravelers = travelers.size();
		boolean moreThanOneLeg = firstLeg != lastLeg;

		String originCity = StrUtils.getWaypointCityOrCode(firstLeg.getFirstWaypoint());
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());

		// Construct the body
		StringBuilder body = new StringBuilder();
		body.append(mContext.getString(R.string.share_hi));

		body.append("\n\n");

		int shareTemplateResId;
		if (!moreThanOneLeg) {
			if (numTravelers > 1) {
				shareTemplateResId = R.string.share_flight_one_way_multiple_travelers_TEMPLATE;
			}
			else {
				shareTemplateResId = R.string.share_flight_one_way_TEMPLATE;
			}
		}
		else {
			// Assume round trip for now
			if (numTravelers > 1) {
				shareTemplateResId = R.string.share_flight_round_trip_multiple_travelers_TEMPLATE;
			}
			else {
				shareTemplateResId = R.string.share_flight_round_trip_TEMPLATE;
			}
		}
		body.append(mContext.getString(shareTemplateResId, originCity, destinationCity));

		body.append("\n\n");

		if (trip != null && !TextUtils.isEmpty(trip.getItineraryNumber())) {
			body.append(mContext.getString(R.string.share_flight_itinerary_TEMPLATE, trip.getItineraryNumber()));

			body.append("\n\n");
		}

		if (moreThanOneLeg) {
			body.append(mContext.getString(R.string.share_flight_section_outbound));

			body.append("\n\n");
		}

		addShareLeg(body, firstLeg);

		// Assume only round trips
		if (moreThanOneLeg) {
			body.append("\n\n");

			body.append(mContext.getString(R.string.share_flight_section_return));

			body.append("\n\n");

			addShareLeg(body, lastLeg);
		}

		body.append("\n\n");

		body.append(mContext.getString(R.string.share_travelers_section));

		body.append("\n");

		for (int i = 0; i < numTravelers; i++) {
			Traveler traveler = travelers.get(i);
			body.append(traveler.getFirstName() + " " + traveler.getLastName());
			body.append("\n");
		}

		body.append("\n");

		body.append(mContext.getString(R.string.share_template_long_ad, PointOfSale.getPointOfSale().getAppInfoUrl()));

		return body.toString();
	}

	// Hotels

	public String getHotelShareSubject(String city, LocalDate startDate, LocalDate endDate) {
		String template = mContext.getString(R.string.share_template_subject_hotel);
		String checkIn = JodaUtils.formatLocalDate(mContext, startDate, SHARE_CHECK_IN_FLAGS);
		String checkOut = JodaUtils.formatLocalDate(mContext, endDate, SHARE_CHECK_IN_FLAGS);

		return String.format(template, city, checkIn, checkOut);
	}

	public String getHotelShareTextShort(String hotelName, DateTime startDate, DateTime endDate, String detailsUrl) {
		String template = mContext.getString(R.string.share_template_short_hotel);
		String checkIn = JodaUtils.formatDateTime(mContext, startDate, SHARE_CHECK_IN_FLAGS);
		String checkOut = JodaUtils.formatDateTime(mContext, endDate, SHARE_CHECK_IN_FLAGS);

		return String.format(template, hotelName, checkIn, checkOut, detailsUrl);
	}

	public String getHotelShareTextLong(String hotelName, String address, String phone, LocalDate startDate,
			LocalDate endDate, String detailsUrl) {

		String checkIn = JodaUtils.formatLocalDate(mContext, startDate, LONG_SHARE_DATE_FLAGS);
		String checkOut = JodaUtils.formatLocalDate(mContext, endDate, LONG_SHARE_DATE_FLAGS);
		String downloadUrl = PointOfSale.getPointOfSale().getAppInfoUrl();

		int nights = JodaUtils.daysBetween(startDate, endDate);
		String lengthOfStay = mContext.getResources().getQuantityString(R.plurals.length_of_stay, nights, nights);

		StringBuilder builder = new StringBuilder();
		builder.append(mContext.getString(R.string.share_template_long_hotel_1_greeting, hotelName, lengthOfStay));
		builder.append("\n\n");

		if (checkIn != null || checkOut != null) {
			builder.append(mContext.getString(R.string.share_template_long_hotel_2_checkin_checkout, checkIn, checkOut));
			builder.append("\n\n");
		}

		if (address != null) {
			builder.append(mContext.getString(R.string.share_template_long_hotel_3_address, hotelName, address));
			builder.append("\n\n");
		}

		if (phone != null) {
			builder.append(mContext.getString(R.string.share_template_long_hotel_4_phone, phone));
			builder.append("\n\n");
		}

		if (detailsUrl != null) {
			builder.append(mContext.getString(R.string.share_template_long_hotel_5_more_info, detailsUrl));
			builder.append("\n\n");
		}

		builder.append(mContext.getString(R.string.share_template_long_ad, downloadUrl));

		return builder.toString();
	}

	// Cars

	public String getCarShareSubject(DateTime pickUpDateTime, DateTime dropOffDateTime) {
		String subject;
		if (pickUpDateTime != null && dropOffDateTime != null) {
			String pickUpDate = JodaUtils.formatDateTime(mContext, pickUpDateTime, SHORT_DATE_FLAGS);
			String dropOffDate = JodaUtils.formatDateTime(mContext, dropOffDateTime, SHORT_DATE_FLAGS);

			subject = mContext.getString(R.string.share_template_subject_car, pickUpDate, dropOffDate);
		}
		else {
			subject = mContext.getString(R.string.share_template_subject_car_no_dates);
		}

		return subject;
	}

	public String getCarShareTextShort(Car.Category carCategory, DateTime pickUpDateTime, DateTime dropOffDateTime,
			String vendorName, String vendorAddress) {

		String category = mContext.getString(carCategory.getCategoryResId());
		String pickUpDate = JodaUtils.formatDateTime(mContext, pickUpDateTime, SHORT_DATE_FLAGS);
		String dropOffDate = JodaUtils.formatDateTime(mContext, dropOffDateTime, SHORT_DATE_FLAGS);

		StringBuilder sb = new StringBuilder();

		if (!TextUtils.isEmpty(category)) {
			sb.append(mContext.getString(R.string.share_template_short_car_type, category));
		}

		if (!TextUtils.isEmpty(pickUpDate) && !TextUtils.isEmpty(dropOffDate)) {
			sb.append(mContext.getString(R.string.share_template_short_car_dates, pickUpDate, dropOffDate));
		}

		if (!TextUtils.isEmpty(vendorName)) {
			sb.append("\n");
			sb.append(vendorName);
		}

		if (!TextUtils.isEmpty(vendorAddress)) {
			sb.append("\n");
			sb.append(vendorAddress);
		}

		return sb.toString();
	}

	public String getCarShareTextLong(Car.Category carCategory, DateTime pickUpDateTime, DateTime dropOffDateTime,
			CarVendor vendor, Location pickUpLocation, Location dropOffLocation) {

		StringBuilder sb = new StringBuilder();

		sb.append(mContext.getString(R.string.share_hi));
		sb.append("\n\n");

		if (!TextUtils.isEmpty(vendor.getShortName())) {
			sb.append(mContext.getString(R.string.share_car_start_TEMPLATE, vendor.getShortName()));
			sb.append("\n\n");
		}

		String category = mContext.getString(carCategory.getCategoryResId());
		if (!TextUtils.isEmpty(category)) {
			sb.append(mContext.getString(R.string.share_car_vehicle_TEMPLATE, category));
			sb.append("\n");
		}

		if (pickUpDateTime != null) {
			String pickUpDate = JodaUtils.formatDateTime(mContext, pickUpDateTime, SHORT_DATE_FLAGS);
			String pickUpTime = JodaUtils.formatDateTime(mContext, pickUpDateTime, TIME_FLAGS) + " "
					+ JodaUtils.formatTimeZone(pickUpDateTime);
			sb.append(mContext.getString(R.string.share_car_pickup_TEMPLATE, pickUpDate, pickUpTime));
			sb.append("\n");
		}

		if (dropOffDateTime != null) {
			String dropOffDate = JodaUtils.formatDateTime(mContext, dropOffDateTime, SHORT_DATE_FLAGS);
			String dropOffTime = JodaUtils.formatDateTime(mContext, dropOffDateTime, TIME_FLAGS) + " "
					+ JodaUtils.formatTimeZone(dropOffDateTime);
			sb.append(mContext.getString(R.string.share_car_dropoff_TEMPLATE, dropOffDate, dropOffTime));
			sb.append("\n\n");
		}

		String localPhone = vendor.getLocalPhone();
		String vendorPhone = vendor.getTollFreePhone();

		boolean hasDiffLocations = pickUpLocation != null && !pickUpLocation.equals(dropOffLocation);

		if (pickUpLocation != null) {
			if (!hasDiffLocations) {
				sb.append(mContext.getString(R.string.share_car_location_section));
			}
			else {
				sb.append(mContext.getString(R.string.share_car_pickup_location_section));
			}

			sb.append("\n");
			sb.append(pickUpLocation.toLongFormattedString());
			sb.append("\n");

			if (!TextUtils.isEmpty(localPhone)) {
				sb.append(localPhone);
				sb.append("\n");
			}

			if (!TextUtils.isEmpty(vendorPhone)) {
				sb.append(vendorPhone);
				sb.append("\n");
			}

			sb.append("\n");
		}

		if (hasDiffLocations && dropOffLocation != null) {
			sb.append(mContext.getString(R.string.share_car_dropoff_location_section));
			sb.append("\n");
			sb.append(dropOffLocation.toLongFormattedString());
			sb.append("\n");

			if (!TextUtils.isEmpty(vendorPhone)) {
				sb.append(vendorPhone);
				sb.append("\n");
			}

			sb.append("\n");
		}

		sb.append(mContext.getString(R.string.share_template_long_ad, PointOfSale.getPointOfSale().getAppInfoUrl()));

		return sb.toString();
	}

	// Activities

	public String getActivityShareSubject(String title) {
		String subject;
		if (!TextUtils.isEmpty(title)) {
			subject = mContext.getString(R.string.share_template_subject_activity, title);
		}
		else {
			subject = mContext.getString(R.string.share_template_subject_activity_no_title);
		}

		return subject;
	}

	public String getActivityShareTextShort(String title, DateTime validDateTime, DateTime expirationDateTime) {
		String validDate = JodaUtils.formatDateTime(mContext, validDateTime, SHARE_DATE_FLAGS);
		String expirationDate = JodaUtils.formatDateTime(mContext, expirationDateTime, SHARE_DATE_FLAGS);

		StringBuilder sb = new StringBuilder();

		if (!TextUtils.isEmpty(title)) {
			sb.append(mContext.getString(R.string.share_template_short_activity_title, title));
		}

		if (!TextUtils.isEmpty(validDate)) {
			sb.append("\n");
			sb.append(mContext.getString(R.string.share_template_short_activity_valid, validDate));
		}

		if (!TextUtils.isEmpty(expirationDate)) {
			sb.append("\n");
			sb.append(mContext.getString(R.string.share_template_short_activity_expires, expirationDate));
		}

		return sb.toString();
	}

	public String getActivityShareTextLong(String title, DateTime validDateTime, DateTime expirationDateTime,
			List<Traveler> travelers) {

		String validDate = JodaUtils.formatDateTime(mContext, validDateTime, SHARE_DATE_FLAGS);
		String expirationDate = JodaUtils.formatDateTime(mContext, expirationDateTime, SHARE_DATE_FLAGS);
		String downloadUrl = PointOfSale.getPointOfSale().getAppInfoUrl();

		StringBuilder sb = new StringBuilder();

		if (!TextUtils.isEmpty(title)) {
			sb.append(mContext.getString(R.string.share_template_long_activity_title, title));
			sb.append("\n");
		}

		if (!TextUtils.isEmpty(validDate)) {
			sb.append("\n");
			sb.append(mContext.getString(R.string.share_template_long_activity_valid, validDate));
		}

		if (!TextUtils.isEmpty(expirationDate)) {
			sb.append("\n");
			sb.append(mContext.getString(R.string.share_template_long_activity_expires, expirationDate));
		}

		final int guestCount = travelers.size();
		final String[] guests = new String[guestCount];
		for (int i = 0; i < guestCount; i++) {
			guests[i] = travelers.get(i).getFullName();
		}

		sb.append("\n\n");
		sb.append(mContext.getString(R.string.share_template_long_activity_guests, TextUtils.join("\n", guests)));

		sb.append("\n\n");
		sb.append(mContext.getString(R.string.share_template_long_ad, downloadUrl));

		return sb.toString();
	}

	// Helper methods

	private void appendLabelValue(Context context, StringBuilder sb, int labelStrId, String value) {
		appendLabelValue(sb, context.getString(labelStrId), value);
	}

	private void appendLabelValue(StringBuilder sb, String label, String value) {
		sb.append(label);
		sb.append(": ");
		sb.append(value);
	}

	private void addShareLeg(StringBuilder sb, FlightLeg flightLeg) {
		Resources res = mContext.getResources();
		int segCount = flightLeg.getSegmentCount();

		for (int a = 0; a < segCount; a++) {
			Flight flight = flightLeg.getSegment(a);

			if (a > 0) {
				Layover layover = new Layover(flightLeg.getSegment(a - 1), flight);
				String duration = DateTimeUtils.formatDuration(res, layover.mDuration);
				String waypoint = StrUtils.formatWaypoint(flight.mOrigin);
				sb.append(Html.fromHtml(mContext.getString(R.string.layover_duration_location_TEMPLATE, duration,
						waypoint)));
				sb.append("\n\n");
			}

			sb.append(mContext.getString(R.string.path_template, formatAirport(flight.mOrigin.getAirport()),
					formatAirport(flight.mDestination.getAirport())));
			sb.append("\n");
			Date start = DateTimeUtils.getTimeInLocalTimeZone(flight.mOrigin.getBestSearchDateTime());
			sb.append(DateUtils.formatDateTime(mContext, start.getTime(), DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY));
			sb.append("\n");
			Date end = DateTimeUtils.getTimeInLocalTimeZone(flight.mDestination.getBestSearchDateTime());
			String departureTzString = FormatUtils.formatTimeZone(flightLeg.getFirstWaypoint().getAirport(), start,
					MAX_TIMEZONE_LENGTH);
			String arrivalTzString = FormatUtils.formatTimeZone(flightLeg.getLastWaypoint().getAirport(), end,
					MAX_TIMEZONE_LENGTH);
			sb.append(DateUtils.formatDateTime(mContext, start.getTime(), DateUtils.FORMAT_SHOW_TIME) + " "
					+ departureTzString);
			sb.append(" - ");
			sb.append(DateUtils.formatDateTime(mContext, end.getTime(), DateUtils.FORMAT_SHOW_TIME) + " "
					+ arrivalTzString);
			sb.append("\n");
			sb.append(FormatUtils.formatFlightNumber(flight, mContext));

			if (a + 1 != segCount) {
				sb.append("\n\n");
			}
		}
	}

	private String formatAirport(Airport airport) {
		if (!TextUtils.isEmpty(airport.mCity)) {
			return airport.mCity + " (" + airport.mAirportCode + ")";
		}
		else {
			return airport.mAirportCode;
		}
	}
}
