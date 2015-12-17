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
	public List<HotelRoomResponse> hotelRoomResponse;

	public Double hotelStarRating;
	public String longDescription;

	public Integer totalReviews;
	public Integer totalRecommendations;
	public String telesalesNumber;
	public Boolean isVipAccess;

	public List<Photos> photos;

	public static class HotelAmenities {
		public String id;
		public String description;
	}

	public static class HotelAmenitiesText {
		public String content;
		public String name;
	}

	public static class HotelRoomResponse {
		public List<BedTypes> bedTypes;
		public String cancellationPolicy;
		public Boolean hasFreeCancellation;
		public Boolean isPayLater;
		public PayLaterOffer payLaterOffer;
		public String productKey;
		public Boolean rateChange;
		public String rateDescription;
		public RateInfo rateInfo;
		public String roomTypeDescription;
		public String roomLongDescription;
		public String roomThumbnailUrl;
	}

	public static class BedTypes {
		public String id;
		public String description;
	}

	public static class PayLaterOffer {
		public List<BedTypes> bedTypes;
		public String cancellationPolicy;
		public Boolean hasFreeCancellation;
		public String freeCancellationWindowDate;
		public Boolean immediateChargeRequired;
		public Boolean isPayLater;
		public Boolean nonRefundable;
		public String productKey;
		public Boolean rateChange;
		public String rateDescription;
		public RateInfo rateInfo;
		public String supplierType;
	}

	public static class RateInfo {
		public HotelRate chargeableRateInfo;
		public String description;
	}

	public static class Photos {
		public String displayText;
		public String url;
	}
}
