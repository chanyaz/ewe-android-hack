package com.expedia.bookings.data.hotels;

import java.util.Comparator;

public class Hotel {

	public int sortIndex;
	public String hotelId;
	public String name;
	public String localizedName;
	public String nonLocalizedName;
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
	public float lowRate;
	public HotelRate lowRateInfo;
	public String rateCurrencyCode;
	public String rateCurrencySymbol;
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
	public String notAvailableMessage;
	public boolean isSponsoredListing;
	public boolean hasFreeCancellation;
	public String distanceUnit;
	public boolean didGetBackHighestPriceFromSurvey;
	public boolean isDudley;
	public boolean isVipAccess;
	public boolean isPaymentChoiceAvailable;
	public boolean isShowEtpChoice;
	public String shortDistanceMessage;

	/**
	 * Used to compare two hotels based on guest rating.
	 */
	public static final Comparator<Hotel> GUEST_RATING_COMPARATOR = new Comparator<Hotel>() {
		@Override
		public int compare(Hotel hotel1, Hotel hotel2) {
			float rating1 = hotel1.hotelGuestRating;
			float rating2 = hotel2.hotelGuestRating;

			if (rating1 >= rating2) {
				return -1;
			}
			else {
				return 1;
			}
		}
	};
}
