package com.expedia.bookings.data.flights;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.insurance.InsuranceProduct;
import com.expedia.bookings.data.payment.LoyaltyInformation;
import com.google.gson.annotations.SerializedName;

public class FlightTripDetails {

	//TODO might need to add more fields once we know what's needed for the checkout screen
	public final List<FlightLeg> legs;
	public final FlightOffer offer;
	public FlightOffer oldOffer;
	public Money obFeePrice;
	public String basicEconomyFareRules = "";

	public static class FlightOffer {
		public String productKey;
		public List<String> legIds;
		public List<PricePerPassengerCategory>  pricePerPassengerCategory;

		public Money baseFarePrice;
		public final Money totalPrice;
		public Money averageTotalPricePerTicket;
		public final Money discountAmount;
		public Money taxesPrice;
		public Money feesPrice;
		public final String currency;
		public final String fees;

		public String baggageFeesUrl;
		public final String fareType;
		public boolean isInternational;
		public boolean isPassportNeeded;
		public final boolean isSplitTicket;
		public boolean hasBagFee;
		public boolean hasNoBagFee;
		public boolean showFees;
		public boolean mayChargeOBFees;
		public final String numberOfTickets;
		public int seatsRemaining;
		public final List<SplitFarePrice> splitFarePrice;
		@SerializedName("segmentAttributes")
		public final List<List<SeatClassAndBookingCode>> offersSeatClassAndBookingCode;
		public LoyaltyInformation loyaltyInfo;

		public final List<InsuranceProduct> availableInsuranceProducts = Collections.emptyList();
		public final InsuranceProduct selectedInsuranceProduct;

		public Money getBookingFee() {
			return new Money(fees, currency);
		}

		@Nullable
		public Money getTotalPriceWithInsurance() {
			// the mobile API does not currently populate InsuranceProduct.tripTotalPriceWithInsurance within
			// selectedInsuranceProduct, so we must find the selected product among the availableInsuranceProducts
			// and pull tripTotalPriceWithInsurance from there. TODO: remove this method when mAPI is improved
			if (selectedInsuranceProduct != null) {
				for (InsuranceProduct insuranceProduct : availableInsuranceProducts) {
					if (insuranceProduct.productId.equals(selectedInsuranceProduct.productId)) {
						return insuranceProduct.tripTotalPriceWithInsurance;
					}
				}
			}
			return null;
		}
	}

	public class SplitFarePrice {
		public final Money totalPrice;
	}

	public class PricePerPassengerCategory implements Comparable<PricePerPassengerCategory> {
		public final PassengerCategory passengerCategory;
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

	public class SeatClassAndBookingCode {
		public String bookingCode;
		@SerializedName("cabinCode")
		public final String seatClass;
	}
}
