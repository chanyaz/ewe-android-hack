package com.expedia.bookings.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.trips.BookingStatus;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.Trip.TimePeriod;
import com.expedia.bookings.data.trips.TripComponent.Type;

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
				trip.addTripComponent(parseTripComponent(hotels.optJSONObject(b), Type.HOTEL));
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
			component.setStartDate(parseDateTime2(obj.optString("checkInDate")));
			component.setEndDate(parseDateTime2(obj.optString("checkOutDate")));
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
		if (status.equals("SAVED")) {
			return BookingStatus.SAVED;
		}
		else if (status.equals("PENDING")) {
			return BookingStatus.PENDING;
		}
		else if (status.equals("BOOKED")) {
			return BookingStatus.BOOKED;
		}
		else if (status.equals("CANCELLED")) {
			return BookingStatus.CANCELLED;
		}

		return null;
	}

	private TimePeriod parseTimePeriod(String period) {
		if (period.equals("UPCOMING")) {
			return TimePeriod.UPCOMING;
		}
		else if (period.equals("INPROGRESS")) {
			return TimePeriod.INPROGRESS;
		}
		else if (period.equals("COMPLETED")) {
			return TimePeriod.COMPLETED;
		}

		return null;
	}
}
