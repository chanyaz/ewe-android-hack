package com.expedia.bookings.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripFlight;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Layover;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

public class ShareUtils {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final int LONG_SHARE_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
			| DateUtils.FORMAT_SHOW_WEEKDAY;
	private static final int SHARE_CHECK_IN_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
			| DateUtils.FORMAT_ABBREV_WEEKDAY;
	private static final int SHARE_CHECK_OUT_FLAGS = LONG_SHARE_DATE_FLAGS | DateUtils.FORMAT_ABBREV_WEEKDAY;

	private static final int MAX_TIMEZONE_LENGTH = 6;

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

		return null;
	}

	public String getShareTextShort(ItinCardData itinCardData) {
		if (itinCardData instanceof ItinCardDataFlight) {
			return getFlightShareTextShort((ItinCardDataFlight) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataHotel) {
			return getHotelShareTextShort((ItinCardDataHotel) itinCardData);
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
		return getHotelShareSubject(itinCardData.getPropertyCity(), itinCardData.getStartDate(),
				itinCardData.getEndDate());
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

		return getHotelShareTextLong(hotelName, address, phone, startDate, endDate, detailsUrl);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	// Share methods

	// Flights

	private String getFlightShareSubject(FlightLeg firstLeg, FlightLeg lastLeg) {
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());

		long start = DateTimeUtils.getTimeInLocalTimeZone(firstLeg.getFirstWaypoint().getMostRelevantDateTime())
				.getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(
				lastLeg.getLastWaypoint().getMostRelevantDateTime()).getTime();
		String dateRange = DateUtils.formatDateRange(mContext, start, end, DateUtils.FORMAT_NUMERIC_DATE
				| DateUtils.FORMAT_SHOW_DATE);

		return mContext.getString(R.string.share_template_subject_flight, destinationCity, dateRange);
	}

	private String getFlightShareTextShort(FlightLeg leg) {
		if (leg == null || leg.getLastWaypoint() == null || leg.getLastWaypoint().getAirport() == null) {
			return null;
		}

		String airlineAndFlightNumber = FormatUtils.formatFlightNumberShort(
				leg.getSegment(leg.getSegmentCount() - 1), mContext);
		String destinationCity = leg.getLastWaypoint().getAirport().mCity;
		String destinationAirportCode = leg.getLastWaypoint().getAirport().mAirportCode;
		String originAirportCode = leg.getFirstWaypoint().getAirport().mAirportCode;
		String destinationGateTerminal = getTerminalGateString(leg.getLastWaypoint());

		Calendar departureCal = leg.getFirstWaypoint().getBestSearchDateTime();
		Calendar arrivalCal = leg.getLastWaypoint().getBestSearchDateTime();

		Date departureDate = DateTimeUtils.getTimeInLocalTimeZone(departureCal);
		Date arrivalDate = DateTimeUtils.getTimeInLocalTimeZone(arrivalCal);

		String departureTzString = FormatUtils.formatTimeZone(leg.getFirstWaypoint().getAirport(), departureDate,
				MAX_TIMEZONE_LENGTH);
		String arrivalTzString = FormatUtils.formatTimeZone(leg.getLastWaypoint().getAirport(), arrivalDate,
				MAX_TIMEZONE_LENGTH);

		//The story contains format strings, but we don't want to bone our international customers
		DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
		DateFormat timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM);

		if (PointOfSale.getPointOfSale().getThreeLetterCountryCode().equalsIgnoreCase("USA")) {
			String dateFormatStr = "M/dd/yy";
			String timeFormatStr = "h:mma";

			((SimpleDateFormat) dateFormat).applyPattern(dateFormatStr);
			((SimpleDateFormat) timeFormat).applyPattern(timeFormatStr);
		}

		String departureDateStr = dateFormat.format(departureDate);
		String departureTimeStr = timeFormat.format(departureDate) + " " + departureTzString;
		String departureDateTimeStr = departureTimeStr + " " + departureDateStr;

		String arrivalDateStr = dateFormat.format(departureDate);
		String arrivalTimeStr = timeFormat.format(arrivalDate) + " " + arrivalTzString;
		String arrivalDateTimeStr = arrivalTimeStr + " " + arrivalDateStr;

		//single day
		if (leg.getDaySpan() == 0) {
			String template = mContext.getString(R.string.share_template_short_flight_sameday);

			return String.format(template, airlineAndFlightNumber, destinationCity, departureDateStr,
					originAirportCode,
					departureTimeStr, destinationAirportCode, arrivalTimeStr, destinationGateTerminal);
		}
		//multi day
		else {
			String template = mContext.getString(R.string.share_template_short_flight_multiday);

			return String.format(template, airlineAndFlightNumber, destinationCity, originAirportCode,
					departureDateTimeStr, destinationAirportCode, arrivalDateTimeStr, destinationGateTerminal);
		}
	}

	private String getFlightShareEmail(FlightTrip trip, FlightLeg firstLeg, FlightLeg lastLeg, List<Traveler> travelers) {
		int numTravelers = travelers.size();
		boolean moreThanOneLeg = firstLeg != lastLeg;

		String originCity = StrUtils.getWaypointCityOrCode(firstLeg.getFirstWaypoint());
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());

		// Construct the body
		StringBuilder body = new StringBuilder();
		body.append(mContext.getString(R.string.share_hi));

		body.append("\n\n");

		if (!moreThanOneLeg) {
			body.append(mContext.getString(R.string.share_flight_one_way_TEMPLATE, originCity, destinationCity));
		}
		else {
			// Assume round trip for now
			body.append(mContext.getString(R.string.share_flight_round_trip_TEMPLATE, originCity, destinationCity));
		}

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

	private String getHotelShareSubject(String city, DateTime startDate, DateTime endDate) {
		String template = mContext.getString(R.string.share_template_subject_hotel);
		String checkIn = startDate.formatTime(mContext, SHARE_CHECK_IN_FLAGS);
		String checkOut = endDate.formatTime(mContext, SHARE_CHECK_IN_FLAGS);

		return String.format(template, city, checkIn, checkOut);
	}

	private String getHotelShareTextShort(String hotelName, DateTime startDate, DateTime endDate, String detailsUrl) {
		String template = mContext.getString(R.string.share_template_short_hotel);
		String checkIn = startDate.formatTime(mContext, SHARE_CHECK_IN_FLAGS);
		String checkOut = endDate.formatTime(mContext, SHARE_CHECK_OUT_FLAGS);

		return String.format(template, hotelName, checkIn, checkOut, detailsUrl);
	}

	private String getHotelShareTextLong(String hotelName, String address, String phone, DateTime startDate,
			DateTime endDate, String detailsUrl) {

		String checkIn = startDate.formatTime(mContext, LONG_SHARE_DATE_FLAGS);
		String checkOut = endDate.formatTime(mContext, LONG_SHARE_DATE_FLAGS);
		String downloadUrl = PointOfSale.getPointOfSale().getAppInfoUrl();

		int nights = (int) CalendarUtils.getDaysBetween(startDate.getCalendar(), endDate.getCalendar());
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

	// Helper methods

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
			long start = DateTimeUtils.getTimeInLocalTimeZone(flight.mOrigin.getBestSearchDateTime()).getTime();
			sb.append(DateUtils.formatDateTime(mContext, start, DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY));
			sb.append("\n");
			long end = DateTimeUtils.getTimeInLocalTimeZone(flight.mDestination.getBestSearchDateTime()).getTime();
			sb.append(DateUtils.formatDateRange(mContext, start, end, DateUtils.FORMAT_SHOW_TIME));
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

	private String getTerminalGateString(Waypoint waypoint) {
		Resources res = mContext.getResources();
		if (!waypoint.hasGate() && !waypoint.hasTerminal()) {
			//no gate or terminal info
			return res.getString(R.string.gate_number_only_TEMPLATE, res.getString(R.string.to_be_determined_abbrev));
		}
		else if (waypoint.hasGate()) {
			//gate only
			return res.getString(R.string.gate_number_only_TEMPLATE, waypoint.getGate());
		}
		else if (waypoint.hasTerminal()) {
			//terminal only
			return res.getString(R.string.terminal_but_no_gate_TEMPLATE, waypoint.getTerminal());
		}
		else {
			//We have gate and terminal info
			return res.getString(R.string.generic_terminal_TEMPLATE, waypoint.getTerminal(), waypoint.getGate());
		}
	}
}
