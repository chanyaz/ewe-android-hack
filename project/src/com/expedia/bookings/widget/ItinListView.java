package com.expedia.bookings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.expedia.bookings.animation.ResizeAnimation;
import com.expedia.bookings.animation.ResizeAnimation.AnimationStepListener;
import com.expedia.bookings.data.trips.ItinCardDataAdapter;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.ItinCard.OnItinCardClickListener;
import com.mobiata.android.util.AndroidUtils;

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

	public static final int SCROLL_HEADER_HIDDEN = -9999;

	public static final int MODE_LIST = 0;
	public static final int MODE_DETAIL = 1;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinCardDataAdapter mAdapter;

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
	public void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
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
			getChildAt(mDetailPosition - getFirstVisiblePosition()).dispatchTouchEvent(event);
			return true;
		}

		final int position = findMotionPosition((int) event.getY());
		if (position != INVALID_POSITION) {
			View child = getChildAt(position - getFirstVisiblePosition());
			MotionEvent childEvent = MotionEvent.obtain(event);
			childEvent.offsetLocation(0, -child.getTop());

			if (child.dispatchTouchEvent(childEvent)) {
				childEvent.recycle();
				return true;
			}

			childEvent.recycle();
		}

		return super.onTouchEvent(event);
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
	 * Calling this function will cause the list to be scrolled to the most relevant position the next time the data set changes (and has > 0 items)
	 */
	public void enableScrollToRevelentWhenDataSetChanged() {
		mScrollToReleventOnDataSetChange = true;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	int findMotionPosition(int y) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt(i);
			if (y <= v.getBottom()) {
				return getFirstVisiblePosition() + i;
			}
		}

		return INVALID_POSITION;
	}

	private void hideDetails() {
		if (mDetailPosition < 0) {
			return;
		}

		mMode = MODE_LIST;
		if (mOnListModeChangedListener != null) {
			mOnListModeChangedListener.onListModeChanged(mMode);
		}

		final ItinCard view = (ItinCard) getChildAt(mDetailPosition - getFirstVisiblePosition());
		final int startY = getScrollY();
		final int stopY = mOriginalScrollY;

		final ResizeAnimation animation = new ResizeAnimation(view, mExpandedCardOriginalHeight);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				view.collapse();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				invalidateViews();
				scrollTo(0, stopY);
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
			}
		});
		animation.setAnimationStepListener(new AnimationStepListener() {
			@Override
			public void onAnimationStep(Animation animation, float interpolatedTime) {
				scrollTo(0, (int) (((stopY - startY) * interpolatedTime) + startY));
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
			}
		});

		view.startAnimation(animation);

		mDetailPosition = -1;
	}

	private void showDetails() {
		showDetails(mDetailPosition);
	}

	private void showDetails(int position) {
		final ItinCard view = (ItinCard) getChildAt(position - getFirstVisiblePosition());
		if (!view.hasDetails()) {
			return;
		}

		mDetailPosition = position;
		mMode = MODE_DETAIL;
		if (mOnListModeChangedListener != null) {
			mOnListModeChangedListener.onListModeChanged(mMode);
		}

		mExpandedCardHeight = mExpandedCardHeight > getHeight() ? mExpandedCardHeight : getHeight();
		mExpandedCardOriginalHeight = view.getHeight();
		mOriginalScrollY = getScrollY();

		final int startY = getScrollY();
		final int stopY = view.getTop();

		final ResizeAnimation animation = new ResizeAnimation(view, mExpandedCardHeight);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				view.expand();

				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				scrollTo(0, stopY);
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());

				switch (view.getType()) {
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
		});
		animation.setAnimationStepListener(new AnimationStepListener() {
			@Override
			public void onAnimationStep(Animation animation, float interpolatedTime) {
				scrollTo(0, (int) (((stopY - startY) * interpolatedTime) + startY));
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
			}
		});

		view.startAnimation(animation);
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
								ItinListView.this.smoothScrollToPositionFromTop(pos, 2);
							}
							else {
								ItinListView.this.smoothScrollToPosition(pos);
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
		showDetails(position);

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
			if (mScrollToReleventOnDataSetChange) {
				if (scrollToMostRelevantCard() >= 0) {
					mScrollToReleventOnDataSetChange = false;
				}
			}
		}
	};
}
