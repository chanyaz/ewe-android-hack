package com.expedia.bookings.data.packages;

import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.payment.LoyaltyInformation;

public class PackageOffersResponse extends BaseApiResponse {
	public List<PackageHotelOffer> packageHotelOffers;

	public static class PackageHotelOffer {
		public CancellationPolicy cancellationPolicy;
		public String packageProductId;
		public HotelOffersResponse.HotelRoomResponse hotelOffer;
		public Money pricePerPerson;
		public Money priceDifferencePerNight;
		public PackagePricing packagePricing;
		public LoyaltyInformation loyaltyInfo;
	}

	public static class PackagePricing {
		public HotelPricing hotelPricing;
		public Money savings;
		public Money packageTotal;
	}

	public static class CancellationPolicy {
		public Boolean hasFreeCancellation;
	}

	public static class HotelPricing {
		public MandatoryFees mandatoryFees;
	}

	public static class MandatoryFees {
		public Money feeTotal;
	}
}
