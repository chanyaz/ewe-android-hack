package com.expedia.bookings.data.flights;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.FlightTripResponse;
import com.expedia.bookings.data.Money;
import com.google.gson.annotations.SerializedName;

public class FlightCreateTripResponse extends FlightTripResponse {

	public String tealeafTransactionId;
	public Money totalPrice;

	public FrequentFlyerPlans frequentFlyerPlans;

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

	@Override
	public FlightTripDetails.FlightOffer getOffer() {
		return details.offer;
	}

	public class FlightRules {
		@SerializedName("RuleToTextMap")
		public Map<String, String> rulesToText;

		@SerializedName("RuleToUrlMap")
		public Map<String, String> rulesToUrl;

	}

	public class FrequentFlyerPlans {
		public List<AllFrequentFlyerPlans> allFrequentFlyerPlans;
		public List<EnrolledFrequentFlyerPlans> enrolledFrequentFlyerPlans;

	}

	public class AllFrequentFlyerPlans {
		public String frequentFlyerPlanID;
		public String frequentFlyerPlanCode;
		public String airlineCode;
		public String frequentFlyerPlanName;
	}

	public class EnrolledFrequentFlyerPlans {
		public String frequentFlyerPlanID;
		public String frequentFlyerPlanCode;
		public String airlineCode;
		public String membershipNumber;
		public String frequentFlyerPlanName;
	}

}
