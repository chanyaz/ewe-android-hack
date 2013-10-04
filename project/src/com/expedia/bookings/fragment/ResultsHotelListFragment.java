package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter.OnFilterChangedListener;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.widget.FlightAdapter;
import com.expedia.bookings.widget.SimpleColorAdapter;
import com.expedia.bookings.widget.TabletHotelAdapter;
import com.mobiata.android.util.Ui;

/**
 * ResultsHotelListFragment: The hotel list fragment designed for tablet results 2013
 */
public class ResultsHotelListFragment extends ResultsListFragment implements OnFilterChangedListener {

	public interface ISortAndFilterListener {
		public void onSortAndFilterClicked();
	}

	private ListAdapter mAdapter;
	private ISortAndFilterListener mSortAndFilterListener;
	private IResultsHotelSelectedListener mHotelSelectedListener;
	private List<ISortAndFilterListener> mSortAndFilterListeners = new ArrayList<ResultsHotelListFragment.ISortAndFilterListener>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mSortAndFilterListener = Ui.findFragmentListener(this, ISortAndFilterListener.class, true);
		mHotelSelectedListener = Ui.findFragmentListener(this, IResultsHotelSelectedListener.class, true);
		mSortAndFilterListeners.add(mSortAndFilterListener);
	}

	@Override
	public void onStart() {
		super.onStart();

		Db.getFilter().addOnFilterChangedListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		Db.getFilter().removeOnFilterChangedListener(this);
	}

	@Override
	public void onFilterChanged() {
		updateAdapter();
		setStickyHeaderText(initializeStickyHeaderString());
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mAdapter instanceof TabletHotelAdapter) {
			int headerCount = getTopSpaceListView().getHeaderViewsCount();
			int itemPosition = position - headerCount;
			if (itemPosition >= 0) {
				Db.getHotelSearch().setSelectedProperty((Property) mAdapter.getItem(itemPosition));
				mHotelSelectedListener.onHotelSelected();
			}
		}
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
			count = response.getFilteredPropertiesCount();
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
	protected OnClickListener initializeTopRightTextButtonOnClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (ISortAndFilterListener listener : mSortAndFilterListeners) {
					listener.onSortAndFilterClicked();
				}
			}
		};
	}

	@Override
	protected boolean initializeTopRightTextButtonEnabled() {
		return true;
	}

	public void addSortAndFilterListener(ISortAndFilterListener sortAndFilterListener) {
		if (!mSortAndFilterListeners.contains(sortAndFilterListener)) {
			mSortAndFilterListeners.add(sortAndFilterListener);
		}
	}
}
