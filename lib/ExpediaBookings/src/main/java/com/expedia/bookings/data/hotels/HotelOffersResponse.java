package com.expedia.bookings.data.hotels;

import java.util.List;

public class HotelOffersResponse {

	public String airAttachExpirationTimeSeconds;
	public String checkInDate;
	public String checkOutDate;
	public Boolean deskTopOverrideNumber;
	public String firstHotelOverview;
	public String hotelAddress;
	public List<HotelAmenities> hotelAmenities;
	public HotelAmenitiesText hotelAmenitiesText;
	public String hotelCity;
	public String hotelCountry;
	public Double hotelGuestRating;
	public String hotelId;
	public String hotelName;
	private List<HotelRoomResponse> hotelRoomResponse;

	public Double hotelStarRating;
	public String longDescription;

	public Integer totalReviews;
	public Integer totalRecommendations;
	public String telesalesNumber;
	public Boolean isVipAccess;

	public static class HotelAmenities {
		String id;
		String description;
	}

	public static class HotelAmenitiesText {
		String content;
		String name;
	}

	public static class HotelRoomResponse {
		List<BedTypes> bedTypes;
		String cancellationPolicy;
		Boolean hasFreeCancellation;
		Boolean isPayLater;
		PayLaterOffer payLaterOffer;
		String productKey;
		Boolean rateChange;
		String rateDescription;
		RateInfo rateInfo;
	}

	public static class BedTypes {
		String id;
		String description;
	}

	public static class PayLaterOffer {
		List<BedTypes> bedTypes;
		String cancellationPolicy;
		Boolean hasFreeCancellation;
		String freeCancellationWindowDate;
		Boolean immediateChargeRequired;
		Boolean isPayLater;
		Boolean nonRefundable;
		String productKey;
		Boolean rateChange;
		String rateDescription;
		RateInfo rateInfo;
		String supplierType;
	}

	public static class RateInfo {
		HotelRate chargeableRateInfo;
		String description;
	}
}
