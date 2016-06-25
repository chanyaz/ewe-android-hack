package com.expedia.bookings.data.flights;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripDetails;
import com.expedia.bookings.data.TripResponse;
import com.expedia.bookings.data.insurance.InsuranceProduct;
import com.google.gson.annotations.SerializedName;

public class FlightCreateTripResponse extends TripResponse {
	public List<InsuranceProduct> availableInsuranceProducts;
	public FlightTripDetails details;
	public Money totalPrice;
	public TripDetails newTrip;
	public String tealeafTransactionId;

	@SerializedName("rules")
	public FlightRules flightRules;

	@NotNull
	@Override
	public Money getTripTotalExcludingFee() {
		return totalPrice;
	}

	@Override
	public Money tripTotalPayableIncludingFeeIfZeroPayableByPoints() {
		throw new UnsupportedOperationException("TripTotalIncludingFee is not implemented for flights");
	}

	@NotNull
	@Override
	public boolean isCardDetailsRequiredForBooking() {
		return true;
	}

	public class FlightRules {
		@SerializedName("RuleToTextMap")
		public Map<String, String> rulesToText;

		@SerializedName("RuleToUrlMap")
		public Map<String, String> rulesToUrl;

	}
}
