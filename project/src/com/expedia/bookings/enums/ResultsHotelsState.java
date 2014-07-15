package com.expedia.bookings.enums;

public enum ResultsHotelsState {
	LOADING,
	SEARCH_ERROR,
	HOTEL_LIST_DOWN,
	HOTEL_LIST_UP,
	HOTEL_LIST_AND_FILTERS,
	ROOMS_AND_RATES,
	REVIEWS,
	GALLERY,
	ADDING_HOTEL_TO_TRIP,
	MAX_HOTEL_STAY;

	public ResultsState getResultsState() {
		switch (this) {
		case LOADING:
		case SEARCH_ERROR:
		case HOTEL_LIST_DOWN:
		case MAX_HOTEL_STAY:
			return ResultsState.OVERVIEW;
		case HOTEL_LIST_UP:
		case HOTEL_LIST_AND_FILTERS:
		case ROOMS_AND_RATES:
		case REVIEWS:
		case GALLERY:
		case ADDING_HOTEL_TO_TRIP:
		default:
			return ResultsState.HOTELS;
		}
	}

	/*
	 * Helper method to check if for the given state, we should show the respective message or show the results list.
	 */
	public boolean isShowMessageState() {
		switch (this) {
		case SEARCH_ERROR:
		case MAX_HOTEL_STAY:
			return true;
		default:
			return false;
		}
	}
}
