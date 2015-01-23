package com.expedia.bookings.utils;

import java.util.Locale;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarType;

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
		return getCarRental(car.getCategory(), car.getType());
	}

	public static String getCarRental(CarCategory category, CarType type) {
		final String categoryString = category.toString().replace("_", "").toLowerCase(Locale.ENGLISH);
		final String typeString = type.toString().replace("_", "").toLowerCase(Locale.ENGLISH);
		final String code = categoryString + "_" + typeString;
		return ExpediaBookingApp.MEDIA_URL + "/mobiata/mobile/apps/ExpediaBooking/CarRentals/images/" + code + ".jpg";
	}
}
