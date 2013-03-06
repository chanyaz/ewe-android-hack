package com.expedia.bookings.widget;

import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.expedia.bookings.animation.ResizeAnimator;
import com.expedia.bookings.data.trips.ItinCardDataAdapter;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.ItinCard.OnItinCardClickListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

@SuppressWarnings("rawtypes")
public class ItinListView extends ListView implements OnItemClickListener, OnScrollListener, OnItinCardClickListener {
	//////////////////////////////////////////////////////////////////////////////////////
	// INTERFACES
	//////////////////////////////////////////////////////////////////////////////////////

	public interface OnListModeChangedListener {
		public void onListModeChanged(int mode);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final String STATE_DO_AUTOSCROLL = "STATE_DO_AUTOSCROLL";
	private static final String STATE_DEFAULT_SAVESTATE = "STATE_DEFAULT_SAVESTATE";
	private static final String STATE_LAST_ITEM_COUNT = "STATE_LAST_ITEM_COUNT";

	public static final int SCROLL_HEADER_HIDDEN = -9999;

	public static final int MODE_LIST = 0;
	public static final int MODE_DETAIL = 1;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinCardDataAdapter mAdapter;

	private ItinCard mDetailsCard;

	private OnItemClickListener mOnItemClickListener;
	private OnScrollListener mOnScrollListener;
	private OnListModeChangedListener mOnListModeChangedListener;
	private OnItinCardClickListener mOnItinCardClickListener;

	private int mMode = MODE_LIST;

	private int mScrollState = SCROLL_STATE_IDLE;
	private int mDetailPosition = -1;
	private int mOriginalScrollY;
	private boolean mScrollToReleventOnDataSetChange;

	private int mExpandedCardHeight;
	private int mExpandedCardOriginalHeight;
	private int mLastItemCount = 0;

	private Semaphore mModeSwitchSemaphore = new Semaphore(1);

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinListView(Context context) {
		this(context, null);
	}

	public ItinListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ItinListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mAdapter = new ItinCardDataAdapter(context);
		mAdapter.setOnItinCardClickListener(this);

		setAdapter(mAdapter);
		setOnItemClickListener(null);
		setOnScrollListener(null);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(STATE_DEFAULT_SAVESTATE, super.onSaveInstanceState());
		bundle.putBoolean(STATE_DO_AUTOSCROLL, mScrollToReleventOnDataSetChange);
		bundle.putInt(STATE_LAST_ITEM_COUNT, mLastItemCount);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle && ((Bundle) state).containsKey(STATE_DEFAULT_SAVESTATE)) {
			super.onRestoreInstanceState(((Bundle) state).getParcelable(STATE_DEFAULT_SAVESTATE));
			mScrollToReleventOnDataSetChange = ((Bundle) state).getBoolean(STATE_DO_AUTOSCROLL, true);
			mLastItemCount = ((Bundle) state).getInt(STATE_LAST_ITEM_COUNT, 0);
		}
		else {
			super.onRestoreInstanceState(state);
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		unregisterDataSetObserver();
		mAdapter.disableSelfManagement();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		registerDataSetObserver();
		mAdapter.enableSelfManagement();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		final int scrollY = getScrollY();

		super.onWindowFocusChanged(hasWindowFocus);

		scrollTo(0, scrollY);
	}

	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
		super.setOnItemClickListener(this);
	}

	@Override
	public void setOnScrollListener(OnScrollListener listener) {
		mOnScrollListener = listener;
		super.setOnScrollListener(this);
	}

	// Touch overrides
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mMode == MODE_DETAIL) {
			if (mDetailsCard != null) {
				boolean retVal = mDetailsCard.dispatchTouchEvent(event);
				return retVal;
			}
			else {
				return super.onTouchEvent(event);
			}
		}

		View child = findMotionView((int) event.getY());
		if (child != null) {
			MotionEvent childEvent = MotionEvent.obtain(event);
			childEvent.offsetLocation(0, -child.getTop());

			if (child.dispatchTouchEvent(childEvent)) {
				childEvent.recycle();
				return true;
			}

			childEvent.recycle();
		}

		try {
			return super.onTouchEvent(event);
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			Log.e("ArrayIndexOutOfBoundsException in ItinListView.onTouchEvent()", ex);
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mScrollState == SCROLL_STATE_IDLE) {
			return onTouchEvent(ev);
		}

		return super.onInterceptTouchEvent(ev);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void setExpandedCardHeight(int height) {
		mExpandedCardHeight = height;
	}

	public int getMode() {
		return mMode;
	}

	public void setMode(int mode) {
		switch (mode) {
		default:
		case MODE_LIST: {
			hideDetails();
			break;
		}
		case MODE_DETAIL: {
			showDetails();
			break;
		}
		}
	}

	public void setOnListModeChangedListener(OnListModeChangedListener onListModeChangedListener) {
		mOnListModeChangedListener = onListModeChangedListener;
	}

	public void setOnItinCardClickListener(OnItinCardClickListener onItinCardClickListener) {
		mOnItinCardClickListener = onItinCardClickListener;
	}

	/**
	 * Calling this function will cause the list to be scrolled to the most relevant position the next time the data set changes
	 * if the previous data set contained 0 items. So when we first load up itins, scroll to our good position, otherwise dont
	 */
	public void enableScrollToRevelentWhenDataSetChanged() {
		mScrollToReleventOnDataSetChange = true;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private View findMotionView(int y) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt(i);
			if (y <= v.getBottom()) {
				return v;
			}
		}
		return null;
	}

	private View getFreshDetailView(int position) {
		int start = getFirstVisiblePosition();
		View view = getChildAt(position - start);
		if (view != null) {
			if (AndroidUtils.getSdkVersion() < 11) {
				//This could use some more investigation and possibly a more all around solution
				// 2.x needs this because otherwise all of the listview rows use the same view and they all expand which bones our animation
				// 4.x breaks from this because the adapter decides that it should call measure mid animation - the last details card was collapsed
				//	   after we used it, so it thinks that the height of the thing should be non-expanded height our animation gets boned
				mAdapter.setDetailPosition(position);
			}

			return mAdapter.getView(position, view, this);
		}
		return null;
	}

	private void clearDetailView() {
		mDetailPosition = -1;
		mDetailsCard = null;
		mAdapter.setDetailPosition(-1);
	}

	private boolean hideDetails() {
		boolean releaseSemHere = true;
		boolean semGot = false;
		try {
			if (mModeSwitchSemaphore.tryAcquire()) {
				semGot = true;
				if (mDetailPosition < 0 || mDetailsCard == null) {
					return false;
				}

				mMode = MODE_LIST;
				if (mOnListModeChangedListener != null) {
					mOnListModeChangedListener.onListModeChanged(mMode);
				}

				final int startY = getScrollY();
				final int stopY = mOriginalScrollY;

				ValueAnimator resizeAnimator = ResizeAnimator.buildResizeAnimator(mDetailsCard,
						mExpandedCardOriginalHeight);
				resizeAnimator.addUpdateListener(new AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator arg0) {
						scrollTo(0, (int) (((stopY - startY) * arg0.getAnimatedFraction()) + startY));
						onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
					}
				});

				AnimatorSet detailExpandAnim = mDetailsCard.collapse(false);
				AnimatorSet set = new AnimatorSet();
				set.playTogether(resizeAnimator, detailExpandAnim);

				set.addListener(new AnimatorListener() {

					@Override
					public void onAnimationCancel(Animator arg0) {
					}

					@Override
					public void onAnimationEnd(Animator arg0) {
						scrollTo(0, stopY);
						onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
						mDetailsCard.getLayoutParams().height = mExpandedCardOriginalHeight;
						mDetailsCard.requestLayout();

						clearDetailView();
						invalidateViews();
					}

					@Override
					public void onAnimationRepeat(Animator arg0) {
					}

					@Override
					public void onAnimationStart(Animator arg0) {
					}

				});

				set.addListener(mModeSwitchSemListener);
				set.start();
				releaseSemHere = false;
				return true;
			}
		}
		finally {
			if (releaseSemHere && semGot) {
				mModeSwitchSemaphore.release();
			}
		}
		return false;
	}

	private void showDetails() {
		showDetails(mDetailPosition);
	}

	@SuppressLint("NewApi")
	private boolean showDetails(int position) {
		boolean releaseSemHere = true;
		boolean semGot = false;
		try {
			if (mModeSwitchSemaphore.tryAcquire()) {
				semGot = true;
				mDetailsCard = (ItinCard) getFreshDetailView(position);
				if (mDetailsCard == null || !mDetailsCard.hasDetails()) {
					return false;
				}

				mDetailPosition = position;
				mMode = MODE_DETAIL;
				if (mOnListModeChangedListener != null) {
					mOnListModeChangedListener.onListModeChanged(mMode);
				}

				mExpandedCardHeight = mExpandedCardHeight > getHeight() ? mExpandedCardHeight : getHeight();
				mExpandedCardOriginalHeight = mDetailsCard.getHeight();
				mOriginalScrollY = getScrollY();

				final int startY = getScrollY();
				final int stopY = mDetailsCard.getTop();

				ValueAnimator resizeAnimator = ResizeAnimator.buildResizeAnimator(mDetailsCard, mExpandedCardHeight);
				if (AndroidUtils.getSdkVersion() >= 11) {
					resizeAnimator.addUpdateListener(new AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator arg0) {
							scrollTo(0, (int) (((stopY - startY) * arg0.getAnimatedFraction()) + startY));
							onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
						}

					});
				}
				else {
					this.setSelectionFromTop(mDetailPosition, 0);
				}

				AnimatorSet set = new AnimatorSet();
				AnimatorSet detailExpandAnim = mDetailsCard.expand(false);
				set.playTogether(resizeAnimator, detailExpandAnim);
				set.addListener(new AnimatorListener() {

					@Override
					public void onAnimationCancel(Animator arg0) {
					}

					@Override
					public void onAnimationEnd(Animator arg0) {
						mDetailsCard.getLayoutParams().height = mExpandedCardHeight;
						mDetailsCard.requestLayout();

						if (mDetailsCard != null) {
							switch (mDetailsCard.getType()) {
							case CAR:
								OmnitureTracking.trackItinCar(getContext());
								break;
							case FLIGHT:
								OmnitureTracking.trackItinFlight(getContext());
								break;
							case HOTEL:
								OmnitureTracking.trackItinHotel(getContext());
								break;
							case ACTIVITY:
								OmnitureTracking.trackItinActivity(getContext());
								break;
							}
						}
					}

					@Override
					public void onAnimationRepeat(Animator arg0) {
					}

					@Override
					public void onAnimationStart(Animator arg0) {
						onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
					}

				});

				set.addListener(mModeSwitchSemListener);
				set.start();
				releaseSemHere = false;
				return true;
			}
		}
		finally {
			if (releaseSemHere && semGot) {
				mModeSwitchSemaphore.release();
			}
		}
		return false;
	}

	private void registerDataSetObserver() {
		mAdapter.registerDataSetObserver(mDataSetObserver);
	}

	private void unregisterDataSetObserver() {
		mAdapter.unregisterDataSetObserver(mDataSetObserver);
	}

	/**
	 * Asks the adapter for the most relevent card and scrolls to it.
	 * @return the position scrolled to ( < 0 if invalid )
	 */
	private int scrollToMostRelevantCard() {
		if (mAdapter != null) {
			final int pos = mAdapter.getMostRelevantCardPosition();
			if (pos >= 0) {
				Runnable runner = new Runnable() {
					@SuppressLint("NewApi")
					@Override
					public void run() {
						if (ItinListView.this != null && ItinListView.this.mMode == MODE_LIST) {
							if (AndroidUtils.getSdkVersion() >= 11) {
								ItinListView.this.smoothScrollToPositionFromTop(pos, 0);
							}
							else {
								ItinListView.this.setSelectionFromTop(pos, 0);
							}
						}
					}
				};
				ItinListView.this.post(runner);
			}
			return pos;
		}
		return -1;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mAdapter.getItem(position).hasDetailData()) {
			showDetails(position);
		}

		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(parent, view, position, id);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).invalidate();
		}

		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;

		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onCloseButtonClicked() {
		hideDetails();

		if (mOnItinCardClickListener != null) {
			mOnItinCardClickListener.onCloseButtonClicked();
		}
	}

	@Override
	public void onShareButtonClicked(String subject, String shortMessage, String longMessage) {
		if (mOnItinCardClickListener != null) {
			mOnItinCardClickListener.onShareButtonClicked(subject, shortMessage, longMessage);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// INNER CLASS INSTANCES
	//////////////////////////////////////////////////////////////////////////////////////

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			if (mScrollToReleventOnDataSetChange && mLastItemCount <= 0) {
				if (scrollToMostRelevantCard() >= 0) {
					mScrollToReleventOnDataSetChange = false;
				}
			}
			if (mAdapter != null) {
				mLastItemCount = mAdapter.getCount();
			}
		}
	};

	private AnimatorListener mModeSwitchSemListener = new AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator arg0) {
			mModeSwitchSemaphore.release();
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			mModeSwitchSemaphore.release();
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
		}

	};
}
