package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CarSearch {

	public Map<CarCategory, CategorizedCarOffers> carCategoryOfferMap;

	public CarSearch() {
		reset();
	}

	public void reset() {
		carCategoryOfferMap = new EnumMap<>(CarCategory.class);
	}

	public void updateOfferMap(CarOffer carOffer) {
		CarCategory offerCategory = carOffer.vehicleInfo.category;
		if (carCategoryOfferMap.get(offerCategory) == null) {
			carCategoryOfferMap.put(offerCategory, new CategorizedCarOffers(offerCategory));
		}

		CategorizedCarOffers categorizedOffer = carCategoryOfferMap.get(offerCategory);

		List<CarOffer> carOffers = categorizedOffer.getOffers();
		CarOffer totalPriceOffer = categorizedOffer.getLowestTotalPriceOffer();
		if (totalPriceOffer == null || (carOffer.fare.total.compareTo(totalPriceOffer.fare.total) == -1)) {
			categorizedOffer.setLowestTotalPriceOffer(carOffer);
		}
		carOffers.add(carOffer);
	}

	public List<CategorizedCarOffers> getCategoriesFromResponse() {
		return new ArrayList<CategorizedCarOffers>(carCategoryOfferMap.values());
	}
}
