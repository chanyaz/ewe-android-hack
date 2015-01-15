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
		if (carCategoryOfferMap.get(carOffer.vehicleInfo.category) == null) {
			carCategoryOfferMap.put(carOffer.vehicleInfo.category, new CategorizedCarOffers());
		}

		CategorizedCarOffers categorizedCarOffers = carCategoryOfferMap.get(carOffer.vehicleInfo.category);

		List<CarOffer> carOffers = categorizedCarOffers.getOffers();
		CarOffer bestOffer = categorizedCarOffers.getSelectedOffer();
		if(bestOffer == null || (carOffer.fare.total.compareTo(bestOffer.fare.total) == -1)) {
			categorizedCarOffers.setSelectedOffer(carOffer);
		}
		
		carOffers.add(carOffer);
	}

	public List<CarCategory> getCategoriesFromResponse() {
		List<CarCategory> test = new ArrayList<CarCategory>();
		for(Map.Entry<CarCategory, CategorizedCarOffers> entry : carCategoryOfferMap.entrySet()) {
			test.add(entry.getKey());
		}
		return test;
	}
}
