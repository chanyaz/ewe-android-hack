package com.expedia.bookings.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelFilter.OnFilterChangedListener;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.widget.SimpleColorAdapter;
import com.expedia.bookings.widget.TabletHotelAdapter;
import com.mobiata.android.util.Ui;

/**
 * ResultsHotelListFragment: The hotel list fragment designed for tablet results 2013
 */
public class ResultsHotelListFragment extends ResultsListFragment {

	public interface ISortAndFilterListener {
		public void onSortAndFilterClicked();
	}

	private ListAdapter mAdapter;
	private ISortAndFilterListener mSortAndFilterListener;
	private IResultsHotelSelectedListener mHotelSelectedListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mSortAndFilterListener = Ui.findFragmentListener(this, ISortAndFilterListener.class, true);
		mHotelSelectedListener = Ui.findFragmentListener(this, IResultsHotelSelectedListener.class, true);
	}

	@Override
	public void onStart() {
		super.onStart();

		HotelFilter filter = Db.getFilter();
		if (filter != null) {
			filter.addOnFilterChangedListener(mListener);
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		HotelFilter filter = Db.getFilter();
		if (filter != null) {
			filter.removeOnFilterChangedListener(mListener);
		}
	}

	OnFilterChangedListener mListener = new OnFilterChangedListener() {
		public void onFilterChanged() {
			updateAdapter();
			setStickyHeaderText(initializeStickyHeaderString());
		}
	};

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mHotelSelectedListener.onHotelSelected();
	}

	@Override
	protected ListAdapter initializeAdapter() {

		// TODO: This block is temporary
		if (Db.getHotelSearch() == null || Db.getHotelSearch().getSearchResponse() == null) {
			int[] hotelColors = { Color.rgb(255, 0, 0), Color.rgb(220, 0, 0), Color.rgb(150, 0, 0) };
			mAdapter = new SimpleColorAdapter(getActivity(), 250, 25, hotelColors);
			return mAdapter;
		}

		TabletHotelAdapter adapter = new TabletHotelAdapter(getActivity());
		mAdapter = adapter;
		adapter.highlightSelectedPosition(true);

		updateAdapter();

		return mAdapter;
	}

	private void updateAdapter() {
		TabletHotelAdapter adapter = (TabletHotelAdapter) mAdapter;

		HotelSearchResponse response = Db.getHotelSearch().getSearchResponse();
		adapter.setSearchResponse(response);

		if (Db.getHotelSearch().getSelectedProperty() != null) {
			// In case there is a currently selected property, select it on the screen.
			adapter.setSelectedProperty(Db.getHotelSearch().getSelectedProperty());
		}
	}

	@Override
	protected CharSequence initializeStickyHeaderString() {
		int total = 0;
		int count = 0;
		HotelSearchResponse response = Db.getHotelSearch().getSearchResponse();
		if (response != null) {
			total = response.getPropertiesCount();
			count = response.getFilteredAndSortedProperties().length;
		}

		CharSequence text = null;
		if (count == total) {
			text = getResources().getQuantityString(R.plurals.x_Hotels_TEMPLATE, total, total);
		}
		else {
			text = getResources().getQuantityString(R.plurals.x_of_y_Hotels_TEMPLATE, total, count, total);
		}
		return text;
	}

	@Override
	protected OnClickListener initializeSortAndFilterOnClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSortAndFilterListener.onSortAndFilterClicked();
			}
		};
	}

	@Override
	protected boolean initializeSortAndFilterEnabled() {
		return true;
	}
}
