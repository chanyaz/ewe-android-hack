package com.expedia.bookings.data;

import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.expedia.bookings.utils.CurrencyUtils;

public class TripBucketItemHotelV2 extends TripBucketItem {
	public HotelCreateTripResponse mHotelTripResponse;

	public TripBucketItemHotelV2(HotelCreateTripResponse mHotelTripResponse) {
		this.mHotelTripResponse = mHotelTripResponse;
		if (mHotelTripResponse.getValidFormsOfPayment() != null) {
			for (ValidPayment payment : mHotelTripResponse.getValidFormsOfPayment() ) {
				payment.setPaymentType(CurrencyUtils.parsePaymentType(payment.name));
			}
			addValidPayments(mHotelTripResponse.getValidFormsOfPayment());
		}
	}

	public HotelCreateTripResponse updateHotelProducts(HotelCheckoutResponse.PriceChangeResponse priceChange) {
		mHotelTripResponse.originalHotelProductResponse = priceChange.oldProduct;
		mHotelTripResponse.newHotelProductResponse = priceChange.newProduct;
		return mHotelTripResponse;
	}

	public HotelCreateTripResponse updateTotalPointsToEarn(int points) {
		mHotelTripResponse.getExpediaRewards().setTotalPointsToEarn(points);
		return mHotelTripResponse;
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.HOTELSV2;
	}
}
