package com.expedia.bookings.data.hotels;

import java.util.List;

public class Hotel {
	public String hotelId;
	public String localizedName;
	public String address;
	public String city;
	public String stateProvinceCode;
	public String countryCode;
	public String postalCode;
	public String airportCode;
	public String supplierType;
	public float hotelStarRating;
	public String hotelStarRatingCssClassName;
	public float hotelGuestRating;
	public int totalRecommendations;
	public int percentRecommended;
	public int totalReviews;
	public String shortDescription;
	public String locationDescription;
	public String locationId;
	public HotelRate lowRateInfo;
	public String rateCurrencyCode;
	public int roomsLeftAtThisRate;
	public double latitude;
	public double longitude;
	public double proximityDistanceInMiles;
	public double proximityDistanceInKiloMeters;
	public String largeThumbnailUrl;
	public String thumbnailUrl;
	public String discountMessage;
	public boolean isDiscountRestrictedToCurrentSourceType;
	public boolean isSameDayDRR;
	public boolean isHotelAvailable;
	public boolean isSponsoredListing;
	public transient boolean hasShownImpression;
	public String clickTrackingUrl;
	public String impressionTrackingUrl;
	public boolean hasFreeCancellation;
	public List<Amenity> amenities;
	public String distanceUnit;
	public boolean isVipAccess;
	public boolean isPaymentChoiceAvailable;
	public boolean isShowEtpChoice;

	public class Amenity {
		public String id;
		public String description;
	}
}
