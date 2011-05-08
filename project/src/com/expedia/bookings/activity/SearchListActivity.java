package com.expedia.bookings.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.HotelAdapter;
import com.expedia.bookings.widget.ListViewScrollBar;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Filter.OnFilterChangedListener;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchResponse;

public class SearchListActivity extends ListActivity implements SearchListener, OnScrollListener,
		OnFilterChangedListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	private static final int MAX_THUMBNAILS = 100;

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private ISearchActivity mParent;
	private HotelAdapter mAdapter;
	private SearchResponse mSearchResponse;

	private ListViewScrollBar mScrollBar;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_list);

		mParent = (ISearchActivity) getParent();
		mScrollBar = (ListViewScrollBar) findViewById(R.id.scroll_bar);

		mParent.addSearchListener(this);
		mScrollBar.setListView(getListView());
		mScrollBar.setOnScrollListener(this);

		if (AndroidUtils.getSdkVersion() == 11) {
			((ImageButton) findViewById(R.id.view_button)).setVisibility(View.GONE);
			final int left = mScrollBar.getPaddingLeft();
			final int top = mScrollBar.getPaddingLeft();
			final int right = mScrollBar.getPaddingLeft();

			mScrollBar.setPadding(left, top, right, top);
		}

		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		if (state != null) {
			onSearchCompleted(state.searchResponse);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mSearchResponse != null) {
			mSearchResponse.getFilter().removeOnFilterChangedListener(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mSearchResponse != null) {
			mSearchResponse.getFilter().addOnFilterChangedListener(this);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		ActivityState state = new ActivityState();
		state.searchResponse = mSearchResponse;

		return state;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Property property = (Property) mAdapter.getItem(position);

		Intent intent = new Intent(this, HotelActivity.class);
		intent.putExtra(Codes.PROPERTY, property.toJson().toString());
		intent.putExtra(Codes.SEARCH_PARAMS, mParent.getSearchParams().toString());
		intent.putExtra(Codes.SESSION, mParent.getSession().toJson().toString());
		intent.putExtra(HotelActivity.EXTRA_POSITION, position);
		startActivity(intent);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// Don't do any trimming for this version.
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFilterChanged() {
		mAdapter.rebuildCache();
		mScrollBar.rebuildCache();

		getListView().setSelection(0);
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
			// We assume that the parent handles errors, but just in case, don't crash if this happens
			return;
		}

		mSearchResponse = response;
		mSearchResponse.getFilter().addOnFilterChangedListener(this);

		mScrollBar.setSearchResponse(mSearchResponse);
		mAdapter = new HotelAdapter(this, mSearchResponse);
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

	//////////////////////////////////////////////////////////////////////////////////////////
	// Private classes

	private class ActivityState {
		public SearchResponse searchResponse;
	}
}