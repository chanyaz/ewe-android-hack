package com.expedia.bookings.data;

import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.utils.CurrencyUtils;

public class TripBucketItemLX extends TripBucketItem {
	LXCreateTripResponse createTripResponse;

	public TripBucketItemLX(LXCreateTripResponse createTripResponse) {
		this.createTripResponse = createTripResponse;
		if (createTripResponse.validFormsOfPayment != null) {
			for (ValidPayment payment : createTripResponse.validFormsOfPayment) {
				payment.setCreditCardType(CurrencyUtils.parseCardType(payment.name));
			}
			addValidPayments(createTripResponse.validFormsOfPayment);
		}
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.LX;
	}
}
