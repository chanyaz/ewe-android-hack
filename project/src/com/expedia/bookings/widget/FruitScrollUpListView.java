package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.mobiata.android.Log;

/**
 * FruitScrollUpListView: A view designed for tablet results 2013
 * 
 * This listview uses a transparent header view to act as empty space above the list.
 * As we scroll the list, the portion of the header visible is reported to listeners.
 * 
 * It should be able to act as a pretty normal listview with the only caveat being that
 * at the time of this writing (9/1/13) it is dependent on constant size listview rows.
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class FruitScrollUpListView extends ListView implements OnScrollListener {

	private static final String STATE_DEFAULT_SAVESTATE = "STATE_DEFAULT_SAVESTATE";
	private static final String STATE_STATE = "STATE_STATE";
	private static final String STATE_SHRUNK = "STATE_SHRUNK";
	private static final String STATE_SHRUNK_LOCKED = "STATE_SHRUNK_LOCKED";

	public interface IFruitScrollUpListViewInitListener {
		public void onInitStatusChanged(boolean initialized, State state, float percentage);
	}

	public interface IFruitScrollUpListViewChangeListener {

		public void onStateChanged(State oldState, State newState, float percentage);

		public void onPercentageChanged(State state, float percentage);

	}

	public enum State {
		LIST_CONTENT_AT_TOP, LIST_CONTENT_AT_BOTTOM, TRANSIENT
	}

	//This thing is sort of a switch, and this indicates its status
	private State mState = State.LIST_CONTENT_AT_BOTTOM;
	private State mPrevState = State.LIST_CONTENT_AT_BOTTOM;

	//Settings
	private float mPercentageOfHeightUsedForHeaderSpacer = 0.5f;
	private float mPercentageOfHeaderSpacerOnScreenForSnap = 0.5f;

	//The spacers
	private BlockEventFrameLayout mHeaderSpacer;
	private BlockEventFrameLayout mFooterSpacer;

	//Heights
	private int mHeight = -1;
	private int mHeaderSpacerHeight = -1;
	private int mFooterSpacerHeight = -1;

	//State
	private boolean mHeaderSpacerShrunk = false;
	private boolean mHeaderSpacerShrinkLocked = false;
	private boolean mAdapterSet = false;
	private boolean mStateRestored = false;
	private boolean mInitialized = false;
	private boolean mIsBeingTouched = false;
	private boolean mIsBeingScrolled = false;
	private boolean mIsSmoothScrolling = false;
	private int mPreviousScrollState = OnScrollListener.SCROLL_STATE_IDLE;
	private float mPreviouslyReportedPercentage = 1f;
	private boolean mListenersEnabled = true;

	//Listeners
	ArrayList<IFruitScrollUpListViewInitListener> mInitListeners = new ArrayList<IFruitScrollUpListViewInitListener>();
	ArrayList<IFruitScrollUpListViewChangeListener> mChangeListeners = new ArrayList<IFruitScrollUpListViewChangeListener>();

	public FruitScrollUpListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	/*
	 * OVERRIDES
	 */

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		sizeOrDataChanged();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		getViewTreeObserver().removeOnPreDrawListener(mInitPreDrawListener);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (h != mHeight) {
			mHeight = h;
			sizeOrDataChanged();
		}
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (mStateRestored) {
			throw new RuntimeException("IT IS A BUG TO CALL setAdapter AFTER onRestoreInstanceState");
		}
		super.setAdapter(adapter);
		adapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				sizeOrDataChanged();
			}
		});
		mAdapterSet = true;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle state = new Bundle();

		//super state.
		state.putParcelable(STATE_DEFAULT_SAVESTATE, super.onSaveInstanceState());

		//our state
		State saveState = mState;
		if (saveState == State.TRANSIENT) {
			saveState = mPrevState;
		}
		float percentage = getScrollDownPercentage();
		if (saveState == State.TRANSIENT) {
			if (percentage > mPercentageOfHeaderSpacerOnScreenForSnap) {
				saveState = State.LIST_CONTENT_AT_BOTTOM;
			}
			else {
				saveState = State.LIST_CONTENT_AT_TOP;
			}
		}
		Log.d("FruitScrollUpListView.onSaveInstanceState() - state:" + saveState.name() + " percentage:" + percentage);
		state.putString(STATE_STATE, saveState.name());
		if (saveState == State.LIST_CONTENT_AT_TOP) {
			//If we are at the top, and the header is shrunk, we want to keep it that way.
			state.putBoolean(STATE_SHRUNK, mHeaderSpacerShrunk);
			state.putBoolean(STATE_SHRUNK_LOCKED, mHeaderSpacerShrinkLocked);
		}

		return state;
	}

	@Override
	public void onRestoreInstanceState(Parcelable savedInstanceState) {
		if (!mAdapterSet) {
			throw new RuntimeException("IT IS A BUG TO CALL onRestoreInstanceState BEFORE setAdapter");
		}
		if (savedInstanceState instanceof Bundle) {
			Bundle stateBundle = (Bundle) savedInstanceState;

			super.onRestoreInstanceState(stateBundle.getParcelable(STATE_DEFAULT_SAVESTATE));

			//We set the state here, and wait for setState to be called by the initialization methods
			mState = State.valueOf(stateBundle.getString(STATE_STATE, State.LIST_CONTENT_AT_BOTTOM.name()));

			//It is important that we preserve header shrunkenness, otherwise we will appear to allow really odd overscroll.
			mHeaderSpacerShrunk = stateBundle.getBoolean(STATE_SHRUNK, mHeaderSpacerShrunk);
			mHeaderSpacerShrinkLocked = mHeaderSpacerShrunk
					&& stateBundle.getBoolean(STATE_SHRUNK_LOCKED, mHeaderSpacerShrinkLocked);

			sizeOrDataChanged();
		}
		else {
			super.onRestoreInstanceState(savedInstanceState);
		}
		mStateRestored = true;
	}

	/*
	 * INITIALIZATION
	 */

	private void init(Context context) {
		//This gets altered based on state...
		setOverScrollMode(OVER_SCROLL_NEVER);

		initSpacers(context);

		initializeSizes();
	}

	private void sizeOrDataChanged() {
		if (mInitialized) {
			mInitialized = false;
			reportInitStatusChanged(mInitialized);
			initializeSizes();
		}
	}

	private void initializeSizes() {
		if (!mInitialized) {
			getViewTreeObserver().addOnPreDrawListener(mInitPreDrawListener);
		}
	}

	//This does our initializing, waiting for an adapter and measure to be called.
	private OnPreDrawListener mInitPreDrawListener = new OnPreDrawListener() {
		@Override
		public boolean onPreDraw() {
			if (mInitialized) {
				getViewTreeObserver().removeOnPreDrawListener(this);
				setOnScrollListener(FruitScrollUpListView.this);
				reportInitStatusChanged(true);
			}
			else if (mHeight >= 0 && isShown()) {
				if (mAdapterSet && initializeHeaderSpacerHeight() && initializeFooterSpacerHeight()) {
					if (mHeaderSpacerShrunk) {
						shrinkHeaderSpacer();
					}
					else {
						setHeaderSpacerLayoutHeight(getHeaderSpacerHeight());
					}

					setFooterSpacerLayoutHeight(getFooterSpacerHeight());

					//If we dont post this, we dont move to the correct position.
					post(new Runnable() {
						@Override
						public void run() {
							mInitialized = true;
							setState(mState, true);
						}
					});
				}
			}
			return true;
		}
	};

	private void initSpacers(Context context) {
		//This is the spacer view that we set as a header
		mHeaderSpacer = new BlockEventFrameLayout(context);
		mHeaderSpacer.setEnabled(false);
		AbsListView.LayoutParams spacerParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 0);
		mHeaderSpacer.setLayoutParams(spacerParams);
		addHeaderView(mHeaderSpacer);

		//This is a footer view, so that we can still scroll up, even if we dont have enough rows to fill the listview
		mFooterSpacer = new BlockEventFrameLayout(context);
		mFooterSpacer.setEnabled(false);
		mFooterSpacer.setBlockNewEventsEnabled(true);
		mFooterSpacer.setBackgroundColor(context.getResources().getColor(R.color.tablet_white_pane_bg));
		AbsListView.LayoutParams footerParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 0);
		mFooterSpacer.setLayoutParams(footerParams);
		addFooterView(mFooterSpacer);
	}

	/*
	 * GETTERS
	 */
	public State getState() {
		return mState;
	}

	public boolean isInitialized() {
		return mInitialized;
	}

	/*
	 * SETTERS
	 */

	public void setListenersEnabled(boolean enabled) {
		mListenersEnabled = enabled;
	}

	/*
	 * METHODS FOR MOVING AROUND THE LIST
	 */

	public void setState(State state, boolean forceUpdate) {
		setState(state, forceUpdate, 0);
	}

	public void setState(State state, boolean forceUpdate, int duration) {
		Log.d("FruitScrollUpListView.setState state:" + state.name() + " prevState:" + mState + " forceUpdate:"
				+ forceUpdate + " duration:" + duration);

		if (!isInitialized()) {
			//If we aren't yet initialized, we wait for the init listener to fire setState.
			mState = state;
			return;
		}

		if (forceUpdate || mState != state) {
			mPrevState = mState;
			mState = state;

			if (duration == 0 || !stateChangeRequiresMove(state)) {
				//If we are smooth scrolling, this method will be called again when the
				//list reaches its position, so calling when duration == 0 makes sense,
				//but if we arent going to actually scroll, we should also report because otherwise
				//we can get stuck in a strange state....
				reportStateChanged(mPrevState, state);
			}

			//If we just moved to bottom state, we must move our header to the top.
			if (state == State.LIST_CONTENT_AT_BOTTOM) {
				setListLockedToTop(false);
				unShrinkHeaderSpacer();
				moveToPosition(0, 0, duration);
			}
			else if (state == State.LIST_CONTENT_AT_TOP && getFirstVisiblePosition() == 0) {
				moveToPosition(1, 0, duration);
			}
		}
	}

	public Pair<Integer, Integer> moveToPosition(final int position, final int y, final int duration) {
		Pair<Integer, Integer> sanePosition = sanatizePosition(position, y);
		boolean requiresMove = requiresMove(sanePosition);
		Log.d("FruitScrollUpListView.moveToPosition position:" + position + " y:" + y + " duration:" + duration
				+ " sanePosition.first" + sanePosition.first + " sanePosition.second:" + sanePosition.second
				+ " requiresMove:" + requiresMove);
		if (duration > 0 && requiresMove) {
			mIsSmoothScrolling = true;
			someUserInteractionHasStarted();

			if (position == 0 && getFirstVisiblePosition() > 1) {
				// If we are trying to smooth scroll to the bottom position, but are too far down the list,
				// we wont get nice animations, and that is sad. so we jump to the top and then begin smooth
				// scrolling afterwards.
				setSelectionFromTop(1, 0);
				post(new Runnable() {
					@Override
					public void run() {
						moveToPosition(position, y, duration);
					}
				});
			}
			else {
				//Typically we just want to smooth scroll
				smoothScrollToPositionFromTop(sanePosition.first, sanePosition.second, duration);
			}
		}
		else if (requiresMove) {
			setSelectionFromTop(sanePosition.first, sanePosition.second);
		}
		return sanePosition;
	}

	private boolean stateChangeRequiresMove(State state) {
		if (state == State.LIST_CONTENT_AT_BOTTOM) {
			return requiresMove(new Pair<Integer, Integer>(0, 0));
		}
		else if (state == State.LIST_CONTENT_AT_TOP && getFirstVisiblePosition() == 0) {
			return requiresMove(new Pair<Integer, Integer>(1, 0));
		}
		return false;
	}

	private boolean requiresMove(Pair<Integer, Integer> posAndOffset) {
		if (getFirstVisiblePosition() == posAndOffset.first) {
			if (getChildAt(0).getTop() == posAndOffset.second) {
				return false;
			}
		}
		return true;
	}

	private Pair<Integer, Integer> sanatizePosition(int position, int y) {
		int retPos = position;
		int retTop = y;

		if (mState == State.LIST_CONTENT_AT_TOP && position == 0) {
			//Setting position will not change state, so we may not go to the 0th position in this state.
			retPos = 1;
			retTop = 0;
		}
		else if (position >= getCount() - getFooterViewsCount()) {
			if (getRowCount() > 0) {
				//If position is past our last real row, we assume we just want to go to the last row.
				retPos = getCount() - getFooterViewsCount() - 1;
				retTop = 0;
			}
			else {
				//We have no data rows, so we just go to whatever is after the headers.
				retPos = getHeaderViewsCount();
				retTop = 0;
			}
		}
		else if (y > 0) {
			//We need to make sure we aren't going to set the top y in such a way that the first row isn't exposing the header spacer
			int rowDistanceFromFirstRealRow = getRowDistanceFromFirstRowTop(position);
			if (y > rowDistanceFromFirstRealRow) {
				retTop = rowDistanceFromFirstRealRow;
			}
		}
		return new Pair<Integer, Integer>(retPos, retTop);
	}

	/*
	 * INIT LISTENER METHODS
	 */

	public void addInitializationListener(IFruitScrollUpListViewInitListener listener, boolean fireListener) {
		mInitListeners.add(listener);
		if (fireListener) {
			reportInitStatusChangedToListener(listener, mInitialized);
		}
	}

	public void removeInitializationListener(IFruitScrollUpListViewInitListener listener) {
		mInitListeners.remove(listener);
	}

	public void clearInitializtionListeners() {
		mInitListeners.clear();
	}

	private void reportInitStatusChanged(boolean initialized) {
		for (IFruitScrollUpListViewInitListener listener : mInitListeners) {
			reportInitStatusChangedToListener(listener, initialized);
		}
	}

	private void reportInitStatusChangedToListener(IFruitScrollUpListViewInitListener listener, boolean initialized) {
		listener.onInitStatusChanged(initialized, mState, getScrollDownPercentage());
	}

	/*
	 * CHANGE LISTENER METHODS
	 */

	public void addChangeListener(IFruitScrollUpListViewChangeListener listener, boolean fireListener) {
		mChangeListeners.add(listener);
		if (fireListener) {
			reportPercentageChangedToListener(listener, getScrollDownPercentage());
			reportStateChangedToListener(listener, mState, mState);
		}
	}

	public void removeChangeListener(IFruitScrollUpListViewChangeListener listener) {
		mChangeListeners.remove(listener);
	}

	public void clearChangeListeners() {
		mChangeListeners.clear();
	}

	private void reportStateChanged(State oldState, State newState) {
		for (IFruitScrollUpListViewChangeListener listener : mChangeListeners) {
			reportStateChangedToListener(listener, oldState, newState);
		}
	}

	private void reportStateChangedToListener(IFruitScrollUpListViewChangeListener listener, State oldState,
			State newState) {
		if (changeListenersEnabled()) {
			listener.onStateChanged(oldState, newState, getScrollDownPercentage());
		}
	}

	private void reportPercentageChanged(float percentage) {
		for (IFruitScrollUpListViewChangeListener listener : mChangeListeners) {
			reportPercentageChangedToListener(listener, percentage);
		}
	}

	private void reportPercentageChangedToListener(IFruitScrollUpListViewChangeListener listener, float percentage) {
		if (changeListenersEnabled()) {
			listener.onPercentageChanged(mState, percentage);
		}
	}

	private boolean changeListenersEnabled() {
		return mInitialized && mListenersEnabled && isShown();
	}

	/*
	 * PERCENTAGE METHODS
	 */

	public float getScrollDownPercentage() {
		if (mState == State.TRANSIENT) {
			return calculateScrollDownPercentage();
		}
		else if (mState == State.LIST_CONTENT_AT_BOTTOM) {
			return 1f;
		}
		else if (mState == State.LIST_CONTENT_AT_TOP) {
			return 0f;
		}
		throw new RuntimeException("mState appears to not be set. Fail.");
	}

	private float calculateScrollDownPercentage() {
		if (mHeaderSpacer.isShown()) {
			float retVal = (float) calculateHeaderSpacerVisibleHeight() / getHeaderSpacerHeight();
			return retVal;
		}
		else {
			return 0f;
		}
	}

	private void updateOverscrollMode(float percentage) {
		//We dont want to overscroll when the headerview is showing because, it will
		//appear to overscroll above the clear header and look stupid.

		if (mPreviouslyReportedPercentage < 1f && percentage == 1f) {
			//hit bottom
			setOverScrollMode(OVER_SCROLL_NEVER);
		}
		if (mPreviouslyReportedPercentage == 1f && percentage < 1f) {
			//leave bottom
			setOverScrollMode(OVER_SCROLL_ALWAYS);
		}
		if (mPreviouslyReportedPercentage > 0f && percentage == 0f) {
			//hit top
			setOverScrollMode(OVER_SCROLL_ALWAYS);
		}
		if (mPreviouslyReportedPercentage == 0f && percentage > 0f) {
			//leave top
			setOverScrollMode(OVER_SCROLL_NEVER);
		}
	}

	/*
	 * HEADER SPACER METHODS
	 */

	public void setListLockedToTop(boolean lockedToTop) {
		mHeaderSpacerShrinkLocked = lockedToTop;
		if (lockedToTop && !mHeaderSpacerShrunk) {
			shrinkHeaderSpacer();
		}
		else if (!lockedToTop && mHeaderSpacerShrunk && mState != State.TRANSIENT) {
			unShrinkHeaderSpacer();
		}
	}

	public int getHeaderSpacerHeight() {
		return mHeaderSpacerHeight;
	}

	public int calculateHeaderSpacerVisibleHeight() {
		if (!mHeaderSpacerShrunk && getChildAt(0) == mHeaderSpacer) {
			//Because mHeaderSpacer is always the first item in the list, mHeaderSpacer.getTop()
			//can only ever be 0 or negative.
			return Math.max(0, getHeaderSpacerHeight() + mHeaderSpacer.getTop());
		}
		return 0;
	}

	private boolean initializeHeaderSpacerHeight() {
		mHeaderSpacerHeight = (int) (mHeight * mPercentageOfHeightUsedForHeaderSpacer);
		return mHeaderSpacerHeight >= 0;
	}

	private void setHeaderSpacerLayoutHeight(int height) {
		mHeaderSpacer.getLayoutParams().height = height;
		mHeaderSpacer.setLayoutParams(mHeaderSpacer.getLayoutParams());
	}

	private void shrinkHeaderSpacer() {
		setHeaderSpacerLayoutHeight(0);
		mHeaderSpacerShrunk = true;
	}

	private void unShrinkHeaderSpacer() {
		if (!mHeaderSpacerShrinkLocked) {
			setHeaderSpacerLayoutHeight(getHeaderSpacerHeight());
			mHeaderSpacerShrunk = false;
		}
	}

	/*
	 * FOOTER SPACER METHODS
	 */

	public int getFooterSpacerHeight() {
		return mFooterSpacerHeight;
	}

	private boolean initializeFooterSpacerHeight() {
		int footerHeight = -1;
		int rowsCount = getRowCount();
		int singleRowHeight = getRowHeight(false);
		if (rowsCount == 0) {
			//Need a full space footer so we can move the header all the way off screen.
			footerHeight = mHeight;
		}
		else if (singleRowHeight < 0) {
			//couldn't get a rows height...
			footerHeight = -1;
		}
		else {
			//Calculate the footer based on the height of all rows and dividers along with the header height
			int allRowsHeight = (rowsCount * (singleRowHeight + getDividerHeight())) + getDividerHeight();
			footerHeight = Math.max(mHeight - allRowsHeight, 0);
		}

		mFooterSpacerHeight = footerHeight;
		return mFooterSpacerHeight >= 0;
	}

	private void setFooterSpacerLayoutHeight(int height) {
		mFooterSpacer.getLayoutParams().height = height;
		mFooterSpacer.setLayoutParams(mFooterSpacer.getLayoutParams());
	}

	/*
	 * ROW METHODS
	 */

	public int getRowCount() {
		return getCount() - getFooterViewsCount() - getHeaderViewsCount();
	}

	public int getRowHeight(boolean withDividerHeight) {
		View row = getOneRow();
		if (row != null) {
			int retHeight = row.getHeight();
			retHeight += withDividerHeight ? getDividerHeight() : 0;
			return retHeight;
		}
		return -1;
	}

	private View getOneRow() {
		View row = null;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child != mHeaderSpacer && child != mFooterSpacer) {
				row = child;
				break;
			}
		}
		return row;
	}

	private int getRowDistanceFromTop(int position) {
		if (position == 0) {
			return 0;
		}
		else if (position == getHeaderViewsCount()) {
			return getHeaderSpacerHeight();
		}
		else {
			int numRowsBefore = position - getHeaderViewsCount();
			return getHeaderSpacerHeight() + (numRowsBefore * getRowHeight(true));
		}
	}

	private int getRowDistanceFromFirstRowTop(int position) {
		int fromTop = getRowDistanceFromTop(position);
		if (fromTop > 0) {
			return fromTop - getHeaderSpacerHeight();
		}
		else {
			return fromTop;
		}
	}

	/*
	 * TOUCH AND SCROLL HANDLERS
	 */

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		if (mInitialized && !mIsSmoothScrolling) {
			//We want to keep track of when the user is touching this list
			if (me.getAction() == MotionEvent.ACTION_DOWN) {
				updateTouchState(true);
			}
			else if (me.getAction() == MotionEvent.ACTION_UP || me.getAction() == MotionEvent.ACTION_CANCEL) {
				updateTouchState(false);
			}

			return super.onTouchEvent(me);
		}
		return true;
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		updateScrollPercentage(getScrollDownPercentage());
	}

	@Override
	public void onScrollStateChanged(AbsListView listView, int scrollState) {
		boolean isIdle = scrollState == OnScrollListener.SCROLL_STATE_IDLE;
		if (mIsSmoothScrolling) {
			mIsSmoothScrolling = !isIdle;
		}
		else {
			boolean isFlinging = scrollState == OnScrollListener.SCROLL_STATE_FLING;
			boolean wasFlinging = mPreviousScrollState == OnScrollListener.SCROLL_STATE_FLING;

			if (isFlinging & !wasFlinging && getFirstVisiblePosition() > 0) {
				//We started flinging while the header spacer was off screen. We don't want people to fling to
				//a different mode un-intentionally, so we hide the header which means flinging will stop at the top data row
				shrinkHeaderSpacer();
			}
		}
		updateScrollState(!isIdle);
		mPreviousScrollState = scrollState;
	}

	private void updateTouchState(boolean isBeingTouched) {
		if (mIsBeingTouched != isBeingTouched) {
			mIsBeingTouched = isBeingTouched;
			if (mIsBeingTouched) {
				someUserInteractionHasStarted();
			}
			else {
				someUserInteractionHasStopped();
			}
		}
	}

	private void updateScrollState(boolean isBeingScrolled) {
		if (mIsBeingScrolled != isBeingScrolled) {
			mIsBeingScrolled = isBeingScrolled;
			if (mIsBeingScrolled) {
				someUserInteractionHasStarted();
			}
			else {
				someUserInteractionHasStopped();
			}
		}
	}

	private void updateScrollPercentage(float percentage) {
		if (percentage != mPreviouslyReportedPercentage) {
			reportPercentageChanged(percentage);
			updateOverscrollMode(percentage);
		}
		mPreviouslyReportedPercentage = percentage;
	}

	private void someUserInteractionHasStarted() {
		setState(State.TRANSIENT, true);
	}

	private void someUserInteractionHasStopped() {
		if (mState == State.TRANSIENT && !mIsBeingScrolled && !mIsBeingTouched) {
			float percentage = getScrollDownPercentage();
			if (percentage > mPercentageOfHeaderSpacerOnScreenForSnap) {
				setState(State.LIST_CONTENT_AT_BOTTOM, true);
			}
			else {
				setState(State.LIST_CONTENT_AT_TOP, true);
			}

			if (mHeaderSpacerShrunk) {
				unShrinkHeaderSpacer();
			}
		}
	}
}
