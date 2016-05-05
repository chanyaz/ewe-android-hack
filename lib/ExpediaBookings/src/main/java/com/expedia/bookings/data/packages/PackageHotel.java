package com.expedia.bookings.data.packages;

public class PackageHotel {
	public String hotelPid;
	public boolean allInclusiveAvailable;
	public boolean available;
	public int distanceFromAirport;
	public String distanceFromAirportUnit;
	public String drrHook;
	public String drrMessage;
	public boolean exclusiveAmenity;
	public HotelAddress hotelAddress;
	public String hotelDescription;
//	hotelExtraModelList
	public String hotelId;
	public String hotelName;
	public String hotelStarRating;
	public String infositeURL;
	public double latitude;
	public String localizedHotelName;
	public double longitude;
	public String neighborhood;
	public float overallReview;
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
	public String thumbnailURL;
	public boolean vip;
	public PackageOfferModel packageOfferModel;

	public static class HotelAddress {
		public String city;
		public String countryAlpha3Code;
		public String firstAddressLine;
		public String postalCode;
		public String province;
		public String provinceShort;
		public String secondAddressLine;
	}
}
