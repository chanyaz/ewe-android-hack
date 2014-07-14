package com.expedia.bookings.enums;

public enum ResultsSearchState {
	HOTELS_UP,
	FLIGHTS_UP,
	DEFAULT,
	DESTINATION,
	FLIGHT_ORIGIN,
	CALENDAR,
	TRAVELER_PICKER;

	/**
	 * @return whether or not this state associates with a waypoint fragment shown on screen
	 */
	public boolean showsWaypoint() {
		return this == DESTINATION || this == FLIGHT_ORIGIN;
	}

	/**
	 * @return whether or not the search knobs are shown on screen
	 */
	public boolean showsSearchControls() {
		return this == CALENDAR || this == TRAVELER_PICKER;
	}

}
