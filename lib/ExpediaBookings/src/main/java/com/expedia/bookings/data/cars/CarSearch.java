package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CarSearch {

	public Map<CarCategory, List<CarOffer>> carCategoryOfferMap;

	public void reset() {
		carCategoryOfferMap = new EnumMap<>(CarCategory.class);
	}

	public void updateOfferMap(CarOffer carOffer) {
		if (carCategoryOfferMap.get(carOffer.vehicleInfo.category) == null) {
			carCategoryOfferMap.put(carOffer.vehicleInfo.category, new ArrayList<CarOffer>());
		}
		carCategoryOfferMap.get(carOffer.vehicleInfo.category).add(carOffer);
	}

}
