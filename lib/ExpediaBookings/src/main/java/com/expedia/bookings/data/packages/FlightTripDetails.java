package com.expedia.bookings.data.packages;

import java.util.List;

import com.expedia.bookings.data.Money;

public class FlightTripDetails {

	//TODO might need to add more fields once we know what's needed for the checkout screen
	public FlightOffer offer;
	public Money obFeePrice;

	public static class FlightOffer {
		public List<String> legIds;

		public Money baseFarePrice;
		public Money totalFarePrice;
		public Money averageTotalPricePerTicket;
		public Money taxesPrice;
		public Money feesPrice;

		public String baggageFeesUrl;
		public boolean isInternational;
		public boolean isPassportNeeded;
		public boolean hasBagFee;
		public boolean hasNoBagFee;
		public boolean showFees;
		public String numberOfTickets;
	}
}
