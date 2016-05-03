package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoScrollListener;
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
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.section.HotelSummarySection;
import com.expedia.bookings.tracking.AdImpressionTracking;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.FruitList;
import com.expedia.bookings.widget.TabletHotelAdapter;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;

/**
 * ResultsHotelListFragment: The hotel list fragment designed for tablet results 2013
 */
public class ResultsHotelListFragment extends ResultsListFragment<ResultsHotelsListState>
	implements OnFilterChangedListener {

	public interface ISortAndFilterListener {
		void onSortAndFilterClicked();
	}

	private TabletHotelAdapter mAdapter;
	private IResultsHotelSelectedListener mHotelSelectedListener;
	private List<ISortAndFilterListener> mSortAndFilterListeners = new ArrayList<ResultsHotelListFragment.ISortAndFilterListener>();
	private ResultsHotelsListState mState = getDefaultState();

	private static final String PICASSO_TAG = "HOTEL_LIST";

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		ISortAndFilterListener sortAndFilterListener = Ui
			.findFragmentListener(this, ISortAndFilterListener.class, true);
		mHotelSelectedListener = Ui.findFragmentListener(this, IResultsHotelSelectedListener.class, true);
		mSortAndFilterListeners.add(sortAndFilterListener);
	}

	@Override
	public int getLayoutResId() {
		return R.layout.fragment_tablet_results_hotel_list;
	}

	@Override
	public float getMaxHeaderTranslateY() {
		return getListView().getTop()
			+ getListView().getMaxDistanceFromTop()
			+ getListView().getPaddingTop()
			+ getListView().getDividerHeight() // To overlap the divider at the top of the list
			- getStickyHeader().getTop()
			- getStickyHeader().getHeight();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setListViewContentDescription(R.string.cd_tablet_results_hotel_list);
		View view = super.onCreateView(inflater, container, savedInstanceState);
		view.setBackgroundResource(R.drawable.bg_half_white);
		FruitList listView = getListView();
		listView.addPicassoScrollListener(new PicassoScrollListener(getActivity(), PICASSO_TAG));
		return view;
	}

	// All this work for awesome Hotel Card expand/contract animation
	IStateListener<ResultsHotelsListState> mExpandyListener = new IStateListener<ResultsHotelsListState>() {
		@Override
		public void onStateTransitionStart(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo) {
		}

		@Override
		public void onStateTransitionUpdate(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo,
			float percentage) {
			float collapsePct = 0f;
			if (stateTwo == ResultsHotelsListState.HOTELS_LIST_AT_TOP) {
				collapsePct = Math.max(0f, (0.5f - percentage) * 2f);
			}
			else if (stateTwo == ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM) {
				collapsePct = Math.max(0f, (percentage - 0.5f) * 2f);
			}
			collapseRowsBy(collapsePct);
		}

		@Override
		public void onStateTransitionEnd(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo) {
		}

		@Override
		public void onStateFinalized(ResultsHotelsListState state) {
			mState = state;
			collapseRowsPerState();
		}
	};

	@Override
	public void updateListExpandedState(float percentage, boolean actionComplete) {
		super.updateListExpandedState(percentage, actionComplete);
		collapseRowsBy(percentage);
	}

	private void collapseRowsPerState() {
		if (mState == ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM) {
			collapseRowsBy(1f);
		}
		else if (mState == ResultsHotelsListState.HOTELS_LIST_AT_TOP) {
			collapseRowsBy(0f);
		}
	}

	private float mCollapsedBy = -1f;

	private void collapseRowsBy(float percentage) {
		if (mCollapsedBy == percentage) {
			return;
		}
		mCollapsedBy = percentage;

		mAdapter.collapseNewViewsBy(percentage);

		FruitList listView = getListView();
		int adapterPosition = listView.getFirstVisiblePosition();
		for (int listIndex = 0; listIndex < listView.getChildCount() && adapterPosition < mAdapter.getCount();
			listIndex++) {
			View child = listView.getChildAt(listIndex);
			if (!(child instanceof HotelSummarySection)) {
				continue;
			}
			HotelSummarySection hss = (HotelSummarySection) child;
			hss.collapseBy(percentage * mAdapter.estimateExpandableHeight(adapterPosition));
			hss.setTranslationY(-mAdapter.estimateExpandableOffset(adapterPosition));
			adapterPosition++;
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (getActivity() != null) {
			boolean shouldShowVipIcon = PointOfSale.getPointOfSale().supportsVipAccess()
				&& User.getLoggedInLoyaltyMembershipTier(getActivity()).isMidOrTopTier();
			mAdapter.setShowVipIcon(shouldShowVipIcon);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		registerStateListener(mExpandyListener, true);

		// TODO: the state manager may handle this in the future
		collapseRowsPerState();

		Db.getFilter().addOnFilterChangedListener(this);
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		unRegisterStateListener(mExpandyListener);
		Db.getFilter().removeOnFilterChangedListener(this);
		Events.unregister(this);
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
		if (itemPosition >= 0 && itemPosition < mAdapter.getCount()) {
			mAdapter.setSelectedPosition(itemPosition);
			mAdapter.notifyDataSetChanged();
			Property property = (Property) mAdapter.getItem(itemPosition);
			Db.getHotelSearch().setSelectedProperty(property);
			mHotelSelectedListener.onHotelSelected();
			if (property.isSponsored()) {
				AdImpressionTracking.trackAdClickOrImpression(getActivity(), property.getClickTrackingUrl(), null);
				OmnitureTracking.trackHotelSponsoredListingClick();
			}
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
			if (search != null && search.getSearchResponse() != null) {
				search.getSearchResponse().setFilter(Db.getFilter());
			}
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
	protected boolean initializeTopRightTextButtonEnabled() {
		return true;
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

	@Subscribe
	public void onHotelAvailabilityUpdated(Events.HotelAvailabilityUpdated event) {
		if (hasList()) {
			getListView().invalidate();
		}
	}

	public void clearSelectedProperty() {
		mAdapter.setSelectedPosition(-1);
		mAdapter.notifyDataSetChanged();
	}
}
