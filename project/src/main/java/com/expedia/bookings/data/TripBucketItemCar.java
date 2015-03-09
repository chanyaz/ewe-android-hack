package com.expedia.bookings.data;

import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.utils.CurrencyUtils;

public class
	TripBucketItemCar extends TripBucketItem {
	CarCreateTripResponse mCarTripResponse;

	public TripBucketItemCar(CarCreateTripResponse carTripResponse) {
		this.mCarTripResponse = carTripResponse;
		if (mCarTripResponse.validFormsOfPayment != null) {
			for (ValidPayment payment : mCarTripResponse.validFormsOfPayment ) {
				payment.setCreditCardType(CurrencyUtils.parseCardType(payment.name));
			}
			addValidPayments(mCarTripResponse.validFormsOfPayment);
		}
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.CARS;
	}
}
