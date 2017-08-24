package com.expedia.bookings.data.rail.responses;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.BaseApiResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.flights.ValidFormOfPayment;
import com.expedia.bookings.utils.CollectionUtils;

public class RailCreateTripResponse extends BaseApiResponse {
	public final Money totalPrice;
	public String offerToken;
	public final RailDomainProduct railDomainProduct;
	public final List<RailValidFormOfPayment> validFormsOfPayment;
	public final String tripId;
	public final RailResponseStatus responseStatus;

	// Set through code
	@Nullable
	public final Money totalPriceIncludingFees;
	public final Money selectedCardFees;
	public final Money ticketDeliveryFees;

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

	public boolean isErrorResponse() {
		return (hasErrors() || (responseStatus != null &&
			!responseStatus.status.equals(RailsApiStatusCodes.STATUS_SUCCESS)));
	}
}
