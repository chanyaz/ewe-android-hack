package com.expedia.bookings.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.trips.BookingStatus;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.Trip.TimePeriod;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripHotel;

/**
 * Common class for parsing trips, since the trip summary and trip details
 * responses are essentially the same (just with different amounts of
 * information).
 *
 */
public class TripParser {

	public Trip parseTrip(JSONObject tripJson) {
		Trip trip = new Trip();
		trip.setTripId(tripJson.optString("tripId"));
		trip.setTripNumber(tripJson.optString("tripNumber"));
		trip.setTitle(tripJson.optString("title"));
		trip.setDescription(tripJson.optString("description"));
		trip.setStartDate(parseDateTime(tripJson.optJSONObject("startTime")));
		trip.setEndDate(parseDateTime(tripJson.optJSONObject("endTime")));

		trip.setBookingStatus(parseBookingStatus(tripJson.optString("bookingStatus")));
		trip.setTimePeriod(parseTimePeriod(tripJson.optString("timePeriod")));

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
				trip.addTripComponent(parseTripComponent(flights.optJSONObject(b), Type.FLIGHT));
			}
		}

		// TODO: Parse more component types?

		return trip;
	}

	private TripComponent parseTripComponent(JSONObject obj, Type type) {
		TripComponent component = new TripComponent(type);

		if (type == Type.HOTEL) {

		}
		else if (type == Type.FLIGHT) {
			component.setStartDate(parseDateTime2(obj.optString("startDate")));
			component.setEndDate(parseDateTime2(obj.optString("endDate")));
		}

		component.setBookingStatus(parseBookingStatus(obj.optString("bookingStatus")));
		return component;
	}

	private DateTime parseDateTime(JSONObject obj) {
		return new DateTime(obj.optLong("epochSeconds") * 1000, obj.optInt("timeZoneOffsetSeconds"));
	}

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	private DateTime parseDateTime2(String str) {
		// TODO: DELETE ONCE OBSELETE
		//
		// Parse with no timezone.  The current timezone code is buggy and we shouldn't
		// be using this code anyways.

		try {
			Date date = DATE_FORMAT.parse(str);
			return new DateTime(date.getTime(), 0);
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
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

		hotel.setStartDate(parseDateTime2(obj.optString("checkInDate")));
		hotel.setEndDate(parseDateTime2(obj.optString("checkOutDate")));

		Property property = new Property();
		property.setPropertyId(obj.optString("hotelId"));

		JSONObject propertyJson = obj.optJSONObject("hotelPropertyInfo");
		if (propertyJson != null) {
			property.setName(propertyJson.optString("name", null));
			property.setThumbnail(new Media(propertyJson.optString("photoThumbnailURL", null)));

			JSONObject addressJson = propertyJson.optJSONObject("address");
			Location location = new Location();
			location.addStreetAddressLine(addressJson.optString("fullAddress", null));
			location.setCity(addressJson.optString("city", null));
			location.setStateCode(addressJson.optString("countrySubdivisionCode", null));
			location.setCountryCode(addressJson.optString("countryCode", null));
			location.setLatitude(propertyJson.optDouble("latitude"));
			location.setLongitude(propertyJson.optDouble("longitude"));
			property.setLocation(location);

			hotel.setProperty(property);
		}

		return hotel;
	}

	private void parseTripCommon(JSONObject obj, TripComponent component) {
		component.setBookingStatus(parseBookingStatus(obj.optString("bookingStatus")));
	}
}
