package com.expedia.bookings.data.packages;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.multiitem.BundleHotelRoomResponse;
import com.expedia.bookings.data.payment.LoyaltyInformation;

public class PackageOffersResponse extends BaseApiResponse implements BundleHotelRoomResponse {
	public final List<PackageHotelOffer> packageHotelOffers;

	String checkInDate, checkOutDate;

	@NotNull
	@Override
	public List<HotelOffersResponse.HotelRoomResponse> getBundleRoomResponse() {
		return HotelOffersResponse.convertPSSHotelRoomResponse(this);
	}

	@Override
	public boolean hasRoomResponseErrors() {
		return hasErrors();
	}

	@NotNull
	@Override
	public ApiError getRoomResponseFirstError() {
		return getFirstError();
	}

	public void setCheckInDate(String checkInDate) {
		this.checkInDate = checkInDate;
	}

	public void setCheckOutDate(String checkOutDate) {
		this.checkOutDate = checkOutDate;
	}

	@NotNull
	@Override
	public String getHotelCheckInDate() {
		return checkInDate;
	}

	@NotNull
	@Override
	public String getHotelCheckOutDate() {
		return checkOutDate;
	}

	public static class PackageHotelOffer {
		public final CancellationPolicy cancellationPolicy;
		public final String packageProductId;
		public final HotelOffersResponse.HotelRoomResponse hotelOffer;
		public final Money pricePerPerson;
		public final Money priceDifferencePerNight;
		public final PackagePricing packagePricing;
		public final LoyaltyInformation loyaltyInfo;
	}

	public static class PackagePricing {
		public final HotelPricing hotelPricing;
		public final Money savings;
		public final Money packageTotal;
	}

	public static class CancellationPolicy {
		public final Boolean hasFreeCancellation;
	}

	public static class HotelPricing {
		public final MandatoryFees mandatoryFees;
	}

	public static class MandatoryFees {
		public final Money feeTotal;
	}
}
