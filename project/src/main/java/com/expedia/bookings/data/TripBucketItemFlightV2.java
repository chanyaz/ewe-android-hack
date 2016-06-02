package com.expedia.bookings.data;

import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.trips.TripBucketItem;
import com.expedia.bookings.utils.CurrencyUtils;

public class TripBucketItemFlightV2 extends TripBucketItem {

	FlightCreateTripResponse flightCreateTripResponse;

	public TripBucketItemFlightV2(FlightCreateTripResponse flightCreateTripResponse) {
		this.flightCreateTripResponse = flightCreateTripResponse;
		for (ValidPayment payment : flightCreateTripResponse.getValidFormsOfPayment()) {
			payment.setPaymentType(CurrencyUtils.parsePaymentType(payment.name));
		}
		addValidPayments(flightCreateTripResponse.getValidFormsOfPayment());
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.FLIGHTS_V2;
	}
}
