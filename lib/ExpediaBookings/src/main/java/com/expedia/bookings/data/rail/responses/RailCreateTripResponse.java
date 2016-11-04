package com.expedia.bookings.data.rail.responses;

import java.util.List;
import java.util.Map;

import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.flights.ValidFormOfPayment;


public class RailCreateTripResponse extends BaseApiResponse {
	public Money totalPrice;
	public String offerToken;
	public RailDomainProduct railDomainProduct;
	public List<RailValidFormOfPayment> validFormsOfPayment;
	public String tripId;
	public Money totalPriceIncludingFees = null;
	public Money selectedCardFees = null;

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
		public RailTicketDeliveryOptionToken ticketDeliveryOptionToken;
		public String ticketDeliveryOptionCategoryCode;
		public String ticketDeliveryDescription;
		public Money ticketDeliveryFee;
		public List<String> ticketDeliveryCountryCodeList;
		public boolean departureStation;
	}

	public static class RailValidFormOfPayment extends ValidFormOfPayment {
		public Map<RailTicketDeliveryOptionToken, Fee> fees;
	}

	public static class Fee {
		public Money feePrice;
		public Money tripTotalPrice;
	}

	public enum RailTicketDeliveryOptionToken {
		SEND_BY_EXPRESS_POST_UK,
		SEND_BY_OVERNIGHT_POST_GLOBAL,
		SEND_BY_OVERNIGHT_POST_EU,
		PICK_UP_AT_TICKETING_OFFICE_NONE,
		KIOSK_NONE,
		SEND_BY_POST_UK,
		SEND_BY_OVERNIGHT_POST_UK
	}
}
