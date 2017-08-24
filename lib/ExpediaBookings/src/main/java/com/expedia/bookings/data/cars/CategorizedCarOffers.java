package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.utils.Interval;

public class CategorizedCarOffers {
	public CarCategory category;
	public CarType type;
	public String carCategoryDisplayLabel;
	public final List<SearchCarOffer> offers = new ArrayList<>();

	// Storing data used to display icons, keeping it sorted
	// as an optimization.
	public final Interval passengerSet = new Interval();
	public final Interval luggageSet = new Interval();
	public final Interval doorSet = new Interval();

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
		passengerSet.addIgnoreZero(offer.vehicleInfo.adultCapacity);
		luggageSet.addIgnoreZero(offer.vehicleInfo.largeLuggageCapacity);
		doorSet.addIgnoreZero(offer.vehicleInfo.maxDoors);
		doorSet.addIgnoreZero(offer.vehicleInfo.minDoors);
	}

	public SearchCarOffer getLowestTotalPriceOffer() {
		return lowestTotalPriceOffer;
	}
}
