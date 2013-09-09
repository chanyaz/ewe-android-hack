package com.expedia.bookings.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.widget.SimpleColorAdapter;
import com.mobiata.android.util.Ui;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.widget.TabletHotelAdapter;

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
	public void onListItemClick(ListView l, View v, int position, long id) {
		mHotelSelectedListener.onHotelSelected();
	}

	@Override
	protected ListAdapter initializeAdapter() {
		if (Db.getHotelSearch() == null || Db.getHotelSearch().getSearchResponse() == null) {
			int[] hotelColors = { Color.rgb(255, 0, 0), Color.rgb(220, 0, 0), Color.rgb(150, 0, 0) };
			mAdapter = new SimpleColorAdapter(getActivity(), 250, 25, hotelColors);
		}
		else {
			TabletHotelAdapter adapter = new TabletHotelAdapter(getActivity());
			mAdapter = adapter;
			adapter.highlightSelectedPosition(true);

			HotelSearchResponse response = Db.getHotelSearch().getSearchResponse();
			adapter.setSearchResponse(response);

			if (Db.getHotelSearch().getSelectedProperty() != null) {
				// In case there is a currently selected property, select it on the screen.
				adapter.setSelectedProperty(Db.getHotelSearch().getSelectedProperty());
			}
		}
		return mAdapter;
	}

	@Override
	protected CharSequence initializeStickyHeaderString() {
		int count = mAdapter == null ? 0 : mAdapter.getCount();
		CharSequence text = getResources().getQuantityString(R.plurals.number_of_hotels_TEMPLATE,
				count, count);
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
