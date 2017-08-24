package com.expedia.bookings.data;

import com.expedia.bookings.data.flights.FlightCheckoutResponse;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.trips.TripBucketItem;

public class TripBucketItemFlightV2 extends TripBucketItem {
	public FlightCheckoutResponse flightCheckoutResponse;
	public final FlightCreateTripResponse flightCreateTripResponse;

	public TripBucketItemFlightV2(FlightCreateTripResponse flightCreateTripResponse) {
		this.flightCreateTripResponse = flightCreateTripResponse;
		addValidPaymentsV2(flightCreateTripResponse.getValidFormsOfPayment());
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.FLIGHTS_V2;
	}
}
