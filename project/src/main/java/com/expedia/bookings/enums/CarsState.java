package com.expedia.bookings.enums;

import com.expedia.bookings.data.cars.CategorizedCarOffers;

public enum CarsState {
	SEARCH,
	LOADING,
	RESULTS,
	DETAILS,
	CHECKOUT;

	public CategorizedCarOffers offers;

}
