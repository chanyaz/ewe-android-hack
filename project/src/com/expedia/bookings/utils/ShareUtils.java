package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.FacebookShareActivity;
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
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.widget.itin.FlightItinContentGenerator;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
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
			return getFlightShareSubject((ItinCardDataFlight) itinCardData);
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

	public String getFlightShareSubject(ItinCardDataFlight itinCardData) {
		int travelerCount = 1;//default
		TripFlight tripFlight = (TripFlight) itinCardData.getTripComponent();
		if (tripFlight != null && tripFlight.getTravelers() != null) {
			travelerCount = tripFlight.getTravelers().size();
		}
		String travelerName = tripFlight.getTravelers().get(0).getFirstName();
		boolean isShared = itinCardData.isSharedItin();
		return getFlightShareSubject(itinCardData.getFlightLeg(), travelerCount, isShared, travelerName);
	}

	public String getFlightShareSubject(FlightTrip trip, int travelerCount) {
		//This method is called only for sharing in flight confirmation screen. So the isShared & travelerName would not be relevant.
		return getFlightShareSubject(trip.getLeg(0), trip.getLeg(trip.getLegCount() - 1), travelerCount, false, null);
	}

	public String getFlightShareSubject(FlightLeg leg, int travelerCount, boolean isShared, String travelerName) {
		return getFlightShareSubject(leg, leg, travelerCount, isShared, travelerName);
	}

	public String getHotelShareSubject(ItinCardDataHotel itinCardData) {
		boolean isShared = itinCardData.isSharedItin();
		TripHotel hotel = (TripHotel) itinCardData.getTripComponent();
		String travelerName = hotel.getPrimaryTraveler().getFirstName();
		return getHotelShareSubject(itinCardData.getPropertyCity(), itinCardData.getStartDate().toLocalDate(),
				itinCardData.getEndDate().toLocalDate(), isShared, travelerName);
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

			TripFlight flight = (TripFlight) itinCardData.getTripComponent();
			List<Traveler> travelers = flight.getTravelers();
			String travelerName = travelers.get(0).getFirstName();
			boolean isShared = itinCardData.isSharedItin();

			return getFlightShareTextShort(itinCardData.getFlightLeg(), itinCardData.getSharableDetailsUrl(), isShared,
					travelerName);
		}
		return null;
	}

	public String getHotelShareTextShort(ItinCardDataHotel itinCardData) {
		// This is so we can share a shared itin. the API does not return the shareable URL when we hit
		// it with a shared itin link. as such, we stuff the shareable url in the parent trip and just use
		// that if the component link is not present (in the case of a shared itin)
		String urlFromParentTrip = itinCardData.getTripComponent().getParentTrip().getShareInfo().getSharableUrl();
		String urlFromComponent = itinCardData.getSharableDetailsUrl();
		String url = TextUtils.isEmpty(urlFromComponent) ? urlFromParentTrip : urlFromComponent;
		boolean isShared = itinCardData.isSharedItin();

		TripHotel hotel = (TripHotel) itinCardData.getTripComponent();
		String travelerName = hotel.getPrimaryTraveler().getFirstName();

		return getHotelShareTextShort(itinCardData.getPropertyName(), itinCardData.getStartDate(), url, isShared, travelerName);
	}

	public String getCarShareTextShort(ItinCardDataCar itinCardData) {
		Car.Category category = itinCardData.getCar().getCategory();
		DateTime pickUpDate = itinCardData.getPickUpDate();
		DateTime dropOffDate = itinCardData.getDropOffDate();
		String vendorName = itinCardData.getVendorName();
		String vendorAddress = itinCardData.getRelevantVendorLocation().toLongFormattedString();
		/*
		 *  #2139. SharableURL/Importing shared itin is currently restricted to only hotels and flights.
		 *  So currently just share the webDetailsURL. When we support itinSharing for other trips just use the itinCardData.getSharableDetailsUrl() below
		 */
		String sharableDetailsURL = itinCardData.getDetailsUrl();

		return getCarShareTextShort(category, pickUpDate, dropOffDate, vendorName, vendorAddress, sharableDetailsURL);
	}

	public String getActivityShareTextShort(ItinCardDataActivity itinCardData) {
		/*
		 *  #2139. SharableURL/Importing shared itin is currently restricted to only hotels and flights.
		 *  So currently just share the webDetailsURL. When we support itinSharing for other trips just use the itinCardData.getSharableDetailsUrl() below
		 */
		return getActivityShareTextShort(itinCardData.getTitle(), itinCardData.getValidDate(),
				itinCardData.getExpirationDate(), itinCardData.getDetailsUrl());
	}

	// SHARE TEXT LONG

	public String getFlightShareTextLong(ItinCardDataFlight itinCardData) {
		TripFlight tripFlight = (TripFlight) itinCardData.getTripComponent();

		// This is so we can share a shared itin. the API does not return the shareable URL when we hit
		// it with a shared itin link. as such, we stuff the shareable url in the parent trip and just use
		// that if the component link is not present (in the case of a shared itin)
		String urlFromParentTrip = tripFlight.getParentTrip().getShareInfo().getSharableUrl();
		String urlFromComponent = itinCardData.getSharableDetailsUrl();
		String url = TextUtils.isEmpty(urlFromComponent) ? urlFromParentTrip : urlFromComponent;
		TripFlight flight = (TripFlight) itinCardData.getTripComponent();
		List<Traveler> travelers = flight.getTravelers();
		String travelerName = travelers.get(0).getFirstName();
		boolean isShared = itinCardData.isSharedItin();

		return getFlightShareEmail(itinCardData.getFlightLeg(), tripFlight.getTravelers(), url, isShared, travelerName);
	}

	public String getFlightShareEmail(FlightTrip trip, List<Traveler> travelers) {
		//This method is called only for sharing in flight confirmation screen. So the isShared & travelerName would not be relevant.
		return getFlightShareEmail(trip, trip.getLeg(0), trip.getLeg(trip.getLegCount() - 1), travelers, null, false, null);
	}

	public String getFlightShareEmail(FlightTrip trip, List<Traveler> travelers, String sharableDetailsURL) {
		return getFlightShareEmail(trip, trip.getLeg(0), trip.getLeg(trip.getLegCount() - 1), travelers,
				sharableDetailsURL, false, null);
	}

	public String getFlightShareEmail(FlightLeg leg, List<Traveler> travelers, String sharableDetailsURL, boolean isShared, String travelerFirstName) {
		return getFlightShareEmail(null, leg, leg, travelers, sharableDetailsURL, isShared, travelerFirstName);
	}

	public String getHotelShareTextLong(ItinCardDataHotel itinCardData) {
		String hotelName = itinCardData.getPropertyName();
		String address = itinCardData.getAddressString();
		String phone = itinCardData.getRelevantPhone();
		DateTime startDate = itinCardData.getStartDate();
		DateTime endDate = itinCardData.getEndDate();
		String sharableDetailsUrl = itinCardData.getSharableDetailsUrl();
		boolean isShared = itinCardData.isSharedItin();
		TripHotel hotel = (TripHotel) itinCardData.getTripComponent();
		String travelerName = hotel.getPrimaryTraveler().getFirstName();

		return getHotelShareTextLong(hotelName, address, phone, startDate.toLocalDate(), endDate.toLocalDate(),
				sharableDetailsUrl, isShared, travelerName);
	}

	public String getCarShareTextLong(ItinCardDataCar itinCardData) {
		Car.Category category = itinCardData.getCar().getCategory();
		DateTime pickUpDate = itinCardData.getPickUpDate();
		DateTime dropOffDate = itinCardData.getDropOffDate();
		CarVendor vendor = itinCardData.getCar().getVendor();
		Location pickUpLocation = itinCardData.getPickUpLocation();
		Location dropOffLocation = itinCardData.getDropOffLocation();
		/*
		 *  #2139. SharableURL/Importing shared itin is currently restricted to only hotels and flights.
		 *  So currently just share the webDetailsURL. When we support itinSharing for other trips just use the itinCardData.getSharableDetailsUrl() below
		 */
		String sharableDetailsURL = itinCardData.getDetailsUrl();

		return getCarShareTextLong(category, pickUpDate, dropOffDate, vendor, pickUpLocation, dropOffLocation,
				sharableDetailsURL);
	}

	public String getActivityShareTextLong(ItinCardDataActivity itinCardData) {
		/*
		 *  #2139. SharableURL/Importing shared itin is currently restricted to only hotels and flights.
		 *  So currently just share the webDetailsURL. When we support itinSharing for other trips just use the itinCardData.getSharableDetailsUrl() below
		 */
		return getActivityShareTextLong(itinCardData.getTitle(), itinCardData.getValidDate(),
				itinCardData.getExpirationDate(), itinCardData.getTravelers(), itinCardData.getGuestCount(),
				itinCardData.getDetailsUrl());
	}

	// Share methods

	// Flights

	public String getFlightShareSubject(FlightLeg firstLeg, FlightLeg lastLeg, int travelerCount, boolean isShared, String travelerName) {
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());

		long start = DateTimeUtils.getTimeInLocalTimeZone(firstLeg.getFirstWaypoint().getMostRelevantDateTime())
				.getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(
				lastLeg.getLastWaypoint().getMostRelevantDateTime()).getTime();
		String dateRange = DateUtils.formatDateRange(mContext, start, end, DateUtils.FORMAT_NUMERIC_DATE
				| DateUtils.FORMAT_SHOW_DATE);

		int emailSubjectResId = isShared ? R.string.share_template_subject_flight_reshare
				: R.string.share_template_subject_flight;
		if (travelerCount > 1) {
			emailSubjectResId = isShared ? R.string.share_template_subject_flight_reshare
					: R.string.share_template_subject_flight_multiple_travelers;
		}

		if (isShared) {
			return mContext.getString(emailSubjectResId, travelerName, destinationCity, dateRange);
		}
		else {
			return mContext.getString(emailSubjectResId, destinationCity, dateRange);
		}
	}

	public String getFlightShareTextShort(FlightLeg leg, String shareableDetailsURL, boolean isShared, String travelerFirstName) {
		if (leg == null || leg.getLastWaypoint() == null || leg.getLastWaypoint().getAirport() == null) {
			return null;
		}

		String destinationCity = leg.getLastWaypoint().getAirport().mCity;
		Calendar departureCal = leg.getFirstWaypoint().getBestSearchDateTime();
		Date departureDate = DateTimeUtils.getTimeInLocalTimeZone(departureCal);

		if (!isShared) {
			String template = mContext.getString(R.string.share_msg_template_short_flight);
			String departureDateStr = DateUtils.formatDateTime(mContext, departureDate.getTime(),
					DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
			return String.format(template, destinationCity, departureDateStr, shareableDetailsURL);
		}
		else {
			// This is a reshare, hence append the primaryTraveler's FirstName to the share message.
			String template = mContext.getString(R.string.share_msg_template_short_flight_reshare);
			String departureDateStr = DateUtils.formatDateTime(mContext, departureDate.getTime(),
					DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
			return String.format(template, travelerFirstName, destinationCity, departureDateStr, shareableDetailsURL);
		}
	}

	public String getFlightShareEmail(FlightTrip trip, FlightLeg firstLeg, FlightLeg lastLeg, List<Traveler> travelers,
			String sharableDetailsURL, boolean isShared, String travelerFirstName) {
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
				shareTemplateResId = isShared ? R.string.share_flight_one_way_multiple_travelers_TEMPLATE_reshare
						: R.string.share_flight_one_way_multiple_travelers_TEMPLATE;
			}
			else {
				shareTemplateResId = isShared ? R.string.share_flight_one_way_TEMPLATE_reshare
						: R.string.share_flight_one_way_TEMPLATE;
			}
		}
		else {
			// Assume round trip for now
			if (numTravelers > 1) {
				shareTemplateResId = isShared ? R.string.share_flight_round_trip_multiple_travelers_TEMPLATE_reshare
						: R.string.share_flight_round_trip_multiple_travelers_TEMPLATE;
			}
			else {
				shareTemplateResId = isShared ? R.string.share_flight_round_trip_TEMPLATE_reshare
						: R.string.share_flight_round_trip_TEMPLATE;
			}
		}
		if (isShared) {
			body.append(mContext.getString(shareTemplateResId, travelerFirstName, originCity, destinationCity));
		}
		else {
			body.append(mContext.getString(shareTemplateResId, originCity, destinationCity));
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

		if (!TextUtils.isEmpty(sharableDetailsURL)) {
			body.append("\n");
			body.append(mContext.getString(R.string.share_link_section, sharableDetailsURL));
			body.append("\n");
		}

		body.append("\n");

		//1683. VSC Don't show Android App crossSell text and link.
		if (!ExpediaBookingApp.IS_VSC) {
			body.append(mContext.getString(R.string.share_template_long_ad, PointOfSale.getPointOfSale()
					.getAppInfoUrl()));
		}

		return body.toString();
	}

	// Hotels

	public String getHotelShareSubject(String city, LocalDate startDate, LocalDate endDate, boolean isShared,
			String travelerName) {
		String checkIn = JodaUtils.formatLocalDate(mContext, startDate, SHARE_CHECK_IN_FLAGS);
		String checkOut = JodaUtils.formatLocalDate(mContext, endDate, SHARE_CHECK_OUT_FLAGS);

		if (isShared) {
			return String.format(mContext.getString(R.string.share_template_subject_hotel_reshare), travelerName, city,
					checkIn, checkOut);
		}
		else {
			return String.format(mContext.getString(R.string.share_template_subject_hotel), city, checkIn, checkOut);
		}
	}

	public String getHotelShareTextShort(String hotelName, DateTime startDate, String sharableDetailsURL,
			boolean isShared, String travelerFirstName) {

		if (!isShared) {
			String template = mContext.getString(R.string.share_msg_template_short_hotel);
			String departureDateStr = DateUtils.formatDateTime(mContext, startDate.getMillis(),
					DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
			return String.format(template, hotelName, departureDateStr, sharableDetailsURL);
		}
		else {
			// This is a reshare, hence append the primaryTraveler's FirstName to the share message.
			String template = mContext.getString(R.string.share_msg_template_short_hotel_reshare);
			String departureDateStr = DateUtils.formatDateTime(mContext, startDate.getMillis(),
					DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
			return String.format(template, travelerFirstName, hotelName, departureDateStr, sharableDetailsURL);
		}
	}

	public String getHotelShareTextLong(String hotelName, String address, String phone, LocalDate startDate,
			LocalDate endDate, String sharableDetailsUrl, boolean isShared, String travelerName) {

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

		if (!TextUtils.isEmpty(sharableDetailsUrl)) {
			builder.append(mContext.getString(R.string.share_link_section, sharableDetailsUrl));
			builder.append("\n\n");
		}

		//1683. VSC Don't show Android App crossSell text and link.
		//1754. VSC Show the requested text which doesn't contain link to app.
		if (!ExpediaBookingApp.IS_VSC) {
			builder.append(mContext.getString(R.string.share_template_long_ad, downloadUrl));
		}
		else {
			builder.append(mContext.getString(R.string.share_template_long_ad));
		}

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
			String vendorName, String vendorAddress, String sharableDetailsURL) {

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
		if (!TextUtils.isEmpty(sharableDetailsURL)) {
			sb.append("\n");
			sb.append(sharableDetailsURL);
		}

		return sb.toString();
	}

	public String getCarShareTextLong(Car.Category carCategory, DateTime pickUpDateTime, DateTime dropOffDateTime,
			CarVendor vendor, Location pickUpLocation, Location dropOffLocation, String sharableDetailsURL) {

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

		if (!TextUtils.isEmpty(sharableDetailsURL)) {
			sb.append(mContext.getString(R.string.share_link_section, sharableDetailsURL));
			sb.append("\n");
		}

		sb.append("\n");

		//1683. VSC Don't show Android App crossSell text and link.
		if (!ExpediaBookingApp.IS_VSC) {
			sb.append(mContext.getString(R.string.share_template_long_ad, PointOfSale.getPointOfSale().getAppInfoUrl()));
		}

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

	public String getActivityShareTextShort(String title, DateTime validDateTime, DateTime expirationDateTime,
			String sharableDetailsURL) {
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
		if (!TextUtils.isEmpty(sharableDetailsURL)) {
			sb.append("\n");
			sb.append(sharableDetailsURL);
		}

		return sb.toString();
	}

	public String getActivityShareTextLong(String title, DateTime validDateTime, DateTime expirationDateTime,
			List<Traveler> travelers, int guestCount, String sharableDetailsURL) {

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

		final String[] guests = new String[travelers.size()];
		for (int i = 0; i < travelers.size(); i++) {
			guests[i] = travelers.get(i).getFullName();
		}

		sb.append("\n\n");
		sb.append(mContext.getString(R.string.share_template_long_activity_guests_with_count, guestCount,
				TextUtils.join("\n", guests)));

		sb.append("\n");

		if (!TextUtils.isEmpty(sharableDetailsURL)) {
			sb.append("\n");
			sb.append(mContext.getString(R.string.share_link_section, sharableDetailsURL));
			sb.append("\n");
		}

		sb.append("\n");

		//1683. VSC Don't show Android App crossSell text and link.
		if (!ExpediaBookingApp.IS_VSC) {
			sb.append(mContext.getString(R.string.share_template_long_ad, downloadUrl));
		}

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

	public Intent[] getShareIntents(ItinContentGenerator<? extends ItinCardData> generator) {
		ArrayList<Intent> intents = new ArrayList<Intent>();

		if (generator instanceof FlightItinContentGenerator) {
			intents.add(((FlightItinContentGenerator) generator).getShareWithFlightTrackIntent());
		}
		if (AndroidUtils.isPackageInstalled(mContext, "com.facebook.katana")) {
			intents.add(FacebookShareActivity.createIntent(mContext, generator));
		}

		String subject = generator.getShareSubject();
		String longMessage = generator.getShareTextLong();
		String shortMessage = generator.getShareTextShort();

		intents.add(SocialUtils.getEmailIntent(mContext, subject, longMessage));

		Intent share = SocialUtils.getShareIntent(subject, shortMessage, false);

		// We want to strip "Facebook" from the share intent. We have a better
		// solution for Facebook sharing (FacebookShareActivity). How we'll do that is
		// to create multiple intents, one for each that resolves the share intent,
		// excluding Facebook.
		List<ResolveInfo> resolveInfos =
				mContext.getPackageManager().queryIntentActivities(share, 0);

		for (ResolveInfo resolveInfo : resolveInfos) {
			String packageName = resolveInfo.activityInfo.applicationInfo.packageName;
			if ("com.facebook.katana".equals(packageName)) {
				continue;
			}
			String activityName = resolveInfo.activityInfo.name;
			ComponentName component = new ComponentName(packageName, activityName);
			Intent intent = new Intent(share);
			intent.setComponent(component);
			intents.add(intent);
		}

		return intents.toArray(new Intent[] {});
	}
}
