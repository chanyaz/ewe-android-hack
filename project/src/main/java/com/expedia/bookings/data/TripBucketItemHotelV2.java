package com.expedia.bookings.data;

import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.expedia.bookings.utils.CurrencyUtils;

public class TripBucketItemHotelV2 extends TripBucketItem {
	public HotelCreateTripResponse mHotelTripResponse;

	public TripBucketItemHotelV2(HotelCreateTripResponse mHotelTripResponse) {
		this.mHotelTripResponse = mHotelTripResponse;
		if (mHotelTripResponse.validFormsOfPayment != null) {
			for (ValidPayment payment : mHotelTripResponse.validFormsOfPayment ) {
				payment.setPaymentType(CurrencyUtils.parsePaymentType(payment.name));
			}
			addValidPayments(mHotelTripResponse.validFormsOfPayment);
		}
	}

	public HotelCreateTripResponse updateHotelProducts(HotelCheckoutResponse.PriceChangeResponse priceChange) {
		mHotelTripResponse.originalHotelProductResponse = priceChange.oldProduct;
		mHotelTripResponse.newHotelProductResponse = priceChange.newProduct;
		return mHotelTripResponse;
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.HOTELSV2;
	}
}
