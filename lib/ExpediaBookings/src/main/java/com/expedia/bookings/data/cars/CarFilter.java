package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class CarFilter {
	public  LinkedHashSet carCategoryCheckedFilter = new LinkedHashSet();
	public  LinkedHashSet carSupplierCheckedFilter = new LinkedHashSet();
	public  Transmission carTransmissionType;
	public boolean hasUnlimitedMileage;
	public boolean hasAirConditioning;

	public List<SearchCarOffer> applyFilters(CarSearchResponse carSearchResponse, CarFilter carFilter) {
		List<SearchCarOffer> filteredSeachCarOffer = new ArrayList<>();

		for (SearchCarOffer unfilteredCarSearchCarOffer : carSearchResponse.offers) {
			// If offer/category does not match whats selected in the filter, continue
			if (matches(unfilteredCarSearchCarOffer, carFilter)) {
				filteredSeachCarOffer.add(unfilteredCarSearchCarOffer);
			}
		}

		if (!filteredSeachCarOffer.isEmpty()) {
			return filteredSeachCarOffer;
		}
		else if (carFilter.carCategoryCheckedFilter.isEmpty() && carFilter.carSupplierCheckedFilter.isEmpty()
			&& carFilter.carTransmissionType == null
			&& !carFilter.hasAirConditioning && !carFilter.hasUnlimitedMileage) {
			return carSearchResponse.offers;
		}
		throw new ApiError(ApiError.Code.CAR_FILTER_NO_RESULTS);
	}

	private boolean matches(SearchCarOffer offer, CarFilter carFilter) {

		if (!carFilter.carCategoryCheckedFilter.isEmpty() && !carFilter.carCategoryCheckedFilter
			.contains(offer.vehicleInfo.category.toString())) {
			return false;
		}
		if (!carFilter.carSupplierCheckedFilter.isEmpty() && !carFilter.carSupplierCheckedFilter
			.contains(offer.vendor.name)) {
			return false;
		}
		if (carFilter.carTransmissionType != null && carFilter.carTransmissionType != Transmission.UNKNOWN
			&& carFilter.carTransmissionType != offer.vehicleInfo.transmission) {
			return false;
		}
		if (carFilter.hasAirConditioning && !offer.vehicleInfo.hasAirConditioning) {
			return false;
		}
		if (carFilter.hasUnlimitedMileage && !offer.hasUnlimitedMileage) {
			return false;
		}
		return true;
	}
}
