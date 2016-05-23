package com.expedia.bookings.data.packages;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.hotels.Hotel;
import com.google.gson.annotations.SerializedName;

public class PackageSearchResponse extends PackageBaseApiResponse {
	public PackageResult packageResult;
	public UniversalDataObject universalDataObject;

	public static class PackageResult {
		@SerializedName("hotels")
		public HotelPackage hotelsPackage;
		@SerializedName("flights")
		public FlightPackage flightsPackage;
		public PackageOfferModel currentSelectedOffer;
		public List<PackageOfferModel> packageOfferModels;
	}

	public static class HotelPackage {
		public List<Hotel> hotels = new ArrayList<>();
	}

	public static class FlightPackage {
		public List<FlightLeg> flights = new ArrayList<>();
	}

	public static class UniversalDataObject {
		public Entity entity;
	}

	public static class Entity {
		public PackageFHSearch packageFHSearch;
	}

	public static class PackageFHSearch {
		public PackageFHSearchResults packageFHSearchResults;
	}

	public static class PackageFHSearchResults {
		public int resultsCount;
		public int sponsoredListingsSize;
	}

	public static int getHotelResultsCount(PackageSearchResponse response) {
		return response.universalDataObject.entity.packageFHSearch.packageFHSearchResults.resultsCount;
	}

	public static boolean hasSponsoredHotelListing(PackageSearchResponse response) {
		return response.universalDataObject.entity.packageFHSearch.packageFHSearchResults.sponsoredListingsSize > 0;
	}
}
