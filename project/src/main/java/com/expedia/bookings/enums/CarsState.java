package com.expedia.bookings.enums;

import com.expedia.bookings.data.cars.CarSearchParams;

public enum CarsState {
	SEARCH,
	LOADING,
	RESULTS,
	DETAILS,
	CHECKOUT;

	public String pathStringRepresentation; // cars/search
	public CarSearchParams params; // used for results
	public String productKey; // used for checkout

}
