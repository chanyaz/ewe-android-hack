package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.List;

public class CategorizedCarOffers {
	public CarCategory category;
	public CarType type;
	public List<SearchCarOffer> offers = new ArrayList<>();

	private SearchCarOffer lowestTotalPriceOffer;

	public CategorizedCarOffers(CarCategory category, CarType type) {
		this.category = category;
		this.type = type;
	}

	public void add(SearchCarOffer offer) {
		if (lowestTotalPriceOffer == null ||
			offer.fare.total.compareTo(lowestTotalPriceOffer.fare.total) < 0) {
			lowestTotalPriceOffer = offer;
		}
		offers.add(offer);
	}

	public SearchCarOffer getLowestTotalPriceOffer() {
		return lowestTotalPriceOffer;
	}
}
