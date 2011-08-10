package com.expedia.bookings.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity.SetShowDistanceListener;
import com.expedia.bookings.widget.HotelAdapter;
import com.expedia.bookings.widget.ListViewScrollBar;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Filter.OnFilterChangedListener;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchResponse;

public class SearchListActivity extends ListActivity implements SearchListener, OnScrollListener,
		OnFilterChangedListener, SetShowDistanceListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	private static final int MAX_THUMBNAILS = 100;

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private SearchActivity mParent;
	private HotelAdapter mAdapter;
	private SearchResponse mSearchResponse;

	private ListViewScrollBar mScrollBar;

	private boolean mShowDistance = true;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_list);

		mParent = (SearchActivity) getParent();
		mScrollBar = (ListViewScrollBar) findViewById(R.id.scroll_bar);

		mParent.addSearchListener(this);
		mParent.addSetShowDistanceListener(this);

		mScrollBar.setListView(getListView());
		mScrollBar.setOnScrollListener(this);

		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		if (state != null) {			
			mSearchResponse = state.searchResponse;
			mSearchResponse.getFilter().addOnFilterChangedListener(this);

			mScrollBar.setSearchResponse(mSearchResponse);
			mAdapter = state.adapter;
			setListAdapter(mAdapter);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		ActivityState state = new ActivityState();
		state.searchResponse = mSearchResponse;
		state.adapter = mAdapter;

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

	private int mLastCenter = -999;

	private static final int TRIM_TOLERANCE = 5;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// Trim the ends (recycle images)
		if (totalItemCount > MAX_THUMBNAILS) {
			final int center = firstVisibleItem + (visibleItemCount / 2);

			// Don't always trim drawables; only trim them if we've moved the list far enough away from where
			// we last were.
			if (center < mLastCenter - TRIM_TOLERANCE || center > mLastCenter + TRIM_TOLERANCE) {
				mLastCenter = center;

				int start = center - (MAX_THUMBNAILS / 2);
				int end = center + (MAX_THUMBNAILS / 2);

				// prevent overflow
				start = start < 0 ? 0 : start;
				end = end > totalItemCount ? totalItemCount : end;

				mAdapter.trimDrawables(start, end);
			}
		}
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
		mAdapter = new HotelAdapter(this, mSearchResponse, mParent.getOnDrawBookingInfoTextRowListener());
		mAdapter.setShowDistance(mShowDistance);
		setListAdapter(mAdapter);
	}

	@Override
	public void onSetShowDistance(boolean showDistance) {
		mShowDistance = showDistance;
		if (mAdapter != null) {
			mAdapter.setShowDistance(showDistance);
		}
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
		public HotelAdapter adapter;
	}
}