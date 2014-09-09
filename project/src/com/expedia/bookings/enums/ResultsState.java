package com.expedia.bookings.enums;

/**
 * This represents states of Tablet Results 2013
 */
public enum ResultsState {
	OVERVIEW,
	HOTELS,
	FLIGHTS,
	;

	public boolean supportsTouchingTripBucket() {
		return this == OVERVIEW;
	}

}
