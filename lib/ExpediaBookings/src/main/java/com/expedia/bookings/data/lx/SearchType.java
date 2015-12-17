package com.expedia.bookings.data.lx;

public enum SearchType {
	//Default Search is the implicit search which app fires based on Current Location
	//Searches where user has provided the parameters are non-default, explicit searches
	DEFAULT_SEARCH,
	EXPLICIT_SEARCH,
}
