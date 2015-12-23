package com.expedia.bookings.data.hotels;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.packages.Hotel;
import com.expedia.bookings.data.cars.BaseApiResponse;
import com.expedia.bookings.data.packages.FlightLeg;
import com.google.gson.annotations.SerializedName;

public class PackageSearchResponse extends BaseApiResponse {
	public PackageResult packageResult;

	public static class PackageResult {
		@SerializedName("hotels")
		public HotelPackage hotelsPackage;
		@SerializedName("flights")
		public FlightPackage flightsPackage;
	}

	public static class HotelPackage {
		public transient List<Hotel> hotels = new ArrayList<>();
	}

	public static class FlightPackage {
		public transient List<FlightLeg> flights = new ArrayList<>();
	}
}
