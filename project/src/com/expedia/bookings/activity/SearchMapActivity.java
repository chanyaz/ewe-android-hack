package com.expedia.bookings.activity;

import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.expedia.bookings.R;
import com.google.android.maps.MapActivity;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.SearchResponse;

public class SearchMapActivity extends MapActivity implements SearchListener, OnScrollListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private SearchActivity mParent;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_map);

		mParent = (SearchActivity) getParent();
		mParent.addSearchListner(this);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////////
	// OnScrollListener implementation

	@Override
	public void onContentChanged() {
		super.onContentChanged();

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	//////////////////////////////////////////////////////////////////////////////////
	// SearchListener implementation

	@Override
	public void onSearchStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSearchProgress(int strId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSearchFailed(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSearchCompleted(SearchResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasSearchResults() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearResults() {
		// TODO Auto-generated method stub

	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

}
