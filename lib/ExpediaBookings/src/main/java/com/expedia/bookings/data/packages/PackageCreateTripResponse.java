package com.expedia.bookings.data.packages;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;

public class PackageCreateTripResponse extends TripResponse {
	public PackageDetails packageDetails;
	public PackageDetails oldPackageDetails;
	public Money changedPrice;
	public String newTotalPrice;
	
	public static class PackageDetails {
		public String tealeafTransactionId;
		public String tripId;
		public String itineraryNumber;
		public HotelCreateTripResponse.HotelProductResponse hotel;
		public FlightProduct flight;
		public Pricing pricing;
	}

	public static class Pricing {
		public Money packageTotal;
		public Money basePrice;
		public Money totalTaxesAndFees;
		public Money hotelPrice;
		public Money flightPrice;
		public Money savings;
		public boolean taxesAndFeesIncluded;
	}

	public static class FlightProduct {
		public com.expedia.bookings.data.flights.FlightTripDetails details;
	}

	@NotNull
	@Override
	public Money getTripTotal() {
		return packageDetails.pricing.packageTotal;
	}

	@NotNull
	@Override
	public boolean isCardDetailsRequiredForBooking() {
		return true;
	}
}
