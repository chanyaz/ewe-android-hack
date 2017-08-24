package com.expedia.bookings.data.rail.responses;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.rail.RailPassenger;
import com.expedia.bookings.utils.CollectionUtils;

public abstract class BaseRailOffer {
	public Money totalPrice;
	public String railOfferToken;
	public final List<PriceBreakdown> priceBreakdown = Collections.emptyList();
	public final List<RailPassenger> passengerList = Collections.emptyList();

	private final Map<PriceCategoryCode, PriceBreakdown> mapping = new HashMap<>();

	public abstract List<? extends RailProduct> getRailProductList();

	@NotNull
	public Map<PriceCategoryCode, PriceBreakdown> getPriceBreakdownByCode() {
		return Collections.unmodifiableMap(getPriceBreakdownMap());
	}

	public void addPriceBreakdownForCode(Money money, PriceCategoryCode code) {
		PriceBreakdown priceBreakdown = new PriceBreakdown();
		if (money != null) {
			priceBreakdown.amount = money.amount;
			priceBreakdown.currencyCode = money.currencyCode;
			priceBreakdown.formattedPrice = money.formattedPrice;
			priceBreakdown.formattedWholePrice = money.formattedWholePrice;
		}
		priceBreakdown.priceCategoryCode = code;
		mapping.put(priceBreakdown.priceCategoryCode, priceBreakdown);
	}

	private Map<PriceCategoryCode, PriceBreakdown> getPriceBreakdownMap() {
		for (PriceBreakdown breakdown : priceBreakdown) {
			mapping.put(breakdown.priceCategoryCode, breakdown);
		}
		return mapping;
	}

	public boolean isOpenReturn() {
		boolean openReturn = false;
		if (CollectionUtils.isNotEmpty(getRailProductList())) {
			openReturn = getRailProductList().get(0).openReturn;
		}
		return openReturn;
	}

	public static class PriceBreakdown {
		public BigDecimal amount;
		public String currencyCode;
		public String formattedPrice;
		public String formattedWholePrice;
		public String priceCategory;
		public PriceCategoryCode priceCategoryCode;

		public boolean isZero() {
			return (amount == null || amount.compareTo(BigDecimal.ZERO) == 0);
		}
	}

	public enum PriceCategoryCode {
		TICKET,
		TICKET_DELIVERY,
		EXPEDIA_SERVICE_FEE,
		CREDIT_CARD_FEE
	}
}
