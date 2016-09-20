package com.expedia.bookings.data.rail.responses;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;

public class RailCreateTripResponse extends TripResponse {

	public Money totalPrice;
	public String offerToken;
	public RailDomainProduct railDomainProduct;

	public static class RailDomainProduct {
		public RailTripOffer railOffer;
	}

	public static class RailTripOffer extends BaseRailOffer {
		public List<RailTripProduct> railProductList;
		public List<RailTicketDeliveryOption> ticketDeliveryOptionList;
	}

	public static class RailTripProduct extends RailProduct {
		public List<RailLegOption> legList;
	}

	public static class RailTicketDeliveryOption {
		public String ticketDeliveryOptionToken;
		public String ticketDeliveryOptionCategoryCode;
		public String ticketDeliveryDescription;
		public Money ticketDeliveryFee;
		public List<String> ticketDeliveryCountryCodeList;
		public boolean departureStation;
	}

	@NotNull
	@Override
	public Money getTripTotalExcludingFee() {
		return totalPrice;
	}

	@NotNull
	@Override
	public Money tripTotalPayableIncludingFeeIfZeroPayableByPoints() {       //what does it mean???
		throw new UnsupportedOperationException("TripTotalIncludingFee is not implemented for rail");
	}

	@Override
	public boolean isCardDetailsRequiredForBooking() {
		return true;
	}
}
