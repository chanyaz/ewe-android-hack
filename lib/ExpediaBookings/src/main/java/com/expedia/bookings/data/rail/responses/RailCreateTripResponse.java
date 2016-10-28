package com.expedia.bookings.data.rail.responses;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.flights.ValidFormOfPayment;
import com.expedia.bookings.utils.CollectionUtils;

public class RailCreateTripResponse extends BaseApiResponse {
	public Money totalPrice;
	public String offerToken;
	public RailDomainProduct railDomainProduct;
	public List<RailValidFormOfPayment> validFormsOfPayment;
	public String tripId;

	// Set through code
	@Nullable
	public Money totalPriceIncludingFees;
	public Money selectedCardFees;
	public Money ticketDeliveryFees;

	public static class RailDomainProduct {
		public RailTripOffer railOffer;
	}

	public static class RailTripOffer extends BaseRailOffer {
		public List<RailTripProduct> railProductList;
		public List<RailTicketDeliveryOption> ticketDeliveryOptionList;

		@Override
		public List<? extends RailProduct> getRailProductList() {
			return railProductList;
		}

		@Nullable
		public RailLegOption getOutboundLegOption() {
			if (CollectionUtils.isNotEmpty(railProductList)) {
				return railProductList.get(0).getLegOption();
			}
			return null;
		}

		@Nullable
		public RailLegOption getInboundLegOption() {
			// todo will change with open return
			if (CollectionUtils.isNotEmpty(railProductList) && isRoundTrip()) {
				return railProductList.get(1).getLegOption();
			}
			return null;
		}

		public boolean isRoundTrip() {
			return railProductList.size() == 2;
		}

	}

	public static class RailTripProduct extends RailProduct {
		public List<RailLegOption> legOptionList;

		public RailLegOption getLegOption() {
			return legOptionList.get(0);
		}
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

	// total = totalPrice + tdo fees + cc fees + booking fee (if present)
	public Money getTotalPayablePrice() {
		if (totalPriceIncludingFees != null && !totalPriceIncludingFees.isZero()) {
			return totalPriceIncludingFees;
		}
		return totalPrice;
	}

	public Money getTicketDeliveryFeeForOption(String tdoToken) {
		List<RailTicketDeliveryOption> ticketDeliveryOptionList = railDomainProduct.railOffer.ticketDeliveryOptionList;
		if (CollectionUtils.isNotEmpty(ticketDeliveryOptionList)) {
			for (RailTicketDeliveryOption deliveryOption : ticketDeliveryOptionList) {
				if (tdoToken.equals(deliveryOption.ticketDeliveryOptionToken.name())) {
					return deliveryOption.ticketDeliveryFee;
				}
			}
		}
		return null;
	}

	public void updateOfferWithTDOAndCCFees() {
		RailTripOffer offer = railDomainProduct.railOffer;
		offer.addPriceBreakdownForCode(ticketDeliveryFees, BaseRailOffer.PriceCategoryCode.TICKET_DELIVERY);
		offer.addPriceBreakdownForCode(selectedCardFees, BaseRailOffer.PriceCategoryCode.CREDIT_CARD_FEE);
	}
}
