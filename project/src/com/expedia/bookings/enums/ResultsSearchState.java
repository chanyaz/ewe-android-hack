package com.expedia.bookings.enums;

public enum ResultsSearchState {
	HOTELS_UP,
	FLIGHTS_UP,
	DEFAULT,
	DESTINATION,
	FLIGHT_ORIGIN,
	CALENDAR,
	TRAVELER_PICKER;

	public boolean searchControlsAreActive() {
		return this == CALENDAR || this == TRAVELER_PICKER;
	}

}
