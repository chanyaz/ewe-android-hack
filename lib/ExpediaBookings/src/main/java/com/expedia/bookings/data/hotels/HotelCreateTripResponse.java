package com.expedia.bookings.data.hotels;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;
import com.expedia.bookings.utils.Strings;

public class HotelCreateTripResponse extends TripResponse {

	public String userId;
	public HotelProductResponse originalHotelProductResponse;
	public HotelProductResponse newHotelProductResponse;
	public final String tealeafTransactionId;
	public Coupon coupon;

	@NotNull
	@Override
	public Money getOldPrice() {
		//To-Do when we bring Hotels to universal CKO
		return null;
	}

	public static class Coupon {
		public String code;
	}

	public static class HotelProductResponse {
		public final String checkInDate;
		public final String checkOutDate;
		public String adultCount;
		public String numberOfNights;
		public String numberOfRooms;
		public final String hotelId;
		public final String hotelName;
		public final String localizedHotelName;
		public String hotelAddress;
		public final String hotelCity;
		public String hotelStateProvince;
		public String hotelCountry;
		public String regionId;
		public String hotelStarRating;
		public HotelOffersResponse.HotelRoomResponse hotelRoomResponse;
		public String supplierType;
		public List<HotelOffersResponse.HotelAmenities> accessibilityAmenities;
		public String largeThumbnailUrl;

		boolean isVipAccess;
		String tealeafTransactionId;

		public String getHotelName() {
			if (Strings.isEmpty(localizedHotelName)) {
				return hotelName;
			}
			return localizedHotelName;
		}

		public Money getDueNowAmount() {
			HotelRate hotelRate = hotelRoomResponse.rateInfo.chargeableRateInfo;
			boolean isPayLater = hotelRoomResponse.isPayLater;
			String expectedTotalFare;
			if (isPayLater) {
				expectedTotalFare = Strings.isNotEmpty(hotelRate.depositAmount) ? hotelRate.depositAmount : "0"; // yup. For some reason API doesn't return $0 for deposit amounts
			}
			else {
				expectedTotalFare = java.lang.String.format(Locale.ENGLISH, "%.2f", hotelRate.total);
			}
			return new Money(new BigDecimal(expectedTotalFare), hotelRate.currencyCode);
		}
	}

	@NotNull
	@Override
	public Money getTripTotalExcludingFee() {
		return newHotelProductResponse.getDueNowAmount();
	}

	@Override
	public Money tripTotalPayableIncludingFeeIfZeroPayableByPoints() {
		return newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.getDisplayTotalPrice();
	}

	@NotNull
	@Override
	public boolean isCardDetailsRequiredForBooking() {
		return true;
	}
}
