package com.expedia.bookings.data.trips;

import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.payment.PointsAndCurrency;
import com.expedia.bookings.services.HotelCheckoutResponse;

public class TripBucketItemHotelV2 extends TripBucketItem {
	public final HotelCreateTripResponse mHotelTripResponse;

	public TripBucketItemHotelV2(HotelCreateTripResponse mHotelTripResponse) {
		this.mHotelTripResponse = mHotelTripResponse;
		if (mHotelTripResponse.getValidFormsOfPayment() != null) {
			addValidPaymentsV2(mHotelTripResponse.getValidFormsOfPayment());
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
		return LineOfBusiness.HOTELS;
	}
}
