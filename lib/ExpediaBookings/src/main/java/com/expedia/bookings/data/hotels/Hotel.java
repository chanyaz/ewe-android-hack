package com.expedia.bookings.data.hotels;

import java.util.List;

import com.expedia.bookings.data.packages.PackageHotel;
import com.expedia.bookings.data.packages.PackageOfferModel;

public class Hotel {
	public String sortIndex;
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
	public String drrMessage;
	public String impressionTrackingUrl;
	public boolean hasFreeCancellation;
	public List<HotelAmenity> amenities;
	public String distanceUnit;
	public boolean isVipAccess;
	public boolean isPaymentChoiceAvailable;
	public boolean isShowEtpChoice;
	public boolean isMemberDeal;

	public transient List<Integer> amenityFilterIdList;
	public transient boolean isSoldOut = false;
	public transient boolean isPackage = false;
	public transient String hotelPid;
	public transient PackageOfferModel packageOfferModel;

	public static class HotelAmenity {
		public String id;
		public String description;
	}

	public static Hotel convertPackageHotel(PackageHotel packageHotel) {
		Hotel hotel = new Hotel();
		hotel.hotelPid = packageHotel.hotelPid;
		hotel.hotelId = packageHotel.hotelId;
		hotel.localizedName = packageHotel.localizedHotelName;
		hotel.address = packageHotel.hotelAddress.firstAddressLine;
		hotel.city = packageHotel.hotelAddress.city;
		hotel.stateProvinceCode = packageHotel.hotelAddress.province;
		hotel.countryCode = packageHotel.hotelAddress.countryAlpha3Code;
		hotel.postalCode = packageHotel.hotelAddress.postalCode;
		hotel.hotelStarRating = Float.valueOf(packageHotel.hotelStarRating);
		hotel.hotelGuestRating = packageHotel.overallReview;
		hotel.totalRecommendations = packageHotel.recommendationTotal;
		hotel.totalReviews = packageHotel.reviewTotal;
		hotel.shortDescription = packageHotel.hotelDescription;
		hotel.locationDescription = packageHotel.hotelDescription;
		hotel.latitude = packageHotel.latitude;
		hotel.longitude = packageHotel.longitude;
		hotel.largeThumbnailUrl = packageHotel.thumbnailURL;
		hotel.thumbnailUrl = packageHotel.thumbnailURL;
		hotel.isVipAccess = packageHotel.vip;
		hotel.packageOfferModel = packageHotel.packageOfferModel;
		hotel.drrMessage = packageHotel.drrMessage;
		hotel.isPackage = true;
		return hotel;
	}

}
