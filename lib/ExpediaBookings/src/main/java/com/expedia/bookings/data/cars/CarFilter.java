package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CarFilter {
	// Total set of supported filters
	public Set<CarCategory> categoriesSupported = EnumSet.noneOf(CarCategory.class);
	public Set<String> suppliersSupported = new LinkedHashSet<>();

	// Current set of filters the user has chosen
	public Set<CarCategory> categoriesIncluded = EnumSet.noneOf(CarCategory.class);
	public Set<String> suppliersIncluded = new LinkedHashSet<>();

	public Transmission carTransmissionType;
	public boolean hasUnlimitedMileage;
	public boolean hasAirConditioning;

	public List<SearchCarOffer> applyFilters(CarSearchResponse carSearchResponse) {
		if (!altered()) {
			return carSearchResponse.offers;
		}

		List<SearchCarOffer> filteredOffers = new ArrayList<>();

		for (SearchCarOffer unfilteredCarSearchCarOffer : carSearchResponse.offers) {
			// If offer/category does not match whats selected in the filter, continue
			if (matches(unfilteredCarSearchCarOffer)) {
				filteredOffers.add(unfilteredCarSearchCarOffer);
			}
		}

		return filteredOffers;
	}

	private boolean matches(SearchCarOffer offer) {
		if (!categoriesIncluded.isEmpty() && !categoriesIncluded.contains(offer.vehicleInfo.category)) {
			return false;
		}
		if (!suppliersIncluded.isEmpty() && !suppliersIncluded.contains(offer.vendor.name)) {
			return false;
		}
		if (carTransmissionType != null && carTransmissionType != Transmission.UNKNOWN
			&& carTransmissionType != offer.vehicleInfo.transmission) {
			return false;
		}
		if (hasAirConditioning && !offer.vehicleInfo.hasAirConditioning) {
			return false;
		}
		return !hasUnlimitedMileage || offer.hasUnlimitedMileage;
	}

	// Checks if the user has altered the filter
	public boolean altered() {
		boolean ret = false;
		ret |= !categoriesIncluded.isEmpty();
		ret |= !suppliersIncluded.isEmpty();
		ret |= hasUnlimitedMileage;
		ret |= hasAirConditioning;
		ret |= carTransmissionType == Transmission.AUTOMATIC_TRANSMISSION;
		ret |= carTransmissionType == Transmission.MANUAL_TRANSMISSION;

		return ret;
	}
}
