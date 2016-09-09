package com.expedia.bookings.data.flights;

import java.util.List;

import org.joda.time.DateTime;

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
		public Money totalPrice;
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

	// until the API consistently returns the legs in order , this will ensure they are in order (outbound, inbound)
	public List<FlightLeg> getLegs() {
		if (legs.size()  > 1) {
			FlightLeg firstLeg = legs.get(0);
			FlightLeg secondLeg = legs.get(1);
			DateTime outboundDateTime = DateTime.parse(firstLeg.segments.get(0).departureTimeRaw);
			DateTime inboundDateTime = DateTime.parse(secondLeg.segments.get(0).departureTimeRaw);

			if (outboundDateTime.isAfter(inboundDateTime)) {
				legs.set(0, secondLeg);
				legs.set(1, firstLeg);
			}
		}
		return legs;
	}
}
