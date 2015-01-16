package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rkochhar on 1/15/15.
 */
public class CategorizedCarOffers {
	private CarCategory category;
	private List<CarOffer> offers;
	private CarOffer lowestTotalPriceOffer;

	public CategorizedCarOffers(CarCategory category) {
		this.category = category;
		offers = new ArrayList<>();
	}

	public List<CarOffer> getOffers() {
		return offers;
	}

	public CarOffer getLowestTotalPriceOffer() {
		return lowestTotalPriceOffer;
	}

	public void setLowestTotalPriceOffer(CarOffer lowestTotalPriceOffer) {
		this.lowestTotalPriceOffer = lowestTotalPriceOffer;
	}

	public CarCategory getCategory() {
		return category;
	}
}
