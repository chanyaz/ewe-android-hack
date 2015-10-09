package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.utils.Strings;

public class CarSearch {
	public List<CategorizedCarOffers> categories = new ArrayList<>();

	public boolean hasDisplayLabel(String displayLabel) {
		for (int i = 0, count = categories.size(); i < count; i++) {
			CategorizedCarOffers result = categories.get(i);
			if (Strings.equals(result.carCategoryDisplayLabel, displayLabel)) {
				return true;
			}
		}

		return false;
	}

	public CategorizedCarOffers getFromDisplayLabel(String displayLabel) {
		for (int i = 0, count = categories.size(); i < count; i++) {
			CategorizedCarOffers result = categories.get(i);
			if (Strings.equals(result.carCategoryDisplayLabel, displayLabel)) {
				return result;
			}
		}

		throw new RuntimeException("Tried to find a category that does not exist displayLabel=" + displayLabel);
	}

	public SearchCarOffer getLowestTotalPriceOffer() {
		SearchCarOffer lowestTotalPriceOffer = null;
		for (CategorizedCarOffers category : categories) {
			if (lowestTotalPriceOffer == null
				|| category.getLowestTotalPriceOffer().fare.total.compareTo(lowestTotalPriceOffer.fare.total) < 0) {
				lowestTotalPriceOffer = category.getLowestTotalPriceOffer();
			}
		}
		return lowestTotalPriceOffer;
	}
}
