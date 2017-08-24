package com.expedia.bookings.data.trips;

import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.utils.CurrencyUtils;

public class TripBucketItemCar extends TripBucketItem {
	public final CarCreateTripResponse mCarTripResponse;

	public TripBucketItemCar(CarCreateTripResponse carTripResponse) {
		this.mCarTripResponse = carTripResponse;
		if (mCarTripResponse.validFormsOfPayment != null) {
			for (ValidPayment payment : mCarTripResponse.validFormsOfPayment ) {
				payment.setPaymentType(CurrencyUtils.parsePaymentType(payment.name));
			}
			addValidPayments(mCarTripResponse.validFormsOfPayment);
		}
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.CARS;
	}
}
