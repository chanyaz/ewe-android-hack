package com.expedia.bookings.data.hotels;

import java.util.List;

import com.expedia.bookings.data.multiitem.HotelOffer;
import com.expedia.bookings.data.multiitem.MultiItemOffer;
import com.expedia.bookings.data.packages.PackageHotel;
import com.expedia.bookings.data.packages.PackageOfferModel;
import com.expedia.bookings.utils.NumberUtils;

public class Hotel {
	public int sortIndex;
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
	public List<HotelAmenity> amenities;
	public String distanceUnit;
	public boolean isVipAccess;
	public boolean isPaymentChoiceAvailable;
	public boolean isShowEtpChoice;
	public boolean isMemberDeal;

	public List<Integer> amenityFilterIdList;
	public boolean isSoldOut = false;
	public boolean isPackage = false;
	public String hotelPid;
	public PackageOfferModel packageOfferModel;

	public static class HotelAmenity {
		public String id;
		public String description;
	}

	public static Hotel convertPackageHotel(PackageHotel packageHotel) {
		Hotel hotel = new Hotel();
		hotel.hotelPid = packageHotel.hotelPid;
		hotel.hotelId = packageHotel.hotelId;
		hotel.localizedName = packageHotel.localizedHotelName == null ? packageHotel.hotelName : packageHotel.localizedHotelName;
		hotel.address = packageHotel.hotelAddress.firstAddressLine;
		hotel.city = packageHotel.hotelAddress.city;
		hotel.stateProvinceCode = packageHotel.hotelAddress.province;
		hotel.countryCode = packageHotel.hotelAddress.countryAlpha3Code;
		hotel.postalCode = packageHotel.hotelAddress.postalCode;
		hotel.hotelStarRating = Float.valueOf(packageHotel.hotelStarRating);
		hotel.hotelGuestRating = packageHotel.overallReview;
		hotel.locationDescription = packageHotel.hotelDescription;
		hotel.latitude = packageHotel.latitude;
		hotel.longitude = packageHotel.longitude;
		hotel.largeThumbnailUrl = packageHotel.thumbnailURL;
		hotel.thumbnailUrl = packageHotel.thumbnailURL;
		hotel.isVipAccess = packageHotel.vip;
		hotel.packageOfferModel = packageHotel.packageOfferModel;
		hotel.isPackage = true;
		return hotel;
	}


	public static Hotel convertMultiItemHotel(HotelOffer multiItemHotel, MultiItemOffer offer) {
		Hotel hotel = new Hotel();
		hotel.hotelId = multiItemHotel.getId();
		hotel.localizedName = multiItemHotel.getName();
		if (multiItemHotel.getAddress() != null) {
			hotel.address = multiItemHotel.getAddress().getFirstAddressLine();
			hotel.city = multiItemHotel.getAddress().getCity();
			hotel.stateProvinceCode = multiItemHotel.getAddress().getProvinceCode();
			hotel.countryCode = multiItemHotel.getAddress().getThreeLetterCountryCode();
			hotel.postalCode = multiItemHotel.getAddress().getPostalCode();
			hotel.latitude = multiItemHotel.getAddress().getLatitude();
			hotel.longitude = multiItemHotel.getAddress().getLongitude();
		}
		hotel.hotelStarRating = (float) multiItemHotel.getStarRating();
		hotel.hotelGuestRating = NumberUtils.round(multiItemHotel.getAverageReview(), 1);
		hotel.locationDescription = multiItemHotel.getShortDescription();
		hotel.largeThumbnailUrl = multiItemHotel.getThumbnailUrl();
		hotel.thumbnailUrl = multiItemHotel.getThumbnailUrl();
		hotel.isVipAccess = multiItemHotel.getVip();
		hotel.packageOfferModel = new PackageOfferModel(offer);
		hotel.lowRateInfo = getLowRateInfo(offer);
		hotel.isPackage = true;
		return hotel;
	}

	private static HotelRate getLowRateInfo(MultiItemOffer offer) {
		HotelRate lowRateInfo = new HotelRate();
		lowRateInfo.strikethroughPriceToShowUsers = offer.getPrice().strikeThroughPricePerPerson().getAmount().floatValue();
		lowRateInfo.priceToShowUsers = offer.getPrice().pricePerPerson().getAmount().floatValue();
		lowRateInfo.currencyCode = offer.getPrice().pricePerPerson().getCurrency();
		return lowRateInfo;
	}
}
