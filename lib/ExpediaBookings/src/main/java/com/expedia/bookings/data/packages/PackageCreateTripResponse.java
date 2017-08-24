package com.expedia.bookings.data.packages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.flights.FlightTripDetails;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.google.gson.annotations.SerializedName;

public class PackageCreateTripResponse extends TripResponse {
	public final PackageDetails packageDetails;
	public final PackageDetails oldPackageDetails;
	public Money changedPrice;
	public String newTotalPrice;
	public final String packageRulesAndRestrictions;
	public Money selectedCardFees;
	public final Money totalPriceIncludingFees;

	@Nullable
	@Override
	public Money getOldPrice() {
		if (oldPackageDetails == null) {
			return null;
		}
		return oldPackageDetails.pricing.bundleTotal != null ? oldPackageDetails.pricing.bundleTotal : oldPackageDetails.pricing.packageTotal;
	}

	public static class PackageDetails {
		public String tealeafTransactionId;
		public String tripId;
		public String itineraryNumber;
		public final HotelCreateTripResponse.HotelProductResponse hotel;
		public final FlightProduct flight;
		public final Pricing pricing;
	}

	public static class Pricing {
		public final Money packageTotal;
		public Money basePrice;
		public Money totalTaxesAndFees;
		public Money hotelPrice;
		public Money flightPrice;
		public Money savings;
		public boolean taxesAndFeesIncluded;
		public final HotelPricing hotelPricing;
		public final Money bundleTotal;

		public boolean hasResortFee() {
			return hotelPricing != null && hotelPricing.mandatoryFees != null &&
				!hotelPricing.mandatoryFees.feeTotal.isZero();
		}
	}

	public static class HotelPricing {
		public final MandatoryFees mandatoryFees;
	}

	public static class MandatoryFees {
		public final Money feeTotal;
	}

	public static class FlightProduct {
		public final FlightTripDetails details;
		@SerializedName("rules")
		public final FlightCreateTripResponse.FlightRules flightRules;
	}

	@NotNull
	@Override
	public Money getTripTotalExcludingFee() {
		return packageDetails.pricing.packageTotal;
	}

	@Override
	public Money tripTotalPayableIncludingFeeIfZeroPayableByPoints() {
		if (totalPriceIncludingFees != null) {
			return totalPriceIncludingFees;
		}
		return packageDetails.pricing.packageTotal;
	}

	@NotNull
	@Override
	public boolean isCardDetailsRequiredForBooking() {
		return true;
	}

	public Money getBundleTotal() {
		return packageDetails.pricing.bundleTotal != null ? packageDetails.pricing.bundleTotal : tripTotalPayableIncludingFeeIfZeroPayableByPoints();
	}

	@Override
	public Money newPrice() {
		return getBundleTotal();
	}

}
