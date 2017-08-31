package com.expedia.bookings.data.packages;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.multiitem.BundleSearchResponse;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

public class PackageSearchResponse extends PackageBaseApiResponse implements BundleSearchResponse {
	@SerializedName("packagePageInput")
	public PackageInfo packageInfo;
	public PackageResult packageResult;
//	public UniversalDataObject universalDataObject;

	@Override
	public String getHotelCheckInDate() {
		return packageInfo.hotelCheckinDate.isoDate;
	}

	@Override
	public String getHotelCheckOutDate() {
		return packageInfo.hotelCheckoutDate.isoDate;
	}

	@Override
	public int getHotelResultsCount() {
//		return universalDataObject.entity.packageFHSearch.packageFHSearchResults.resultsCount;
		return 0;
	}

	@Override
	public boolean hasSponsoredHotelListing() {
//		return universalDataObject.entity.packageFHSearch.packageFHSearchResults.sponsoredListingsSize > 0;
		return false;
	}

	@Override
	public String getCurrencyCode() {
		return packageResult.packageOfferModels.get(0).price.packageTotalPrice.currencyCode;
	}

	@Override
	@Nullable
	public PackageOfferModel getCurrentOfferModel() {
		return packageResult.currentSelectedOffer;
	}

	@Override
	public void setCurrentOfferModel(PackageOfferModel offerModel) {
		packageResult.currentSelectedOffer = offerModel;
	}

	@Override
	public List<Hotel> getHotels() {
		return packageResult.hotelsPackage.hotels;
	}

	@Override
	public List<FlightLeg> getFlightLegs() {
		return packageResult.flightsPackage.flights;
	}

	public static class PackageInfo {
		public HotelCheckinDate hotelCheckinDate;
		public HotelCheckoutDate hotelCheckoutDate;
	}

	public static class HotelCheckinDate {
		public String isoDate;
	}

	public static class HotelCheckoutDate {
		public String isoDate;
	}

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
}
