package com.expedia.bookings.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.HotelAdapter;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchResponse;

public class SearchListActivity extends ListActivity implements SearchListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private SearchActivity mParent;
	private HotelAdapter mAdapter;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_list);

		mParent = (SearchActivity) getParent();
		mParent.addSearchListener(this);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Property property = (Property) mAdapter.getItem(position);

		Intent intent = new Intent(this, HotelActivity.class);
		intent.putExtra(Codes.PROPERTY, property.toJson().toString());
		intent.putExtra(Codes.SEARCH_PARAMS, (new SearchParams()).toString());
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// SearchListener implementation

	@Override
	public void onSearchStarted() {
		clearResults();
	}

	@Override
	public void onSearchProgress(int strId) {
		// Do nothing.  SearchActivity should handle the display of search progress.
	}

	@Override
	public void onSearchFailed(String message) {
		// Do nothing.  SearchActivity should handle the display of search progress.
	}

	@Override
	public void onSearchCompleted(SearchResponse response) {
		if (response == null) {
			// TODO: Handle error
			return;
		}

		mAdapter = new HotelAdapter(this, response);
		setListAdapter(mAdapter);
	}

	@Override
	public boolean hasSearchResults() {
		return mAdapter != null && !mAdapter.isEmpty();
	}

	@Override
	public void clearResults() {
		setListAdapter(null);
		mAdapter = null;
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

}
