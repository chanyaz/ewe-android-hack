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
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchResponse;

public class SearchListActivity extends ListActivity implements SearchListener, OnScrollListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private SearchActivity mParent;
	private HotelAdapter mAdapter;

	private ImageButton mViewButton;

	private boolean mIsScrolling = false;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_list);

		mParent = (SearchActivity) getParent();
		mParent.addSearchListener(this);

		mViewButton = (ImageButton) findViewById(R.id.view_button);
		mViewButton.setOnClickListener(mViewButtonClickListener);
		
		getListView().setOnScrollListener(this);

		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		if (state != null) {
			mAdapter = state.adapter;

			if (mAdapter != null) {
				setListAdapter(mAdapter);
			}
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		ActivityState state = new ActivityState();
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
			// TODO: Error handling?  Or should we assume that the parent never calls this with null?
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
	// Listeners

	private final View.OnClickListener mViewButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// Stop scrolling
			if (mIsScrolling) {
				getListView().setSelection(getListView().getFirstVisiblePosition());
			}

			mParent.switchResultsView();
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// Private classes

	private class ActivityState {
		public HotelAdapter adapter;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mIsScrolling = (scrollState != SCROLL_STATE_IDLE);
	}
}