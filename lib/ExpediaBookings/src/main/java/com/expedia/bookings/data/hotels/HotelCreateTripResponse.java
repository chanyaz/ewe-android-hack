package com.expedia.bookings.data.hotels;

import java.math.BigDecimal;
import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;
import com.expedia.bookings.utils.Strings;

public class HotelCreateTripResponse extends TripResponse {

	public String userId;
	public HotelProductResponse originalHotelProductResponse;
	public HotelProductResponse newHotelProductResponse;
	public String tealeafTransactionId;
	public Coupon coupon;

	public static class Coupon {
		public String code;
	}

	public static class HotelProductResponse {
		public String checkInDate;
		public String checkOutDate;
		public String adultCount;
		public String numberOfNights;
		public String numberOfRooms;
		public String hotelId;
		public String hotelName;
		public String localizedHotelName;
		public String hotelAddress;
		public String hotelCity;
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
	}

	@Override
	public Money getTripTotal() {
		HotelRate hotelRate = newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo;
		return new Money(new BigDecimal(hotelRate.total), hotelRate.currencyCode);
	}
}
