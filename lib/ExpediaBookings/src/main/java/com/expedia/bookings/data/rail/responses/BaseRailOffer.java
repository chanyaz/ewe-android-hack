package com.expedia.bookings.data.rail.responses;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.rail.RailPassenger;

public abstract class BaseRailOffer {
	public Money totalPrice;
	public String railOfferToken;
	public List<PriceBreakdown> priceBreakdown;
	public List<RailPassenger> passengerList;

	@NotNull
	public Map<PriceCategoryCode, PriceBreakdown> getPriceBreakdownByCode() {
		Map<PriceCategoryCode, PriceBreakdown> mapping = new HashMap<>();
		for (PriceBreakdown breakdown : priceBreakdown) {
			mapping.put(breakdown.priceCategoryCode, breakdown);
		}
		return Collections.unmodifiableMap(mapping);
	}

	public static class PriceBreakdown {
		public String formattedPrice;
		public String formattedWholePrice;
		public String priceCategory;
		public PriceCategoryCode priceCategoryCode;
	}

	public enum PriceCategoryCode {
		TICKET,
		TICKET_DELIVERY,
		EXPEDIA_SERVICE_FEE,
		CREDIT_CARD_FEE
	}
}
