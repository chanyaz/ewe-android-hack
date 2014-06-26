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
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.expedia.bookings.widget.FruitList;
import com.expedia.bookings.widget.TextView;
import com.larvalabs.svgandroid.widget.SVGView;
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
	private TextView mEmptyListTextView;
	private SVGView mEmptyListImageView;

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

		mEmptyListTextView = Ui.findView(view, R.id.missing_search_result_text);
		mEmptyListImageView = Ui.findView(view, R.id.missing_search_result_image);

		mEmptyListTextView.setText(getEmptyListText());
		mEmptyListImageView.setSVG(getEmptyListImageResource());

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

		setTopSpacePixels(mTopSpacePixels);

		return view;
	}

	private int mTopSpacePixels = 0;

	/**
	 * Updates the top space on this FruitList (taking into account the column header height).
	 * @param pixels
	 */
	public void setTopSpacePixels(int pixels) {
		mTopSpacePixels = pixels;
		if (mListView != null) {
			int topMargin = ((ViewGroup.MarginLayoutParams)mListView.getLayoutParams()).topMargin;
			int dividerHeight = getResources().getDimensionPixelSize(R.dimen.results_list_spacer_height);
			mListView.setTopSpacePixels(mTopSpacePixels - topMargin - dividerHeight);
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
		if(readyForListeners != null){
			readyForListeners.acceptingListenersUpdated(this, true);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		IAcceptingListenersListener readyForListeners = Ui.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if(readyForListeners != null){
			readyForListeners.acceptingListenersUpdated(this, false);
		}
		mListView.unRegisterStateListener(mListStateHelper);

	}

	@Override
	public FruitList getListView() {
		return mListView;
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
		if(hasList()) {
			return mListView.getScrollDownPercentage();
		}
		return 0f;
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

	public void updateListExpandedState(float percentage, boolean actionComplete) {
		//top right button stuff...
		if (mTopRightButtonEnabled) {
			mTopRightTextButton.setAlpha(1f - percentage);
			if (percentage == 1f) {
				mTopRightTextButton.setVisibility(View.INVISIBLE);
				mTopRightTextButton.setEnabled(false);
			}
			else {
				mTopRightTextButton.setVisibility(View.VISIBLE);
				if(percentage == 0) {
					mTopRightTextButton.setEnabled(true);
				}
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
			updateListExpandedState(state == ResultsListState.AT_TOP ? 0f : 1f, true);
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

	protected abstract ListAdapter initializeAdapter();

	protected abstract CharSequence initializeStickyHeaderString();

	protected abstract OnClickListener initializeStickyLeftOnClickListener();

	protected abstract OnClickListener initializeTopRightTextButtonOnClickListener();

	protected abstract boolean initializeTopRightTextButtonEnabled();

	protected abstract T translateState(ResultsListState state);

	protected abstract T getDefaultState();

	protected abstract String getEmptyListText();

	protected abstract int getEmptyListImageResource();

}
