package com.expedia.bookings.data.trips;

import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.utils.CurrencyUtils;

public class TripBucketItemTransport extends TripBucketItem {
	LXCreateTripResponse createTripResponse;

	public TripBucketItemTransport(LXCreateTripResponse createTripResponse) {
		this.createTripResponse = createTripResponse;
		if (createTripResponse.validFormsOfPayment != null) {
			for (ValidPayment payment : createTripResponse.validFormsOfPayment) {
				payment.setPaymentType(CurrencyUtils.parsePaymentType(payment.name));
			}
			addValidPayments(createTripResponse.validFormsOfPayment);
		}
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.TRANSPORT;
	}

	public LXCreateTripResponse getCreateTripResponse() {
		return createTripResponse;
	}
}
