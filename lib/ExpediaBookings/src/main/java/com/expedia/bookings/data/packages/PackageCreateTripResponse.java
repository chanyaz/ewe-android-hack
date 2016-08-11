package com.expedia.bookings.data.packages;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;
import com.expedia.bookings.data.flights.FlightTripDetails;
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
		public HotelPricing hotelPricing;
		public Money bundleTotal;

		public Money getBundleTotal() {
			return bundleTotal != null ? bundleTotal : packageTotal;
		}

		public boolean hasResortFee() {
			return hotelPricing != null && hotelPricing.mandatoryFees != null &&
				!hotelPricing.mandatoryFees.feeTotal.isZero();
		}
	}

	public static class HotelPricing {
		public MandatoryFees mandatoryFees;
	}

	public static class MandatoryFees {
		public Money feeTotal;
	}

	public static class FlightProduct {
		public FlightTripDetails details;
	}

	@NotNull
	@Override
	public Money getTripTotalExcludingFee() {
		return packageDetails.pricing.packageTotal;
	}

	@Override
	public Money tripTotalPayableIncludingFeeIfZeroPayableByPoints() {
		throw new UnsupportedOperationException("TripTotalIncludingFee is not implemented for packages");
	}

	@NotNull
	@Override
	public boolean isCardDetailsRequiredForBooking() {
		return true;
	}
}
