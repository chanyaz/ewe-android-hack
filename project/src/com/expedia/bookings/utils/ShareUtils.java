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
import com.expedia.bookings.data.*;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.*;
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
		return getHotelShareSubject(itinCardData.getPropertyCity(), itinCardData.getStartDate(),
				itinCardData.getEndDate());
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

		return getHotelShareTextLong(hotelName, address, phone, startDate, endDate, detailsUrl);
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

	public String getFlightShareEmail(FlightTrip trip, FlightLeg firstLeg, FlightLeg lastLeg, List<Traveler> travelers) {
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

	public String getHotelShareSubject(String city, DateTime startDate, DateTime endDate) {
		String template = mContext.getString(R.string.share_template_subject_hotel);
		String checkIn = startDate.formatTime(mContext, SHARE_CHECK_IN_FLAGS);
		String checkOut = endDate.formatTime(mContext, SHARE_CHECK_IN_FLAGS);

		return String.format(template, city, checkIn, checkOut);
	}

	public String getHotelShareTextShort(String hotelName, DateTime startDate, DateTime endDate, String detailsUrl) {
		String template = mContext.getString(R.string.share_template_short_hotel);
		String checkIn = startDate.formatTime(mContext, SHARE_CHECK_IN_FLAGS);
		String checkOut = endDate.formatTime(mContext, SHARE_CHECK_OUT_FLAGS);

		return String.format(template, hotelName, checkIn, checkOut, detailsUrl);
	}

	public String getHotelShareTextLong(String hotelName, String address, String phone, DateTime startDate,
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

	public String getHotelConfirmationShareSubject(SearchParams searchParams, Property property) {
		DateFormat dateFormatter = new SimpleDateFormat("MM/dd");

		Date checkIn = searchParams.getCheckInDate().getTime();
		Date checkOut = searchParams.getCheckOutDate().getTime();
		String dateStart = dateFormatter.format(checkIn);
		String dateEnd = dateFormatter.format(checkOut);

		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(property.getName())) {
			sb.append(property.getName());
			sb.append(" ");
		}

		if (!TextUtils.isEmpty(dateStart) && !TextUtils.isEmpty(dateEnd)) {
			sb.append(mContext.getString(R.string.share_subject_template_dates, dateStart, dateEnd));
		}

//		if (sb.length() == 0) {
//			String template = mContext.getString(R.string.share_subject_dash_template);
//			String title = mContext.getString(R.string.share_subject_title);
//			return String.format(template, title, sb.toString());
//		}
//		else {
//			return mContext.getString(R.string.share_subject_title);
//		}
		return "74";
	}

	public String getHotelConfirmationShareText(SearchParams searchParams, Property property,
			BookingResponse bookingResponse, BillingInfo billingInfo, Rate rate, Rate discountRate) {

		Resources res = mContext.getResources();

		DateFormat fullDateFormatter = android.text.format.DateFormat.getMediumDateFormat(mContext);
		DateFormat dayFormatter = new SimpleDateFormat("EEE");

		Date checkIn = searchParams.getCheckInDate().getTime();
		Date checkOut = searchParams.getCheckOutDate().getTime();

		// Create the body
		StringBuilder body = new StringBuilder();
		body.append(mContext.getString(R.string.share_body_start));
		body.append("\n\n");

		body.append(property.getName());
		body.append("\n");
		body.append(StrUtils.formatAddress(property.getLocation()));
		body.append("\n\n");

		if (!TextUtils.isEmpty(bookingResponse.getHotelConfNumber())) {
			appendLabelValue(mContext, body, R.string.confirmation_number, bookingResponse.getHotelConfNumber());
			body.append("\n");
		}
		appendLabelValue(mContext, body, R.string.itinerary_number, bookingResponse.getItineraryId());
		body.append("\n\n");

		String first;
		String last;
		Traveler traveler = Db.getUser() == null ? null : Db.getUser().getPrimaryTraveler();
		if (traveler != null) {
			first = traveler.getFirstName();
			last = traveler.getLastName();
		}
		else {
			first = billingInfo.getFirstName();
			last = billingInfo.getLastName();
		}
		appendLabelValue(mContext, body, R.string.name, mContext.getString(R.string.name_template, first, last));

		body.append("\n");
		appendLabelValue(mContext, body, R.string.CheckIn,
				dayFormatter.format(checkIn) + ", " + fullDateFormatter.format(checkIn));
		body.append("\n");
		appendLabelValue(mContext, body, R.string.CheckOut,
				dayFormatter.format(checkOut) + ", " + fullDateFormatter.format(checkOut));
		body.append("\n");
		int numDays = searchParams.getStayDuration();
		appendLabelValue(mContext, body, R.string.stay_duration,
				res.getQuantityString(R.plurals.length_of_stay, numDays, numDays));
		body.append("\n\n");

		appendLabelValue(mContext, body, R.string.room_type, Html.fromHtml(rate.getRoomDescription()).toString());
		body.append("\n");
		appendLabelValue(mContext, body, R.string.bed_type, rate.getRatePlanName());
		body.append("\n");
		appendLabelValue(mContext, body, R.string.adults, searchParams.getNumAdults() + "");
		body.append("\n");
		appendLabelValue(mContext, body, R.string.children, searchParams.getNumChildren() + "");
		body.append("\n\n");

		if (rate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dayFormatter.format(date) + ", " + fullDateFormatter.format(date);
				Money amount = breakdown.getAmount();
				if (amount.isZero()) {
					appendLabelValue(body, mContext.getString(R.string.room_rate_template, dateStr),
							mContext.getString(R.string.free));
				}
				else {
					appendLabelValue(body, mContext.getString(R.string.room_rate_template, dateStr),
							amount.getFormattedMoney());
				}
				body.append("\n");
			}
			body.append("\n\n");
		}

		if (rate.getTotalAmountBeforeTax() != null) {
			appendLabelValue(mContext, body, R.string.subtotal, rate.getTotalAmountBeforeTax().getFormattedMoney());
			body.append("\n");
		}

		Money totalSurcharge = new Money(rate.getTotalSurcharge());
		Money extraGuestFee = rate.getExtraGuestFee();
		if (extraGuestFee != null) {
			appendLabelValue(mContext, body, R.string.extra_guest_charge, extraGuestFee.getFormattedMoney());
			body.append("\n");
			if (totalSurcharge != null) {
				totalSurcharge = totalSurcharge.copy();
				totalSurcharge.subtract(extraGuestFee);
			}
		}
		if (totalSurcharge != null) {
			if (totalSurcharge.isZero()) {
				appendLabelValue(mContext, body, R.string.taxes_and_fees, mContext.getString(R.string.included));
			}
			else {
				appendLabelValue(mContext, body, R.string.taxes_and_fees, totalSurcharge.getFormattedMoney());
			}

			body.append("\n");
		}

		if (discountRate != null) {
			Money discount = new Money(discountRate.getTotalAmountAfterTax());
			discount.subtract(rate.getTotalAmountAfterTax());
			appendLabelValue(mContext, body, R.string.discount, discount.getFormattedMoney());
			body.append("\n");
			appendLabelValue(mContext, body, R.string.Total, discountRate.getTotalAmountAfterTax().getFormattedMoney());
			body.append("\n");
		}
		else {
			if (rate.getTotalAmountAfterTax() != null) {
				body.append("\n");
				appendLabelValue(mContext, body, R.string.Total, rate.getTotalAmountAfterTax().getFormattedMoney());
			}
		}

		Policy cancellationPolicy = rate.getRateRules().getPolicy(Policy.TYPE_CANCEL);
		if (cancellationPolicy != null) {
			body.append("\n\n");
			body.append(mContext.getString(R.string.cancellation_policy));
			body.append("\n");
			body.append(Html.fromHtml(cancellationPolicy.getDescription()));
		}

		body.append("\n\n");
		body.append(ConfirmationUtils.determineContactText(mContext));

		return body.toString();
	}

	// Cars

	public String getCarShareSubject(DateTime pickUpDateTime, DateTime dropOffDateTime) {
		String subject;
		if (pickUpDateTime != null && dropOffDateTime != null) {
			String pickUpDate = pickUpDateTime.formatTime(mContext, SHORT_DATE_FLAGS);
			String dropOffDate = dropOffDateTime.formatTime(mContext, SHORT_DATE_FLAGS);

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
		String pickUpDate = pickUpDateTime.formatTime(mContext, SHORT_DATE_FLAGS);
		String dropOffDate = dropOffDateTime.formatTime(mContext, SHORT_DATE_FLAGS);

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
			String pickUpDate = pickUpDateTime.formatTime(mContext, SHORT_DATE_FLAGS);
			String pickUpTime = pickUpDateTime.formatTime(mContext, TIME_FLAGS) + " " + pickUpDateTime.formatTimeZone();
			sb.append(mContext.getString(R.string.share_car_pickup_TEMPLATE, pickUpDate, pickUpTime));
			sb.append("\n");
		}

		if (dropOffDateTime != null) {
			String dropOffDate = dropOffDateTime.formatTime(mContext, SHORT_DATE_FLAGS);
			String dropOffTime = dropOffDateTime.formatTime(mContext, TIME_FLAGS) + " "
					+ dropOffDateTime.formatTimeZone();
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
		String validDate = validDateTime.formatTime(mContext, SHARE_DATE_FLAGS);
		String expirationDate = expirationDateTime.formatTime(mContext, SHARE_DATE_FLAGS);

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

		String validDate = validDateTime.formatTime(mContext, SHARE_DATE_FLAGS);
		String expirationDate = expirationDateTime.formatTime(mContext, SHARE_DATE_FLAGS);
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
