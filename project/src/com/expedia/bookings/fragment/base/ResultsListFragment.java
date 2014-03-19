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
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.enums.ResultsListState;
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.expedia.bookings.widget.FruitList;
import com.mobiata.android.util.Ui;

/**
 * ResultsListFragment: The abstract base Fragment  for the flight and hotel lists designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class ResultsListFragment<T> extends ListFragment implements IStateProvider<T> {

	private static final String STATE_LIST_STATE = "STATE_LIST_STATE";

	private FruitList mListView;
	private String mListViewContentDescription;
	private FrameLayoutTouchController mStickyHeader;
	private TextView mStickyHeaderTv;
	private TextView mTopRightTextButton;

	private CharSequence mStickyHeaderText = "";
	private CharSequence mTopRightTextButtonText = "";

	private boolean mTopRightButtonEnabled = true;
	private boolean mLockedToTop = false;
	private IBackButtonLockListener mBackLockListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mBackLockListener = Ui.findFragmentListener(this, IBackButtonLockListener.class, true);

		if (mStickyHeader != null) {
			mStickyHeader.getViewTreeObserver().addOnPreDrawListener(mHeaderUpdater);
		}
	}

	@Override
	public void onDetach() {
		if (mStickyHeader != null) {
			mStickyHeader.getViewTreeObserver().removeOnPreDrawListener(mHeaderUpdater);
		}
		super.onDetach();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_list, null);
		mListView = Ui.findView(view, android.R.id.list);
		mListView.setContentDescription(mListViewContentDescription);
		mStickyHeader = Ui.findView(view, R.id.sticky_header_container);
		mStickyHeader.setConsumeTouch(false);
		mStickyHeaderTv = Ui.findView(view, R.id.sticky_number_of_items);
		mTopRightTextButton = Ui.findView(view, R.id.top_right_text_button);

		mStickyHeaderTv.setText(mStickyHeaderText);
		mTopRightTextButton.setText(mTopRightTextButtonText);

		//Note: We must set the adapter before we restore instance state
		mListView.setAdapter(initializeAdapter());

		setTopRightTextButtonEnabled(initializeTopRightTextButtonEnabled());

		mStickyHeaderTv.setOnClickListener(initializeStickyLeftOnClickListener());
		mTopRightTextButton.setOnClickListener(initializeTopRightTextButtonOnClickListener());

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_LIST_STATE)) {
				mListView.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_LIST_STATE));
			}
		}

		mStickyHeader.getViewTreeObserver().addOnPreDrawListener(mHeaderUpdater);

		registerStateListener(new StateListenerLogger<T>(), false);
		mListView.setListLockedToTop(mLockedToTop);
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

	@Override
	public void onResume(){
		super.onResume();
		mListView.registerStateListener(mListStateHelper, false);
	}

	@Override
	public void onPause(){
		mListView.unRegisterStateListener(mListStateHelper);
		super.onPause();
	}

	@Override
	public ListView getListView() {
		return mListView;
	}

	private OnPreDrawListener mHeaderUpdater = new OnPreDrawListener() {
		@Override
		public boolean onPreDraw() {
			if (mListView != null) {
				float perc = mListView.getScrollDownPercentage();
				updateStickyHeaderState(perc, false);
				return true;
			}
			else {
				return false;
			}
		}
	};

	public int getMaxDistanceFromTop() {
		return mListView.getHeaderSpacerHeight();
	}

	public float getPercentage() {
		return mListView.getScrollDownPercentage();
	}

	public boolean hasList() {
		return mListView != null;
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

	public void setPercentage(float percentage, int duration) {
		if (mListView != null) {
			mListView.setScrollDownPercentage(percentage, duration);
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
		else {
			float maxHeaderTransY =
				mListView.getTop() + mListView.getMaxDistanceFromTop() + mListView.getPaddingTop() - mStickyHeader
					.getTop() - mStickyHeader.getHeight();
			if (percentage == 1) {
				mStickyHeader.setTranslationY(maxHeaderTransY);
			}
			else {
				mStickyHeader.setTranslationY(maxHeaderTransY * percentage);
			}
		}
	}

	private StateListenerHelper<ResultsListState> mListStateHelper = new StateListenerHelper<ResultsListState>() {

		@Override
		public void onStateTransitionStart(ResultsListState stateOne, ResultsListState stateTwo) {
			startStateTransition(translateState(stateOne), translateState(stateTwo));
		}

		@Override
		public void onStateTransitionUpdate(ResultsListState stateOne, ResultsListState stateTwo, float percentage) {
			updateStateTransition(translateState(stateOne), translateState(stateTwo), percentage);
		}

		@Override
		public void onStateTransitionEnd(ResultsListState stateOne, ResultsListState stateTwo) {
			endStateTransition(translateState(stateOne), translateState(stateTwo));
		}

		@Override
		public void onStateFinalized(ResultsListState state) {
			updateStickyHeaderState(state == ResultsListState.AT_TOP ? 0f : 1f, true);
			finalizeState(translateState(state));
		}
	};

	private StateListenerCollection<T> mListeners = new StateListenerCollection<T>(getDefaultState());

	@Override
	public void startStateTransition(T stateOne, T stateTwo) {
		mListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(T stateOne, T stateTwo,
		float percentage) {
		mListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(T stateOne, T stateTwo) {
		mListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(T state) {
		mListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<T> listener, boolean fireFinalizeState) {
		mListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<T> listener) {
		mListeners.unRegisterStateListener(listener);
	}

	protected void setListViewContentDescription(int contentDescriptionId) {
		mListViewContentDescription = getString(contentDescriptionId);
	}

	/*
	 * ABSTRACT METHODS
	 */

	protected abstract ListAdapter initializeAdapter();

	protected abstract CharSequence initializeStickyHeaderString();

	protected abstract OnClickListener initializeStickyLeftOnClickListener();

	protected abstract OnClickListener initializeTopRightTextButtonOnClickListener();

	protected abstract boolean initializeTopRightTextButtonEnabled();

	protected abstract T translateState(ResultsListState state);

	protected abstract T getDefaultState();

}
