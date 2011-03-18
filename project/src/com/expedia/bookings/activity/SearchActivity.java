package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityGroup;
import android.os.Bundle;

import com.mobiata.hotellib.app.SearchListener;

public class SearchActivity extends ActivityGroup {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants
	
	//////////////////////////////////////////////////////////////////////////////////
	// Private members
	
	private List<SearchListener> mSearchListeners;
	
	//////////////////////////////////////////////////////////////////////////////////
	// Overrides
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
	}
	
	@Override
    public void onContentChanged() {
        super.onContentChanged();

    }
	
	//////////////////////////////////////////////////////////////////////////////////
	// Public methods
	
	public void addSearchListner(SearchListener searchListener) {
		if(mSearchListeners == null) {
			mSearchListeners = new ArrayList<SearchListener>();
		}
		
		mSearchListeners.add(searchListener);
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// Private methods
}