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
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.ResultsHotelsListState;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.widget.TabletHotelAdapter;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.mobiata.android.util.Ui;

/**
 * ResultsHotelListFragment: The hotel list fragment designed for tablet results 2013
 */
public class ResultsHotelListFragment extends ResultsListFragment implements OnFilterChangedListener,
		IStateProvider<ResultsHotelsListState> {

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

		if (getActivity() != null && mAdapter instanceof TabletHotelAdapter) {
			boolean shouldShowVipIcon = PointOfSale.getPointOfSale().supportsVipAccess()
					&& User.isElitePlus(getActivity());
			((TabletHotelAdapter) mAdapter).setShowVipIcon(shouldShowVipIcon);
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

	/*
	 * Here we convert FruitScrollUpListView states to our ResultsHotelsListState states, and fire the listeners
	 */

	private ResultsHotelsListState mTransStartState;
	private ResultsHotelsListState mTransEndState;
	private boolean mHasStarted = false;

	@Override
	public void onStateChanged(State oldState, State newState, float percentage) {
		super.onStateChanged(oldState, newState, percentage);
		if (oldState == State.LIST_CONTENT_AT_TOP && newState == State.LIST_CONTENT_AT_BOTTOM) {
			finalizeState(ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM);
		}
		else if (oldState == State.LIST_CONTENT_AT_BOTTOM && newState == State.LIST_CONTENT_AT_TOP) {
			finalizeState(ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM);
		}
		else if (oldState == State.LIST_CONTENT_AT_BOTTOM && newState == State.TRANSIENT) {
			mTransStartState = ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM;
			mTransEndState = ResultsHotelsListState.HOTELS_LIST_AT_TOP;
		}
		else if (oldState == State.LIST_CONTENT_AT_TOP && newState == State.TRANSIENT) {
			mTransStartState = ResultsHotelsListState.HOTELS_LIST_AT_TOP;
			mTransEndState = ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM;
		}
		else if (oldState == State.TRANSIENT && newState != State.TRANSIENT) {
			if (mHasStarted) {
				endStateTransition(mTransStartState, mTransEndState);
				if (newState == State.LIST_CONTENT_AT_BOTTOM) {
					finalizeState(ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM);
				}
				else {
					finalizeState(ResultsHotelsListState.HOTELS_LIST_AT_TOP);
				}
			}
			mTransStartState = null;
			mTransEndState = null;
			mHasStarted = false;
		}
	}

	@Override
	public void onPercentageChanged(State state, float percentage) {
		super.onPercentageChanged(state, percentage);
		if (state == State.TRANSIENT && mTransStartState != null && mTransEndState != null) {
			if (percentage > 0 && percentage < 1) {
				if (!mHasStarted) {
					startStateTransition(mTransStartState, mTransEndState);
					mHasStarted = true;
				}
				updateStateTransition(mTransStartState, mTransEndState,
						mTransStartState == ResultsHotelsListState.HOTELS_LIST_AT_TOP ? percentage : 1f - percentage);
			}
		}
	}

	/*
	 *	IStateProvider
	 */

	private StateListenerCollection<ResultsHotelsListState> mResultsStateListeners = new StateListenerCollection<ResultsHotelsListState>(
			ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM);

	@Override
	public void startStateTransition(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo) {
		mResultsStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo, float percentage) {
		mResultsStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo) {
		mResultsStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsHotelsListState state) {
		mResultsStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<ResultsHotelsListState> listener, boolean fireFinalizeState) {
		mResultsStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsHotelsListState> listener) {
		mResultsStateListeners.unRegisterStateListener(listener);
	}
}
