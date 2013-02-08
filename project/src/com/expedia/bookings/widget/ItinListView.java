package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
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
import com.expedia.bookings.data.trips.ItinCardDataAdapter;
import com.mobiata.android.Log;

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

	private ItinCardDataAdapter mAdapter;

	private OnItemClickListener mOnItemClickListener;
	private OnScrollListener mOnScrollListener;

	private int mMode = MODE_LIST;
	private int mDetailPosition = -1;
	private int mOriginalScrollY;

	private int mExpandedCardHeight;
	private int mExpandedCardPaddingBottom;
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

		final Resources res = context.getResources();

		final int headerHeight = res.getDimensionPixelSize(R.dimen.launch_header_height);
		final View headerView = new View(context);
		headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, headerHeight));
		addHeaderView(headerView);

		mExpandedCardPaddingBottom = res.getDimensionPixelSize(R.dimen.itin_list_card_top_image_offset);

		mAdapter = new ItinCardDataAdapter(context);
		setAdapter(mAdapter);
		setOnItemClickListener(null);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mExpandedCardHeight = h - mExpandedCardPaddingBottom;
	}

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
		Log.t("ItinListView OnItemClickListener set");
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
		Log.t("onTouchEvent");
		if (mMode == MODE_DETAIL) {
			Log.t("Passing onTouchEvent to child");
			return getChildAt(mDetailPosition - getFirstVisiblePosition()).dispatchTouchEvent(event);
		}

		boolean handled = super.onTouchEvent(event);

		Log.t("onTouchEvent handled %s", handled);

		return handled;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		Log.t("onInterceptTouchEvent");
		if (mMode == MODE_DETAIL) {
			Log.t("Passing onInterceptTouchEvent to child");
			return getChildAt(mDetailPosition - getFirstVisiblePosition()).dispatchTouchEvent(event);
		}

		boolean handled = super.onTouchEvent(event);

		Log.t("onInterceptTouchEvent handled %s", handled);

		return handled;
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

		if (mAdapter.getCount() < 1) {
			offset = 0;
		}
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
				view.destroyDetailsView();
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

		Log.t("Showing itin details for position %d", position);

		final ItinCard view = (ItinCard) getChildAt(mDetailPosition - getFirstVisiblePosition());
		view.inflateDetailsView(mAdapter.getItem(mDetailPosition - getFirstVisiblePosition()));

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
		Log.t("onItemClick %d", position);

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