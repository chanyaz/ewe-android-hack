package com.expedia.bookings.fragment.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.widget.FruitScrollUpListView;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewInitListener;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.mobiata.android.util.Ui;

/**
 * ResultsListFragment: The abstract base Fragment  for the flight and hotel lists designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class ResultsListFragment extends ListFragment implements IFruitScrollUpListViewChangeListener {

	private static final String STATE_LIST_STATE = "STATE_LIST_STATE";

	private static final int SMOOTH_SCROLL_DURATION = 120;

	protected FruitScrollUpListView mListView;
	private ViewGroup mStickyHeader;
	private TextView mStickyHeaderTv;
	private TextView mTopRightTextButton;

	private CharSequence mStickyHeaderText = "";
	private CharSequence mTopRightTextButtonText = "";

	private boolean mTopRightButtonEnabled = true;
	private boolean mLockedToTop = false;
	private boolean mGotoBottom = false;
	private boolean mGotoTop = false;
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
		mTopRightTextButton = Ui.findView(view, R.id.top_right_text_button);

		mStickyHeaderTv.setText(mStickyHeaderText);
		mTopRightTextButton.setText(mTopRightTextButtonText);

		//Note: We must set the adapter before we restore instance state
		mListView.setAdapter(initializeAdapter());

		setTopRightTextButtonEnabled(initializeTopRightTextButtonEnabled());

		mTopRightTextButton.setOnClickListener(initializeTopRightTextButtonOnClickListener());

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

		if (mGotoTop) {
			gotoTopPosition(0);
		}
		mListView.setListLockedToTop(mLockedToTop);
		if (mGotoBottom) {
			gotoBottomPosition(0);
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

	public void setTopRightTextButtonEnabled(boolean enabled) {
		mTopRightButtonEnabled = enabled;
		if (mTopRightTextButton != null) {
			if (enabled) {
				mTopRightTextButton.setVisibility(View.VISIBLE);
			}
			else {
				mTopRightTextButton.setVisibility(View.GONE);
			}
		}
	}

	public void setTopRightTextButtonOnClick(OnClickListener clicky) {
		mTopRightTextButton.setOnClickListener(clicky);
	}

	public void setStickyHeaderText(CharSequence text) {
		mStickyHeaderText = text;
		if (mStickyHeaderTv != null) {
			mStickyHeaderTv.setText(text);
		}
	}

	public void setTopRightTextButtonText(CharSequence text) {
		mTopRightTextButtonText = text;
		if (mTopRightTextButton != null) {
			mTopRightTextButton.setText(text);
		}
	}

	public void setListLockedToTop(boolean lockedToTop) {
		mLockedToTop = lockedToTop;
		if (mListView != null) {
			mListView.setListLockedToTop(lockedToTop);
		}
	}

	public void gotoTopPosition() {
		gotoTopPosition(SMOOTH_SCROLL_DURATION);
	}

	public void gotoTopPosition(int duration) {
		mGotoTop = true;
		if (mListView != null && mListView.isInitialized()) {
			mListView.setState(State.LIST_CONTENT_AT_TOP, true, duration);
			mGotoTop = false;
		}
	}

	public void gotoBottomPosition() {
		gotoBottomPosition(SMOOTH_SCROLL_DURATION);
	}

	public void gotoBottomPosition(int duration) {
		mGotoBottom = true;
		if (mListView != null && mListView.isInitialized()) {
			mListView.setState(State.LIST_CONTENT_AT_BOTTOM, true, duration);
			mGotoBottom = false;

			if (duration == 0) {
				updateStickyHeaderState(1f, true);
			}
		}
	}

	private void updateStickyHeaderState(float percentage, boolean actionComplete) {
		//top right button stuff...
		if (mTopRightButtonEnabled) {
			mTopRightTextButton.setAlpha(1f - percentage);
			if (percentage == 1f) {
				mTopRightTextButton.setVisibility(View.INVISIBLE);
			}
			else {
				mTopRightTextButton.setVisibility(View.VISIBLE);
			}
			if (actionComplete) {
				mTopRightTextButton.setEnabled(percentage == 0f);
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

	protected abstract OnClickListener initializeTopRightTextButtonOnClickListener();

	protected abstract boolean initializeTopRightTextButtonEnabled();

}
