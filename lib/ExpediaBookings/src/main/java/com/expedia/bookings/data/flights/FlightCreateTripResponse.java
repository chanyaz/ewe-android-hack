package com.expedia.bookings.data.flights;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;
import com.expedia.bookings.data.insurance.InsuranceProduct;

public class FlightCreateTripResponse extends TripResponse {
	public List<InsuranceProduct> availableInsuranceProducts;
	public FlightTripDetails details;
	public Money totalPrice;

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
