package com.expedia.bookings.data.packages;

public class PackageHotel {
	public final String hotelPid;
	public boolean allInclusiveAvailable;
	public boolean available;
	public int distanceFromAirport;
	public String distanceFromAirportUnit;
	public String drrHook;
	public String drrMessage;
	public boolean exclusiveAmenity;
	public final HotelAddress hotelAddress;
	public final String hotelDescription;
//	hotelExtraModelList
	public final String hotelId;
	public final String hotelName;
	public final String hotelStarRating;
	public String infositeURL;
	public final double latitude;
	public final String localizedHotelName;
	public final double longitude;
	public String neighborhood;
	public final float overallReview;
	public boolean pinnedHotel;
//	ratePlanAmenities
	public String ratePlanCode;
	public int recommendationTotal;
	public int reviewTotal;
	public String roomType;
	public String roomTypeCode;
	public boolean showRatingAsStars;
	public boolean showTHNectarDiv;
	public String superlative;
	public final String thumbnailURL;
	public final boolean vip;
	public final PackageOfferModel packageOfferModel;

	public static class HotelAddress {
		public final String city;
		public final String countryAlpha3Code;
		public final String firstAddressLine;
		public final String postalCode;
		public final String province;
		public String provinceShort;
		public String secondAddressLine;
	}
}
