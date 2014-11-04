package com.expedia.bookings.enums;

public enum ResultsFlightLegState {
	LIST_DOWN,
	FILTERS,
	DETAILS,
	ADDING_TO_TRIP,
	LATER_LEG,
	;

	public boolean shouldShowExpandedHeader() {
		return this != LIST_DOWN;
	}

}
