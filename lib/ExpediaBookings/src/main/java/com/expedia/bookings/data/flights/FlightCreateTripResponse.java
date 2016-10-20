package com.expedia.bookings.data.flights;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;
import com.google.gson.annotations.SerializedName;

public class FlightCreateTripResponse extends TripResponse {

	public String tealeafTransactionId;
	public Money totalPrice;

	@SerializedName("rules")
	public FlightRules flightRules;

	@NotNull
	@Override
	public Money getTripTotalExcludingFee() {
		throw new UnsupportedOperationException("TripTotalExcludingFee is not implemented for flights. totalPrice field is untouched/fee-less");
	}

	@NotNull
	@Override
	public Money tripTotalPayableIncludingFeeIfZeroPayableByPoints() {
		if (getTotalPriceIncludingFees() != null) {
			return getTotalPriceIncludingFees();
		}
		Money totalPrice = getDetails().offer.totalPrice.copy();
		totalPrice.add(getSelectedCardFees());
		return totalPrice;
	}

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
