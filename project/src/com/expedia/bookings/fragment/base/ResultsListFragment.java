package com.expedia.bookings.fragment.base;

import com.expedia.bookings.R;
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.widget.FruitScrollUpListView;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewInitListener;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * ResultsListFragment: The abstract base Fragment  for the flight and hotel lists designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class ResultsListFragment extends ListFragment implements IFruitScrollUpListViewChangeListener {

	private static final String STATE_LIST_STATE = "STATE_LIST_STATE";

	private FruitScrollUpListView mListView;
	private ViewGroup mStickyHeader;
	private TextView mStickyHeaderTv;
	private TextView mSortAndFilterButton;

	private CharSequence mStickyHeaderText = "";
	private CharSequence mSortAndFilterText = "";

	private boolean mSortAndFilterButtonEnabled = true;
	private boolean mLockedToTop = false;
	private boolean mGotoBottom = false;
	private IFruitScrollUpListViewChangeListener mChangeListener;
	private IBackButtonLockListener mBackLockListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mBackLockListener = Ui.findFragmentListener(this, IBackButtonLockListener.class, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_list, null);
		mListView = Ui.findView(view, android.R.id.list);
		mStickyHeader = Ui.findView(view, R.id.sticky_header_container);
		mStickyHeaderTv = Ui.findView(view, R.id.sticky_number_of_items);
		mSortAndFilterButton = Ui.findView(view, R.id.sort_and_filter);

		mStickyHeaderTv.setText(mStickyHeaderText);
		mSortAndFilterButton.setText(mSortAndFilterText);

		//Note: We must set the adapter before we restore instance state
		mListView.setAdapter(initializeAdapter());

		setSortAndFilterButtonEnabled(initializeSortAndFilterEnabled());

		mSortAndFilterButton.setOnClickListener(initializeSortAndFilterOnClickListener());

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_LIST_STATE)) {
				mListView.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_LIST_STATE));
			}
		}

		mStickyHeader.setVisibility(View.INVISIBLE);
		mListView.addInitializationListener(new IFruitScrollUpListViewInitListener() {

			@Override
			public void onInitStatusChanged(boolean initialized, State state, float percentage) {
				if (initialized) {
					updateStickyHeaderState(percentage, state != State.TRANSIENT);
					mStickyHeader.setVisibility(View.VISIBLE);
				}
			}
		}, true);

		mListView.addChangeListener(this, false);

		if (mChangeListener != null) {
			mListView.addChangeListener(mChangeListener, false);
		}
		mListView.setListLockedToTop(mLockedToTop);
		if (mGotoBottom) {
			gotoBottomPosition();
		}

		setStickyHeaderText(initializeStickyHeaderString());

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mListView != null) {
			outState.putParcelable(STATE_LIST_STATE, mListView.onSaveInstanceState());
		}
	}

	public FruitScrollUpListView getTopSpaceListView() {
		return mListView;
	}

	public void setChangeListener(IFruitScrollUpListViewChangeListener listener) {
		mChangeListener = listener;
		if (mListView != null) {
			mListView.addChangeListener(listener, false);
		}
	}

	public void setSortAndFilterButtonEnabled(boolean enabled) {
		mSortAndFilterButtonEnabled = enabled;
		if (!enabled) {
			mSortAndFilterButton.setVisibility(View.GONE);
		}
	}

	public void setSortAndFilterOnClick(OnClickListener clicky) {
		mSortAndFilterButton.setOnClickListener(clicky);
	}

	public void setStickyHeaderText(CharSequence text) {
		mStickyHeaderText = text;
		if (mStickyHeaderTv != null) {
			mStickyHeaderTv.setText(text);
		}
	}

	public void setSortAndFilterButtonText(CharSequence text) {
		mSortAndFilterText = text;
		if (mSortAndFilterButton != null) {
			mSortAndFilterButton.setText(text);
		}
	}

	public void setListLockedToTop(boolean lockedToTop) {
		mLockedToTop = lockedToTop;
		if (mListView != null) {
			mListView.setListLockedToTop(lockedToTop);
		}
	}

	public void gotoBottomPosition() {
		mGotoBottom = true;
		if (mListView != null) {
			mListView.setState(State.LIST_CONTENT_AT_BOTTOM, true, true);
			mGotoBottom = false;
		}
	}

	private void updateStickyHeaderState(float percentage, boolean actionComplete) {

		//Sort and filter button stuff...
		if (mSortAndFilterButtonEnabled) {
			mSortAndFilterButton.setAlpha(1f - percentage);
			if (percentage == 1f) {
				mSortAndFilterButton.setVisibility(View.INVISIBLE);
			}
			else {
				mSortAndFilterButton.setVisibility(View.VISIBLE);
			}
			if (actionComplete) {
				mSortAndFilterButton.setEnabled(percentage == 0f);
			}
		}

		//position
		if (percentage == 0) {
			mStickyHeader.setTranslationY(0);
		}
		else if (percentage == 1) {
			mStickyHeader.setTranslationY(mListView.getHeaderSpacerHeight());
		}
		else {
			int stickyHeaderBottom = mListView.calculateHeaderSpacerVisibleHeight();
			mStickyHeader.setTranslationY(stickyHeaderBottom);
		}
	}

	@Override
	public void onStateChanged(State oldState, State newState, float percentage) {
		mBackLockListener.setBackButtonLockState(newState == State.TRANSIENT);
		updateStickyHeaderState(percentage, newState != State.TRANSIENT);
	}

	@Override
	public void onPercentageChanged(State state, float percentage) {
		updateStickyHeaderState(percentage, state != State.TRANSIENT);
	}

	/*
	 * ABSTRACT METHODS
	 */

	protected abstract ListAdapter initializeAdapter();

	protected abstract CharSequence initializeStickyHeaderString();

	protected abstract OnClickListener initializeSortAndFilterOnClickListener();

	protected abstract boolean initializeSortAndFilterEnabled();

}
