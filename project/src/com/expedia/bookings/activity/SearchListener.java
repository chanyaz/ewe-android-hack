package com.expedia.bookings.activity;

import com.expedia.bookings.data.SearchResponse;

public interface SearchListener {
	public void onSearchStarted();

	public void onSearchCompleted(SearchResponse response);

	public void clearResults();
}
