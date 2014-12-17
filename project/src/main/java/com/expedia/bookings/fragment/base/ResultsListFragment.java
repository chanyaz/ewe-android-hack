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

import com.expedia.bookings.R;
import com.expedia.bookings.enums.ResultsListState;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.widget.FruitList;
import com.expedia.bookings.widget.TextView;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.mobiata.android.util.Ui;

/**
 * ResultsListFragment: The abstract base Fragment  for the flight and hotel lists designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class ResultsListFragment<T> extends ListFragment implements IStateProvider<T> {

	private static final String STATE_LIST_STATE = "STATE_LIST_STATE";

	private View mRootC;
	private FruitList mListView;
	private String mListViewContentDescription;
	private TouchableFrameLayout mStickyHeader;
	private TextView mStickyHeaderTv;
	private TextView mTopRightTextButton;

	private CharSequence mStickyHeaderText = "";
	private CharSequence mTopRightTextButtonText = "";

	private boolean mIsTopRightButtonVisible = true;
	private boolean mLockedToTop = false;
	private int mTopSpacePixels = 0;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

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
		mRootC = inflater.inflate(getLayoutResId(), null);
		mListView = Ui.findView(mRootC, android.R.id.list);
		mListView.setContentDescription(mListViewContentDescription);
		mStickyHeader = Ui.findView(mRootC, R.id.sticky_header_container);
		mStickyHeader.setConsumeTouch(true);
		mStickyHeaderTv = Ui.findView(mRootC, R.id.sticky_number_of_items);
		mTopRightTextButton = Ui.findView(mRootC, R.id.top_right_text_button);

		mStickyHeaderTv.setText(mStickyHeaderText);
		mTopRightTextButton.setText(mTopRightTextButtonText);

		//Note: We must set the adapter before we restore instance state
		mListView.setAdapter(initializeAdapter());

		setTopRightTextButtonVisibility(initializeTopRightTextButtonEnabled());

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

		setTopSpacePixels(mTopSpacePixels);

		return mRootC;
	}

	/**
	 * Updates the top space on this fragment's FruitList.
	 *
	 * @param pixels
	 */
	public void setTopSpacePixels(int pixels) {
		mTopSpacePixels = pixels;
		if (mListView != null) {
			mListView.setTopSpacePixels(mTopSpacePixels);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mListView != null) {
			outState.putParcelable(STATE_LIST_STATE, mListView.onSaveInstanceState());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mListView.registerStateListener(mListStateHelper, false);
		IAcceptingListenersListener readyForListeners = Ui.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, true);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		IAcceptingListenersListener readyForListeners = Ui.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, false);
		}
		mListView.unRegisterStateListener(mListStateHelper);

	}

	@Override
	public FruitList getListView() {
		return mListView;
	}

	protected ViewGroup getStickyHeader() {
		return mStickyHeader;
	}

	private OnPreDrawListener mHeaderUpdater = new OnPreDrawListener() {
		@Override
		public boolean onPreDraw() {
			if (mListView != null) {
				float perc = mListView.getScrollDownPercentage();
				updateListExpandedState(perc, false);
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
		if (hasList()) {
			return mListView.getScrollDownPercentage();
		}
		return 0f;
	}

	public boolean hasList() {
		return mListView != null;
	}

	public void setTopRightTextButtonVisibility(boolean isVisible) {
		mIsTopRightButtonVisible = isVisible;
		if (mTopRightTextButton != null) {
			mTopRightTextButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
		}
	}

	public void setTopRightTextButtonEnabled(boolean isEnabled) {
		if (mTopRightTextButton != null) {
			mTopRightTextButton.setEnabled(isEnabled);
		}
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

	public void updateListExpandedState(float percentage, boolean actionComplete) {
		//top right button stuff...
		if (mIsTopRightButtonVisible) {
			mTopRightTextButton.setAlpha(1f - percentage);
			if (percentage == 1f) {
				mTopRightTextButton.setVisibility(View.INVISIBLE);
			}
			else {
				mTopRightTextButton.setVisibility(View.VISIBLE);
			}
		}

		//position
		mStickyHeader.setTranslationY(percentage * getMaxHeaderTranslateY());
	}

	public void setLastReportedTouchPercentage(float percentage) {
		if (hasList()) {
			getListView().setLastReportedPercentage(percentage);
		}
	}

	public void clearSelection() {
		if (hasList()) {
			getListView().clearChoices();
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
			updateListExpandedState(state == ResultsListState.AT_TOP ? 0f : 1f, true);
			finalizeState(translateState(state));
		}
	};

	private StateListenerCollection<T> mListeners = new StateListenerCollection<T>();

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

	public void setListenerEnabled(IStateListener<T> listener, boolean enabled) {
		if (enabled) {
			mListeners.setListenerActive(listener);
		}
		else {
			mListeners.setListenerInactive(listener);
		}
	}

	protected void setListViewContentDescription(int contentDescriptionId) {
		mListViewContentDescription = getString(contentDescriptionId);
	}

	/*
	 * ABSTRACT METHODS
	 */

	public abstract int getLayoutResId();

	public abstract float getMaxHeaderTranslateY();

	protected abstract ListAdapter initializeAdapter();

	protected abstract CharSequence initializeStickyHeaderString();

	protected abstract OnClickListener initializeTopRightTextButtonOnClickListener();

	protected abstract boolean initializeTopRightTextButtonEnabled();

	protected abstract T translateState(ResultsListState state);

	protected abstract T getDefaultState();

}
