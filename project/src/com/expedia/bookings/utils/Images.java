package com.expedia.bookings.utils;

import java.util.Locale;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Car;

public class Images {
	private Images() {
		// ignore
	}

	public static String getTabletLaunch(String destination) {
		return ExpediaBookingApp.MEDIA_URL + "/mobiata/mobile/apps/ExpediaBooking/LaunchDestinations/images/" + destination + ".jpg";
	}

	public static String getFlightDestination(String destination) {
		return ExpediaBookingApp.MEDIA_URL + "/mobiata/mobile/apps/ExpediaBooking/FlightDestinations/images/" + destination + ".jpg";
	}

	public static String getTabletDestination(String destination) {
		return ExpediaBookingApp.MEDIA_URL + "/mobiata/mobile/apps/ExpediaBooking/TabletDestinations/images/" + destination + ".jpg";
	}

	public static String getCarRental(Car car) {
		final String category = car.getCategory().toString().replace("_", "").toLowerCase(Locale.ENGLISH);
		final String type = car.getType().toString().replace("_", "").toLowerCase(Locale.ENGLISH);
		final String code = category + "_" + type;
		return ExpediaBookingApp.MEDIA_URL + "/mobiata/mobile/apps/ExpediaBooking/CarRentals/images/" + code + ".jpg";
	}
}
