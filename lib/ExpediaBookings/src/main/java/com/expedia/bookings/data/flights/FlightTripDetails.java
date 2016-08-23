package com.expedia.bookings.data.flights;

import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.insurance.InsuranceProduct;

public class FlightTripDetails {

	//TODO might need to add more fields once we know what's needed for the checkout screen
	public List<FlightLeg> legs;
	public FlightOffer offer;
	public FlightOffer oldOffer;
	public Money obFeePrice;

	public static class FlightOffer {
		public String productKey;
		public List<String> legIds;
		public List<PricePerPassengerCategory>  pricePerPassengerCategory;

		public Money baseFarePrice;
		public Money totalFarePrice;
		public Money averageTotalPricePerTicket;
		public Money taxesPrice;
		public Money feesPrice;
		public String currency;
		public String fees;

		public String baggageFeesUrl;
		public String fareType;
		public boolean isInternational;
		public boolean isPassportNeeded;
		public boolean isSplitTicket;
		public boolean hasBagFee;
		public boolean hasNoBagFee;
		public boolean showFees;
		public boolean mayChargeOBFees;
		public String numberOfTickets;
		public int seatsRemaining;
		public List<SplitFarePrice> splitFarePrice;

		public List<InsuranceProduct> availableInsuranceProducts;
		public InsuranceProduct selectedInsuranceProduct;

		public Money getBookingFee() {
			return new Money(fees, currency);
		}
	}

	public class SplitFarePrice {
		public Money totalPrice;
	}

	public class PricePerPassengerCategory implements Comparable<PricePerPassengerCategory> {
		public PassengerCategory passengerCategory;
		public Money basePrice;
		public Money totalPrice;
		public Money taxesPrice;

		@Override
		public int compareTo(PricePerPassengerCategory pricePerPassengerCategory) {
			return passengerCategory.compareTo(pricePerPassengerCategory.passengerCategory);
		}
	}

	public enum PassengerCategory {
		ADULT,
		SENIOR,
		ADULT_CHILD,
		CHILD,
		INFANT_IN_SEAT,
		INFANT_IN_LAP
	}
}
