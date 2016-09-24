package com.expedia.bookings.data.flights;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripDetails;
import com.expedia.bookings.data.insurance.InsuranceProduct;
import com.google.gson.annotations.SerializedName;

public class FlightCreateTripResponse extends AbstractFlightOfferResponse {

	public TripDetails newTrip;
	public String tealeafTransactionId;
	public Money totalPrice;

	@SerializedName("rules")
	public FlightRules flightRules;

	public FlightTripDetails.FlightOffer getOffer() {
		return getDetails().offer;
	}

	@NotNull
	public List<InsuranceProduct> getAvailableInsuranceProducts() {
		FlightTripDetails.FlightOffer offer = getOffer();
		return (offer.availableInsuranceProducts != null)
			? offer.availableInsuranceProducts
			: Collections.<InsuranceProduct>emptyList();
	}

	@Nullable
	public InsuranceProduct getSelectedInsuranceProduct() {
		return getOffer().selectedInsuranceProduct;
	}

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
