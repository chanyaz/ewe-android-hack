package com.expedia.bookings.data.flights;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;

public class FlightCreateTripResponse extends TripResponse {
	public Money totalPrice;
	public FlightTripDetails details;

	@NotNull
	@Override
	public Money getTripTotal() {
		return totalPrice;
	}

	@NotNull
	@Override
	public boolean isCardDetailsRequiredForBooking() {
		return true;
	}
}
