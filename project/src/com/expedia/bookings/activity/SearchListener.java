package com.expedia.bookings.activity;

import com.expedia.bookings.data.SearchResponse;

public interface SearchListener {
	public void onSearchStarted();

	public void onSearchProgress(int strId);

	public void onSearchFailed(String message);

	public void onSearchCompleted(SearchResponse response);

	public boolean hasSearchResults();

	public void clearResults();
}
