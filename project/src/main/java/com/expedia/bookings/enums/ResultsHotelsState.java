package com.expedia.bookings.enums;

public enum ResultsHotelsState {
	LOADING,
	SEARCH_ERROR,
	HOTEL_LIST_DOWN,
	HOTEL_LIST_UP,
	LOADING_HOTEL_LIST_UP,
	MAP,
	HOTEL_LIST_AND_FILTERS,
	ROOMS_AND_RATES,
	REVIEWS,
	GALLERY,
	ADDING_HOTEL_TO_TRIP,
	ZERO_RESULT,
	MAX_HOTEL_STAY;

	public ResultsState getResultsState() {
		switch (this) {
		case LOADING:
		case SEARCH_ERROR:
		case HOTEL_LIST_DOWN:
		case MAX_HOTEL_STAY:
		case ZERO_RESULT:
			return ResultsState.OVERVIEW;
		case HOTEL_LIST_UP:
		case LOADING_HOTEL_LIST_UP:
		case MAP:
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
		case ZERO_RESULT:
		case SEARCH_ERROR:
		case MAX_HOTEL_STAY:
			return true;
		default:
			return false;
		}
	}

	public boolean showsActionBar() {
		switch (this) {
		case LOADING:
		case SEARCH_ERROR:
		case HOTEL_LIST_DOWN:
		case MAX_HOTEL_STAY:
		case ZERO_RESULT:
			return false;
		case HOTEL_LIST_UP:
		case LOADING_HOTEL_LIST_UP:
		case MAP:
		case HOTEL_LIST_AND_FILTERS:
		case ROOMS_AND_RATES:
		case REVIEWS:
		case GALLERY:
		case ADDING_HOTEL_TO_TRIP:
		default:
			return true;
		}
	}

	public boolean showLoading() {
		return this == LOADING || this == LOADING_HOTEL_LIST_UP;
	}

}
