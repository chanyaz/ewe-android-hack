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
	public List<PackageHotelOffer> packageHotelOffers;

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
