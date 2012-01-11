package com.expedia.bookings.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity.SetShowDistanceListener;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.widget.HotelAdapter;
import com.expedia.bookings.widget.ListViewScrollBar;

public class SearchListActivity extends ListActivity implements SearchListener, OnScrollListener,
		OnFilterChangedListener, SetShowDistanceListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	private static final int MAX_THUMBNAILS = 100;

	private static final String STATE_POSITION = "STATE_POSITION";

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private PhoneSearchActivity mParent;
	private HotelAdapter mAdapter;
	private SearchResponse mSearchResponse;

	private ListViewScrollBar mScrollBar;
	private TextView mBookingInfoHeader;

	private boolean mShowDistance = true;

	// The selected position from the last instance; cleared once used
	private int mSelectedPosition = -1;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_list);

		mParent = (PhoneSearchActivity) getParent();
		mScrollBar = (ListViewScrollBar) findViewById(R.id.scroll_bar);

		mParent.addSearchListener(this);
		mParent.addSetShowDistanceListener(this);

		mScrollBar.setListView(getListView());
		mScrollBar.setOnScrollListener(this);

		mBookingInfoHeader = (TextView) getLayoutInflater().inflate(R.layout.row_booking_info, null);
		getListView().addHeaderView(mBookingInfoHeader);

		if (savedInstanceState != null) {
			mSelectedPosition = savedInstanceState.getInt(STATE_POSITION, -1);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_POSITION, mSelectedPosition);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Property property = (Property) l.getAdapter().getItem(position);

		// nothing to do here as the view does not contain a 
		// property
		if (property == null) {
			return;
		}

		Intent intent = new Intent(this, HotelActivity.class);
		intent.putExtra(Codes.PROPERTY, property.toJson().toString());
		intent.putExtra(Codes.SEARCH_PARAMS, mParent.getSearchParams().toString());
		intent.putExtra(HotelActivity.EXTRA_POSITION, position);
		startActivity(intent);
	}

	private int mLastCenter = -999;

	private static final int TRIM_TOLERANCE = 5;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mSelectedPosition = firstVisibleItem;

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
		// Do nothing.  PhoneSearchActivity should handle the display of search progress.
	}

	@Override
	public void onSearchFailed(String message) {
		// Do nothing.  PhoneSearchActivity should handle the display of search progress.
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
		mAdapter.setShowDistance(mShowDistance);
		mBookingInfoHeader.setText(mParent.getBookingInfoHeaderText());
		setListAdapter(mAdapter);

		if (mSelectedPosition != -1) {
			setSelection(mSelectedPosition);
		}
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
		mSelectedPosition = -1;
		mScrollBar.setSearchResponse(null);
	}
}