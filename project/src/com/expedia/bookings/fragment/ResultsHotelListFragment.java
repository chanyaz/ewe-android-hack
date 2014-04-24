package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter.OnFilterChangedListener;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.ResultsHotelsListState;
import com.expedia.bookings.enums.ResultsListState;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.section.HotelSummarySection;
import com.expedia.bookings.widget.FruitList;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setListViewContentDescription(R.string.cd_tablet_results_hotel_list);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	// All this work for awesome Hotel Card expand/contract animation
	IStateListener<ResultsListState> mExpandyListener = new IStateListener<ResultsListState>() {
		@Override
		public void onStateTransitionStart(ResultsListState stateOne, ResultsListState stateTwo) {
		}

		@TargetApi(11)
		@Override
		public void onStateTransitionUpdate(ResultsListState stateOne, ResultsListState stateTwo, float percentage) {
			float expandedPercentage = percentage;
			if (stateOne == ResultsListState.AT_TOP) {
				expandedPercentage = 1 - expandedPercentage;
			}
			FruitList listView = getListView();
			int adapterPosition = listView.getFirstVisiblePosition();
			int overallTranslationY = 0;
			for (int listIndex = 0; listIndex < listView.getChildCount(); listIndex++) {
				View child = listView.getChildAt(listIndex);
				//child.setTranslationY(overallTranslationY);
				if (!(child instanceof HotelSummarySection)) {
					continue;
				}
				int expandableHeight = mAdapter.estimateExpandableHeight(adapterPosition);
				int expandPixels = (int) ((expandedPercentage - 1) * expandableHeight);
				if (expandableHeight != 0) {
					HotelSummarySection hss = (HotelSummarySection) child;
					hss.expandBy(expandPixels);
					overallTranslationY += expandPixels;
				}
				adapterPosition++;
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsListState stateOne, ResultsListState stateTwo) {
		}

		@Override
		public void onStateFinalized(ResultsListState state) {
		}
	};

	@Override
	public void onStart() {
		super.onStart();

		if (getActivity() != null) {
			boolean shouldShowVipIcon = PointOfSale.getPointOfSale().supportsVipAccess()
				&& User.isElitePlus(getActivity());
			mAdapter.setShowVipIcon(shouldShowVipIcon);
		}

		getListView().registerStateListener(mExpandyListener, true);
		Db.getFilter().addOnFilterChangedListener(this);

		//TODO: make the list items start off collapsed. because this doesn't fire at the right time.
		mExpandyListener.onStateTransitionUpdate(ResultsListState.AT_BOTTOM, ResultsListState.AT_TOP, 0f);
	}

	@Override
	public void onStop() {
		super.onStop();

		getListView().unRegisterStateListener(mExpandyListener);
		Db.getFilter().removeOnFilterChangedListener(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mAdapter.unregisterDataSetObserver(mDataSetObserver);
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
			mAdapter.setSelectedPosition(itemPosition);
			mAdapter.notifyDataSetChanged();
			Property property = (Property) mAdapter.getItem(itemPosition);
			Db.getHotelSearch().setSelectedProperty(property);
			mHotelSelectedListener.onHotelSelected();
		}
	}

	@Override
	protected ListAdapter initializeAdapter() {
		TabletHotelAdapter adapter = new TabletHotelAdapter(getActivity());
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataSetObserver);
		adapter.highlightSelectedPosition(true);

		updateAdapter();

		return mAdapter;
	}

	public void updateAdapter() {
		if (mAdapter != null) {
			HotelSearch search = Db.getHotelSearch();
			mAdapter.setSearchResponse(search.getSearchResponse());
			mAdapter.setShowDistance(
				search != null
					&& search.getSearchParams().getSearchType() != null
					&& search.getSearchParams().getSearchType().shouldShowDistance()
			);

			if (Db.getHotelSearch().getSelectedProperty() != null) {
				// In case there is a currently selected property, select it on the screen.
				mAdapter.setSelectedProperty(Db.getHotelSearch().getSelectedProperty());
			}
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
		if (Sp.getParams().getStartDate() == null) {
			//It would be better to check Sp from outside, but this is a pretty cheap way to set this up
			text = getString(R.string.hotels_tonight);
		}
		else if (count == total) {
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
	protected OnClickListener initializeStickyLeftOnClickListener() {
		// Do nothing for now
		return null;
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

	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			setStickyHeaderText(initializeStickyHeaderString());
		}
	};

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
