package com.expedia.bookings.data.hotels;

import java.util.List;

import com.expedia.bookings.data.ValidPayment;

public class HotelCreateTripResponse {

	public String tripId;
	public String userId;
	public HotelProductResponse originalHotelProductResponse;
	public HotelProductResponse newHotelProductResponse;
	public ExpediaRewards expediaRewards;
	public String tealeafTransactionId;
	public List<ValidPayment> validFormsOfPayment;
	public String guestUserPromoEmailOptInStatus;

	public static class ExpediaRewards {
		String totalPointsToEarn;
		boolean isActiveRewardsMember;
		String rewardsMembershipTierName;
	}

	public static class HotelProductResponse {
		public String checkInDate;
		public String checkOutDate;
		public String adultCount;
		public String numberOfNights;
		public String numberOfRooms;
		public String hotelId;
		public String localizedHotelName;
		public String hotelAddress;
		public String hotelCity;
		public String hotelStateProvince;
		public String hotelCountry;
		public String hotelStarRating;
		public HotelOffersResponse.HotelRoomResponse hotelRoomResponse;
		public String supplierType;
		public List<HotelOffersResponse.HotelAmenities> accessibilityAmenities;

		boolean isVipAccess;
		String tealeafTransactionId;
	}
}
