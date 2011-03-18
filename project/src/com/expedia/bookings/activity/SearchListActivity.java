package com.expedia.bookings.activity;

import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.SearchResponse;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class SearchListActivity extends ListActivity implements SearchListener, OnScrollListener {
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

		mParent = (SearchActivity) getParent();
		
		mParent.addSearchListner(this);		
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
