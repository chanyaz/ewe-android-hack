package com.expedia.bookings.utils;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Waypoint;

public class AddToCalendarUtils {

	public static Intent generateHotelAddToCalendarIntent(Context context, Property property, LocalDate date,
			boolean checkIn, String confNumber, String itinId) {
		int titleResId = checkIn ? R.string.calendar_hotel_title_checkin_TEMPLATE
				: R.string.calendar_hotel_title_checkout_TEMPLATE;

		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setData(CalendarContract.Events.CONTENT_URI);

		intent.putExtra(CalendarContract.Events.TITLE, context.getString(titleResId, property.getName()));
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.toDateTimeAtStartOfDay().getMillis());
		intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
		intent.putExtra(CalendarContract.Events.EVENT_LOCATION, property.getLocation().toLongFormattedString());

		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(confNumber)) {
			sb.append(context.getString(R.string.confirmation_number) + ": " + confNumber);
			sb.append("\n");
		}
		if (!TextUtils.isEmpty(itinId)) {
			sb.append(context.getString(R.string.itinerary_number) + ": " + itinId);
			sb.append("\n\n");
		}
		sb.append(ConfirmationUtils.determineContactText(context));
		intent.putExtra(CalendarContract.Events.DESCRIPTION, sb.toString());

		return intent;
	}

	public static Intent generateFlightAddToCalendarIntent(Context context, PointOfSale pointOfSale,
			String itineraryNumber, FlightLeg leg) {
		Waypoint origin = leg.getFirstWaypoint();
		Airport originAirport = origin.getAirport();
		Waypoint destination = leg.getLastWaypoint();

		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setData(CalendarContract.Events.CONTENT_URI);
		intent.putExtra(CalendarContract.Events.TITLE, context.getString(R.string.calendar_flight_title_TEMPLATE,
			origin.mAirportCode, destination.mAirportCode));
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, origin.getMostRelevantDateTime().getTimeInMillis());
		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, destination.getMostRelevantDateTime().getTimeInMillis());
		intent.putExtra(
			CalendarContract.Events.EVENT_LOCATION,
			context.getString(R.string.calendar_flight_location_TEMPLATE, originAirport.mName,
				StrUtils.getWaypointCityOrCode(origin)));

		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(itineraryNumber)) {
			sb.append(context.getString(Ui.obtainThemeResID(context, R.attr.skin_calendarFlightDescItinerary), itineraryNumber));
			sb.append("\n\n");
		}
		sb.append(context.getString(R.string.calendar_flight_desc_directions_TEMPLATE,
			"https://maps.google.com/maps?q=" + origin.mAirportCode));
		sb.append("\n\n");
		sb.append(context.getString(Ui.obtainThemeResID(context, R.attr.skin_calendarFlightDescSupport),
			pointOfSale.getSupportPhoneNumberBestForUser(Db.getUser())));
		sb.append("\n\n");
		intent.putExtra(CalendarContract.Events.DESCRIPTION, sb.toString());
		return intent;
	}

	public static Intent generateCarAddToCalendarIntent(Context context, PointOfSale pointOfSale,
		String itineraryNumber, CreateTripCarOffer offer) {
		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setData(CalendarContract.Events.CONTENT_URI);
		intent.putExtra(CalendarContract.Events.TITLE, context.getString(R.string.calendar_car_title_TEMPLATE,
			offer.pickUpLocation.locationCode));
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, offer.getPickupTime().getMillis());
		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, offer.getDropOffTime().getMillis());
		intent.putExtra(
			CalendarContract.Events.EVENT_LOCATION,
			context.getString(R.string.calendar_car_location_TEMPLATE, offer.pickUpLocation.toAddress()));


		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(itineraryNumber)) {
			sb.append(context.getString((R.string.calendar_car_desc_itinerary_TEMPLATE), itineraryNumber));
			sb.append("\n\n");
		}
		sb.append("\n\n");
		sb.append(context.getString((R.string.calendar_car_desc_support_TEMPLATE), offer.vendor.localPhoneNumber,
			offer.vendor.phoneNumber));
		sb.append("\n\n");
		intent.putExtra(CalendarContract.Events.DESCRIPTION, sb.toString());
		return intent;
	}
}
