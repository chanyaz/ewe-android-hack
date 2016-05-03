package com.expedia.bookings.data;

import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.payment.PointsAndCurrency;
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

	public HotelCreateTripResponse updateAfterCheckoutPriceChange(HotelCheckoutResponse hotelCheckoutResponse) {
		mHotelTripResponse.originalHotelProductResponse = hotelCheckoutResponse.checkoutResponse.jsonPriceChangeResponse.oldProduct;
		mHotelTripResponse.newHotelProductResponse = hotelCheckoutResponse.checkoutResponse.jsonPriceChangeResponse.newProduct;
		mHotelTripResponse.setPointsDetails(hotelCheckoutResponse.pointsDetails);
		mHotelTripResponse.setUserPreferencePoints(hotelCheckoutResponse.userPreferencePoints);
		return mHotelTripResponse;
	}

	public HotelCreateTripResponse updateTotalPointsAndCurrencyToEarn(PointsAndCurrency pointsAndCurrency) {
		mHotelTripResponse.getRewards().updatePointsAndCurrencyToEarn(pointsAndCurrency);
		return mHotelTripResponse;
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.HOTELSV2;
	}
}
