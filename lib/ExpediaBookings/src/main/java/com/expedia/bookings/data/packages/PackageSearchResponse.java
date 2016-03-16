package com.expedia.bookings.data.packages;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.hotels.Hotel;
import com.google.gson.annotations.SerializedName;

public class PackageSearchResponse extends BaseApiResponse {
	public PackageResult packageResult;

	public static class PackageResult {
		@SerializedName("hotels")
		public HotelPackage hotelsPackage;
		@SerializedName("flights")
		public FlightPackage flightsPackage;
		public PackageOfferModel currentSelectedOffer;
		public List<PackageOfferModel> packageOfferModels;
	}

	public static class HotelPackage {
		public transient List<Hotel> hotels = new ArrayList<>();
	}

	public static class FlightPackage {
		public transient List<FlightLeg> flights = new ArrayList<>();
	}
}
