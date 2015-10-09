package com.expedia.bookings.data;

import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.utils.CurrencyUtils;

public class TripBucketItemHotelV2 extends TripBucketItem {
	public HotelCreateTripResponse mHotelTripResponse;

	public TripBucketItemHotelV2(HotelCreateTripResponse mHotelTripResponse) {
		this.mHotelTripResponse = mHotelTripResponse;
		if (mHotelTripResponse.validFormsOfPayment != null) {
			for (ValidPayment payment : mHotelTripResponse.validFormsOfPayment ) {
				payment.setCreditCardType(CurrencyUtils.parseCardType(payment.name));
			}
			addValidPayments(mHotelTripResponse.validFormsOfPayment);
		}
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.HOTELSV2;
	}
}
