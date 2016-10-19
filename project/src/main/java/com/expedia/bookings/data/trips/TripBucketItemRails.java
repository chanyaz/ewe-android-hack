package com.expedia.bookings.data.trips;

import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse;

public class TripBucketItemRails extends TripBucketItem {

	private final RailCreateTripResponse railTripResponse;

	public TripBucketItemRails(RailCreateTripResponse tripResponse) {
		this.railTripResponse = tripResponse;
		addValidFormsOfPayment();
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.RAILS;
	}

	private void addValidFormsOfPayment() {
		if (railTripResponse.validFormsOfPayment != null) {
			addValidPaymentsV2(railTripResponse.validFormsOfPayment);
		}
	}
}
