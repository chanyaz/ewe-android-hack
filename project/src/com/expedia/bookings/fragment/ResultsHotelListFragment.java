package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter.OnFilterChangedListener;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.ResultsHotelsListState;
import com.expedia.bookings.enums.ResultsListState;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.widget.TabletHotelAdapter;
import com.mobiata.android.util.Ui;

/**
 * ResultsHotelListFragment: The hotel list fragment designed for tablet results 2013
 */
public class ResultsHotelListFragment extends ResultsListFragment<ResultsHotelsListState> implements
		OnFilterChangedListener {

	public interface ISortAndFilterListener {
		public void onSortAndFilterClicked();
	}

	private TabletHotelAdapter mAdapter;
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

		if (getActivity() != null) {
			boolean shouldShowVipIcon = PointOfSale.getPointOfSale().supportsVipAccess()
					&& User.isElitePlus(getActivity());
			mAdapter.setShowVipIcon(shouldShowVipIcon);
		}

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
		int headerCount = getListView().getHeaderViewsCount();
		int itemPosition = position - headerCount;
		if (itemPosition >= 0) {
			Db.getHotelSearch().setSelectedProperty((Property) mAdapter.getItem(itemPosition));
			mHotelSelectedListener.onHotelSelected();
		}
	}

	@Override
	protected ListAdapter initializeAdapter() {
		TabletHotelAdapter adapter = new TabletHotelAdapter(getActivity());
		mAdapter = adapter;
		adapter.highlightSelectedPosition(true);

		updateAdapter();

		return mAdapter;
	}

	private void updateAdapter() {
		HotelSearch search = Db.getHotelSearch();
		mAdapter.setSearchResponse(search.getSearchResponse());
		mAdapter.setShowDistance(
				search != null
						&& search.getSearchParams().getSearchType() != null
						&& search.getSearchParams().getSearchType().shouldShowDistance());

		if (Db.getHotelSearch().getSelectedProperty() != null) {
			// In case there is a currently selected property, select it on the screen.
			mAdapter.setSelectedProperty(Db.getHotelSearch().getSelectedProperty());
		}
	}

	@Override
	protected CharSequence initializeStickyHeaderString() {
		int total = 0;
		int count = 0;
		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		HotelSearchResponse response = Db.getHotelSearch().getSearchResponse();
		if (response != null) {
			total = response.getPropertiesCount();
			count = response.getFilteredPropertiesCount(params);
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

	@Override
	protected ResultsHotelsListState translateState(ResultsListState state) {
		if (state == ResultsListState.AT_TOP) {
			return ResultsHotelsListState.HOTELS_LIST_AT_TOP;
		}
		else if (state == ResultsListState.AT_BOTTOM) {
			return ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM;
		}
		return null;
	}

	@Override
	protected ResultsHotelsListState getDefaultState() {
		return ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM;
	}

	public void onHotelSelected() {
		Property property = Db.getHotelSearch().getSelectedProperty();
		getListView().setSelection(getListView().getHeaderViewsCount() + mAdapter.getPositionOfProperty(property));
	}
}
