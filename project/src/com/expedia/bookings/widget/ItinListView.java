package com.expedia.bookings.widget;

import android.content.Context;
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

import com.expedia.bookings.R;
import com.expedia.bookings.animation.ResizeAnimation;
import com.expedia.bookings.animation.ResizeAnimation.AnimationStepListener;
import com.expedia.bookings.data.trips.TripComponentAdapter;

public class ItinListView extends ListView implements OnItemClickListener, OnScrollListener {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	public static final int SCROLL_HEADER_HIDDEN = -9999;

	public static final int MODE_LIST = 0;
	public static final int MODE_DETAIL = 1;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private TripComponentAdapter mAdapter;

	private OnItemClickListener mOnItemClickListener;
	private OnScrollListener mOnScrollListener;

	private int mMode;
	private int mDetailPosition = -1;
	private int mExpandedCardOriginalSize;
	private int mOriginalScrollY;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinListView(Context context) {
		this(context, null);
	}

	public ItinListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		final int headerHeight = context.getResources().getDimensionPixelSize(R.dimen.launch_header_height);
		final View headerView = new View(context);
		headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, headerHeight));

		addHeaderView(headerView);

		mAdapter = new TripComponentAdapter(context);
		setAdapter(mAdapter);
		setOnItemClickListener(null);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mAdapter.disableSelfManagement();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		mAdapter.enableSelfManagement();
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
			return getChildAt(mDetailPosition - getFirstVisiblePosition()).dispatchTouchEvent(event);
		}

		return super.onTouchEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mMode == MODE_DETAIL) {
			return getChildAt(mDetailPosition - getFirstVisiblePosition()).dispatchTouchEvent(event);
		}

		return super.onInterceptTouchEvent(event);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

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

	public int getListScrollY() {
		int offset = -getScrollY();

		if (getFirstVisiblePosition() == 0 && getChildCount() > 0) {
			offset += getChildAt(0).getTop();
		}
		else {
			offset = SCROLL_HEADER_HIDDEN;
		}

		return offset;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void hideDetails() {
		if (mDetailPosition < 0) {
			return;
		}

		mMode = MODE_LIST;

		final ItinCard view = (ItinCard) getChildAt(mDetailPosition - getFirstVisiblePosition());
		final int animationPosition = mDetailPosition;

		final int startY = getScrollY();
		final int stopY = mOriginalScrollY;

		final ResizeAnimation animation = new ResizeAnimation(view, mExpandedCardOriginalSize);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.showSummary(animationPosition == 0);
				view.showDetails(false);
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
		mMode = MODE_DETAIL;
		mDetailPosition = position;

		final ItinCard view = (ItinCard) getChildAt(mDetailPosition - getFirstVisiblePosition());

		mExpandedCardOriginalSize = view.getHeight();
		mOriginalScrollY = getScrollY();

		final int startY = getScrollY();
		final int stopY = view.getTop();

		final ResizeAnimation animation = new ResizeAnimation(view, getHeight());
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				view.showSummary(true);
				view.showDetails(true);
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
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
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}
}