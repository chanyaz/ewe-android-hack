package com.expedia.bookings.enums;

public enum ResultsFlightsState {
	LOADING,
	SEARCH_ERROR,
	FLIGHT_LIST_DOWN,
	CHOOSING_FLIGHT,
	ADDING_FLIGHT_TO_TRIP,
	NO_FLIGHTS_DROPDOWN_POS,
	NO_FLIGHTS_POS,
	MISSING_ORIGIN,
	MISSING_STARTDATE,
	INVALID_START_DATE,
	ZERO_RESULT;

	public ResultsState getResultsState() {
		switch (this) {
		case LOADING:
		case MISSING_ORIGIN:
		case MISSING_STARTDATE:
		case NO_FLIGHTS_DROPDOWN_POS:
		case NO_FLIGHTS_POS:
		case FLIGHT_LIST_DOWN:
		case ZERO_RESULT:
		case SEARCH_ERROR:
		case INVALID_START_DATE:
			return ResultsState.OVERVIEW;
		case CHOOSING_FLIGHT:
		case ADDING_FLIGHT_TO_TRIP:
		default:
			return ResultsState.FLIGHTS;
		}
	}

	/*
	 * Helper method to check if for the given state, we should show the respective message or show the results list.
	 */
	public boolean isShowMessageState() {
		switch(this) {
		case MISSING_ORIGIN:
		case MISSING_STARTDATE:
		case NO_FLIGHTS_DROPDOWN_POS:
		case NO_FLIGHTS_POS:
		case ZERO_RESULT:
		case SEARCH_ERROR:
		case INVALID_START_DATE:
			return true;
		default:
			return false;
		}
	}

	public boolean isFlightListState() {
		switch(this) {
		case LOADING:
		case FLIGHT_LIST_DOWN:
		case CHOOSING_FLIGHT:
		case ADDING_FLIGHT_TO_TRIP:
			return true;
		default:
			return false;
		}
	}

	public boolean showsActionBar() {
		switch (this) {
		case LOADING:
		case MISSING_ORIGIN:
		case MISSING_STARTDATE:
		case NO_FLIGHTS_DROPDOWN_POS:
		case NO_FLIGHTS_POS:
		case FLIGHT_LIST_DOWN:
		case ZERO_RESULT:
		case SEARCH_ERROR:
		case INVALID_START_DATE:
			return false;
		case CHOOSING_FLIGHT:
		case ADDING_FLIGHT_TO_TRIP:
		default:
			return true;
		}
	}

}
