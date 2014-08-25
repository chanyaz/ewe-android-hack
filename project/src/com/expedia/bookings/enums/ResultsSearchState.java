package com.expedia.bookings.enums;

public enum ResultsSearchState {
	HOTELS_UP,
	FLIGHTS_UP,
	DEFAULT,
	DESTINATION,
	FLIGHT_ORIGIN,
	CALENDAR,
	CALENDAR_WITH_POPUP,
	TRAVELER_PICKER;

	/**
	 * @return whether or not this state associates with a waypoint fragment shown on screen
	 */
	public boolean showsWaypoint() {
		return this == DESTINATION || this == FLIGHT_ORIGIN;
	}

	public boolean showsCalendar() {
		return this == CALENDAR || this == CALENDAR_WITH_POPUP;
	}

	public boolean isUpState() {
		return this == HOTELS_UP || this == FLIGHTS_UP;
	}

	/**
	 * @return whether or not the search knobs (i.e. destination,
	 * flight origin, dates, travelers) are shown on screen.
	 */
	public boolean showsSearchControls() {
		return this == DEFAULT || this == CALENDAR;
	}

	public boolean showsSearchPopup() {
		return this == CALENDAR_WITH_POPUP || this == TRAVELER_PICKER;
	}

	public boolean showsActionBar() {
		switch (this) {
		case DESTINATION:
		case FLIGHT_ORIGIN:
		case HOTELS_UP:
		case FLIGHTS_UP:
			return false;
		case DEFAULT:
		case CALENDAR:
		case CALENDAR_WITH_POPUP:
		case TRAVELER_PICKER:
		default:
			return true;
		}
	}
}
