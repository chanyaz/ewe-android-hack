package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.List;

public class CategorizedCarOffers {
	public CarCategory category;
	public CarType type;
	public String carCategoryDisplayLabel;
	public List<SearchCarOffer> offers = new ArrayList<>();

	private SearchCarOffer lowestTotalPriceOffer;

	public CategorizedCarOffers() {
	}

	public CategorizedCarOffers(String carCategoryDisplayLabel, CarCategory category) {
		this.carCategoryDisplayLabel = carCategoryDisplayLabel;
		this.category = category;
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
