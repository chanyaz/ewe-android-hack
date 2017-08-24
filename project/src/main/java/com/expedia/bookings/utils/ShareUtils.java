package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.FacebookShareActivity;
import com.expedia.bookings.data.CarVendor;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.itin.data.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.data.trips.TripHotel;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Layover;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;
import com.squareup.phrase.Phrase;

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

	private final Context mContext;

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
		String travelerName = getTravelerFirstName(hotel);
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
		final String url = itinCardData.getSharableDetailsUrl();
		boolean isShared = itinCardData.isSharedItin();

		TripHotel hotel = (TripHotel) itinCardData.getTripComponent();
		String travelerName = getTravelerFirstName(hotel);

		return getHotelShareTextShort(itinCardData.getPropertyName(), itinCardData.getStartDate(), url, isShared, travelerName);
	}

	public String getCarShareTextShort(ItinCardDataCar itinCardData) {
		CarCategory category = itinCardData.getCar().getCategory();
		DateTime pickUpDate = itinCardData.getPickUpDate();
		DateTime dropOffDate = itinCardData.getDropOffDate();
		String vendorName = itinCardData.getVendorName();
		String vendorAddress = itinCardData.getRelevantVendorLocation().toLongFormattedString();

		// #2189: Only use share URL with hotels/flights
		return getCarShareTextShort(category, pickUpDate, dropOffDate, vendorName, vendorAddress, null);
	}

	public String getActivityShareTextShort(ItinCardDataActivity itinCardData) {
		// #2189: Only use share URL with hotels/flights
		return getActivityShareTextShort(itinCardData.getTitle(), itinCardData.getValidDate(),
				itinCardData.getExpirationDate(), null);
	}

	// SHARE TEXT LONG

	public String getFlightShareTextLong(ItinCardDataFlight itinCardData) {
		TripFlight tripFlight = (TripFlight) itinCardData.getTripComponent();
		final String url = itinCardData.getSharableDetailsUrl();
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
		String travelerName = getTravelerFirstName(hotel);

		return getHotelShareTextLong(hotelName, address, phone, startDate.toLocalDate(), endDate.toLocalDate(),
				sharableDetailsUrl, isShared, travelerName);
	}

	public String getCarShareTextLong(ItinCardDataCar itinCardData) {
		CarCategory category = itinCardData.getCar().getCategory();
		DateTime pickUpDate = itinCardData.getPickUpDate();
		DateTime dropOffDate = itinCardData.getDropOffDate();
		CarVendor vendor = itinCardData.getCar().getVendor();
		Location pickUpLocation = itinCardData.getPickUpLocation();
		Location dropOffLocation = itinCardData.getDropOffLocation();
		// #2189: Only use share URL with hotels/flights
		String sharableDetailsURL = null;

		return getCarShareTextLong(category, pickUpDate, dropOffDate, vendor, pickUpLocation, dropOffLocation,
				sharableDetailsURL);
	}

	public String getActivityShareTextLong(ItinCardDataActivity itinCardData) {
		// #2189: Only use share URL with hotels/flights
		return getActivityShareTextLong(itinCardData.getTitle(), itinCardData.getValidDate(),
				itinCardData.getExpirationDate(), itinCardData.getTravelers(), itinCardData.getGuestCount(),
				null);
	}

	// Share methods

	// Flights

	public String getFlightShareSubject(FlightLeg firstLeg, FlightLeg lastLeg, int travelerCount, boolean isShared, String travelerName) {
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());
		DateTime first = firstLeg.getFirstWaypoint().getMostRelevantDateTime().toLocalDateTime().toDateTime();
		DateTime last = lastLeg.getFirstWaypoint().getMostRelevantDateTime().toLocalDateTime().toDateTime();
		long start = first.getMillis();
		long end = last.getMillis();
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
		DateTime departureCal = leg.getFirstWaypoint().getBestSearchDateTime();
		DateTime departureDate = departureCal.toLocalDateTime().toDateTime();

		String shareText = "";

		if (Locale.US.equals(Locale.getDefault())) {
			if (!isShared) {
				String template = mContext.getString(R.string.share_msg_template_short_flight);
				String departureDateStr = DateUtils.formatDateTime(mContext, departureDate.getMillis(),
						DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
				shareText = String.format(template, destinationCity, departureDateStr, shareableDetailsURL);
			}
			else {
				// This is a reshare, hence append the primaryTraveler's FirstName to the share message.
				String template = mContext.getString(R.string.share_msg_template_short_flight_reshare);
				String departureDateStr = DateUtils.formatDateTime(mContext, departureDate.getMillis(),
						DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
				shareText = String.format(template, travelerFirstName, destinationCity, departureDateStr,
						shareableDetailsURL);
			}
		}
		else {
			shareText = shareableDetailsURL;
		}

		// This is a hack to remove the newLine from the text.
		// TODO: Remove the \n from strings.xml for next loc dump.
		return shareText.replace("\n", " ");
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

		if (ProductFlavorFeatureConfiguration.getInstance().shouldDisplayItinTrackAppLink() && !TextUtils
			.isEmpty(sharableDetailsURL)) {
			body.append("\n");
			body.append(mContext.getString(R.string.share_link_section, sharableDetailsURL));
			body.append("\n");
		}

		body.append("\n");

		body.append(Phrase.from(mContext, R.string.share_long_ad_TEMPLATE).put("brand", BuildConfig.brand)
			.put("appinfourl", PointOfSale.getPointOfSale().getAppInfoUrl()).format());

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

		String message = "";
		if (Locale.US.equals(Locale.getDefault())) {
			if (!isShared) {
				String template = mContext.getString(R.string.share_msg_template_short_hotel);
				String departureDateStr = DateUtils.formatDateTime(mContext, startDate.getMillis(),
					DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
				message = shortenShortShareMessage(template, hotelName, departureDateStr, sharableDetailsURL);
			}
			else {
				// This is a reshare, hence append the primaryTraveler's FirstName to the share message.
				String template = mContext.getString(R.string.share_msg_template_short_hotel_reshare);
				String departureDateStr = DateUtils.formatDateTime(mContext, startDate.getMillis(),
					DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
				message = shortenShortShareMessage(template, travelerFirstName, hotelName, departureDateStr, sharableDetailsURL);
			}
		}
		else {
			message = sharableDetailsURL;
		}
		return message.replace('\n', ' ');
	}

	private final static int SMS_CHAR_LIMIT = 160;

	private String shortenShortShareMessage(String template, String hotelName, String departureDateStr, String sharableDetailsURL) {
		String longMsg = String.format(template, hotelName, departureDateStr, sharableDetailsURL);
		hotelName = clipHotelName(longMsg.length(), hotelName);
		return String.format(template, hotelName, departureDateStr, sharableDetailsURL);
	}

	private String shortenShortShareMessage(String template, String travelerFirstName, String hotelName, String departureDateStr, String sharableDetailsURL) {
		String longMsg = String.format(template, travelerFirstName, hotelName, departureDateStr, sharableDetailsURL);
		hotelName = clipHotelName(longMsg.length(), hotelName);
		return String.format(template, travelerFirstName, hotelName, departureDateStr, sharableDetailsURL);
	}

	public static String clipHotelName(int longMsgLength, String hotelName) {
		int diff = longMsgLength - SMS_CHAR_LIMIT;
		if (diff > 0) {
			if (hotelName.length() - diff <= 20) {
				hotelName = hotelName.substring(0, hotelName.length() >= 20 ? 20 : hotelName.length());
			}
			else {
				hotelName = hotelName.substring(0, hotelName.length() - diff);
			}
		}
		return hotelName;
	}

	public String getHotelShareTextLong(String hotelName, String address, String phone, LocalDate startDate,
			LocalDate endDate, String sharableDetailsUrl, boolean isShared, String travelerName) {

		String checkIn = JodaUtils.formatLocalDate(mContext, startDate, LONG_SHARE_DATE_FLAGS);
		String checkOut = JodaUtils.formatLocalDate(mContext, endDate, LONG_SHARE_DATE_FLAGS);

		int nights = JodaUtils.daysBetween(startDate, endDate);
		String lengthOfStay = mContext.getResources().getQuantityString(R.plurals.length_of_stay, nights, nights);

		StringBuilder builder = new StringBuilder();
		if (isShared) {
			builder.append(mContext.getString(R.string.share_template_long_hotel_1_greeting_reshare, travelerName,
				hotelName, lengthOfStay));
		}
		else {
			builder.append(mContext.getString(R.string.share_template_long_hotel_1_greeting, hotelName, lengthOfStay));
		}
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

		if (ProductFlavorFeatureConfiguration.getInstance().shouldDisplayItinTrackAppLink() && !TextUtils
			.isEmpty(sharableDetailsUrl)) {
			builder.append(mContext.getString(R.string.share_link_section, sharableDetailsUrl));
			builder.append("\n\n");
		}

		builder.append(Phrase.from(mContext, R.string.share_long_ad_TEMPLATE).put("brand", BuildConfig.brand)
			.put("appinfourl", PointOfSale.getPointOfSale().getAppInfoUrl()).format());

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

	public String getCarShareTextShort(CarCategory carCategory, DateTime pickUpDateTime, DateTime dropOffDateTime,
			String vendorName, String vendorAddress, String sharableDetailsURL) {

		String category = CarDataUtils.getCategoryStringFor(mContext, carCategory);
		String pickUpDate = JodaUtils.formatDateTime(mContext, pickUpDateTime, SHORT_DATE_FLAGS);
		String dropOffDate = JodaUtils.formatDateTime(mContext, dropOffDateTime, SHORT_DATE_FLAGS);

		StringBuilder sb = new StringBuilder();

		if (!TextUtils.isEmpty(category)) {
			sb.append(CarDataUtils.getShareMessageFor(mContext, carCategory));
		}

		if (!TextUtils.isEmpty(pickUpDate) && !TextUtils.isEmpty(dropOffDate)) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
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

	public String getCarShareTextLong(CarCategory carCategory, DateTime pickUpDateTime, DateTime dropOffDateTime,
			CarVendor vendor, Location pickUpLocation, Location dropOffLocation, String sharableDetailsURL) {

		StringBuilder sb = new StringBuilder();

		sb.append(mContext.getString(R.string.share_hi));
		sb.append("\n\n");

		if (!TextUtils.isEmpty(vendor.getShortName())) {
			sb.append(mContext.getString(R.string.share_car_start_TEMPLATE, vendor.getShortName()));
			sb.append("\n\n");
		}

		String category = CarDataUtils.getCategoryStringFor(mContext, carCategory);
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

		if (ProductFlavorFeatureConfiguration.getInstance().shouldDisplayItinTrackAppLink() && !TextUtils
			.isEmpty(sharableDetailsURL)) {
			sb.append(mContext.getString(R.string.share_link_section, sharableDetailsURL));
			sb.append("\n");
		}

		sb.append("\n");

		if (ProductFlavorFeatureConfiguration.getInstance().isAppCrossSellInCarShareContentEnabled()) {
			sb.append(Phrase.from(mContext, R.string.share_long_ad_TEMPLATE).put("brand", BuildConfig.brand)
				.put("appinfourl", PointOfSale.getPointOfSale().getAppInfoUrl()).format());
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

		if (ProductFlavorFeatureConfiguration.getInstance().shouldDisplayItinTrackAppLink() && !TextUtils
			.isEmpty(sharableDetailsURL)) {
			sb.append("\n");
			sb.append(mContext.getString(R.string.share_link_section, sharableDetailsURL));
			sb.append("\n");
		}

		sb.append("\n");

		if (ProductFlavorFeatureConfiguration.getInstance().isAppCrossSellInActivityShareContentEnabled()) {
			sb.append(Phrase.from(mContext, R.string.share_long_ad_TEMPLATE).put("brand", BuildConfig.brand)
				.put("appinfourl", PointOfSale.getPointOfSale().getAppInfoUrl()).format());
		}

		return sb.toString();
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
				String waypoint = StrUtils.formatWaypoint(flight.getOriginWaypoint());
				sb.append(HtmlCompat.fromHtml(mContext.getString(R.string.layover_duration_location_TEMPLATE, duration,
						waypoint)));

				sb.append("\n\n");
			}

			sb.append(mContext.getString(R.string.path_template, formatAirport(flight.getOriginWaypoint().getAirport()),
					formatAirport(flight.getDestinationWaypoint().getAirport())));
			sb.append("\n");
			DateTime start = flight.getOriginWaypoint().getBestSearchDateTime().toLocalDateTime().toDateTime();
			sb.append(DateUtils.formatDateTime(mContext, start.getMillis(), DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY));
			sb.append("\n");
			DateTime end = flight.getDestinationWaypoint().getBestSearchDateTime().toLocalDateTime().toDateTime();

			String departureTzString = FormatUtils.formatTimeZone(flightLeg.getFirstWaypoint().getAirport(), start,
					MAX_TIMEZONE_LENGTH);
			String arrivalTzString = FormatUtils.formatTimeZone(flightLeg.getLastWaypoint().getAirport(), end,
					MAX_TIMEZONE_LENGTH);
			sb.append(DateUtils.formatDateTime(mContext, start.getMillis(), DateUtils.FORMAT_SHOW_TIME) + " "
					+ departureTzString);
			sb.append(" - ");
			sb.append(DateUtils.formatDateTime(mContext, end.getMillis(), DateUtils.FORMAT_SHOW_TIME) + " "
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
		ArrayList<Intent> intents = new ArrayList<>();

		if (ProductFlavorFeatureConfiguration.getInstance().isFacebookShareIntegrationEnabled() && AndroidUtils
			.isPackageInstalled(mContext, "com.facebook.katana")) {
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

	private String getTravelerFirstName(TripHotel hotel) {
		return hotel.getPrimaryTraveler() != null ? hotel.getPrimaryTraveler().getFirstName() : "";
	}
}
