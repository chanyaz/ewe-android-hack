package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.mobiata.android.R;

public class ListViewScrollBar extends View implements OnScrollListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private boolean mDoRedraw;
	private AbsListView mListView;
	private AbsListView.OnScrollListener mOnScrollListener;

	private Drawable mBarDrawable;
	private Drawable mIndicatorDrawable;

	private int mFirstVisibleItem;
	private int mVisibleItemCount;
	private int mTotalItemCount;

	private int mWidth;
	private int mHeight;

	//////////////////////////////////////////////////////////////////////////////////
	// Constructors

	public ListViewScrollBar(Context context) {
		super(context);
		init();
	}

	public ListViewScrollBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onDraw(Canvas canvas) {
		if (mDoRedraw) {
			doDraw(canvas);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mFirstVisibleItem = firstVisibleItem;
		mVisibleItemCount = visibleItemCount;
		mTotalItemCount = totalItemCount;

		mDoRedraw = true;
		invalidate();

		// Bubble this event
		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

		// Bubble this event
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		mWidth = width;
		mHeight = height;

		if (mBarDrawable != null) {
			mWidth = mBarDrawable.getMinimumWidth();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Public methods

	public void setListView(AbsListView view) {
		mListView = view;
		mListView.setOnScrollListener(this);
	}

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		mOnScrollListener = onScrollListener;
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

	private void doDraw(Canvas canvas) {
		if (mTotalItemCount < 1) {
			return;
		}

		drawBar(canvas);
		drawIndicator(canvas);
	}

	private void drawBar(Canvas canvas) {
		mBarDrawable.setBounds(0, 0, mWidth, mHeight);
		mBarDrawable.draw(canvas);
	}

	private void drawIndicator(Canvas canvas) {

		final int width = mIndicatorDrawable.getMinimumWidth();
		int height = mHeight * (mVisibleItemCount / mTotalItemCount);
		height = height >= 32 ? height : 32;

		final int left = (mWidth - width) / 2;
		final int top = (int) ((float) mHeight * ((float) mFirstVisibleItem / (float) mTotalItemCount));
		final int right = left + width;
		final int bottom = top + height;

		mIndicatorDrawable.setBounds(left, top, right, bottom);
		mIndicatorDrawable.draw(canvas);
	}

	private void init() {
		mBarDrawable = getResources().getDrawable(R.drawable.scroll_bar);
		mIndicatorDrawable = getResources().getDrawable(R.drawable.scroll_indicator);

		mWidth = mBarDrawable.getMinimumWidth();
	}
}
