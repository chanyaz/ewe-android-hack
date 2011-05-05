package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.mobiata.android.R;
import com.mobiata.hotellib.data.Filter;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchResponse;

public class ListViewScrollBar extends View implements OnScrollListener, OnTouchListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	private static final int HEIGHT_INDICATOR_MIN = 24;
	private static final int HEIGHT_ROW_DIVIDER = 2;

	private static final int PADDING_TOP_INDICATOR = 5;
	private static final int PADDING_BOTTOM_INDICATOR = 3;
	private static final int PADDING_TOP_MARKER_RANGE = 8;
	private static final int PADDING_BOTTOM_MARKER_RANGE = 8;

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private float mScaledDensity;

	private SearchResponse mSearchResponse;
	private Integer[] mCachedMarkerPositions;

	private AbsListView mListView;
	private AbsListView.OnScrollListener mOnScrollListener;

	private Drawable mBarDrawable;
	private Drawable mIndicatorDrawable;
	private Drawable mTripAdvisorMarker;

	private float mFirstVisibleItem;
	private float mVisibleItemCount;
	private float mTotalItemCount;

	private float mWidth;
	private float mHeight;
	private float mScrollHeight;

	private float mBarMinimumWidth;

	private float mIndicatorPaddingTop;
	private float mIndicatorPaddingBottom;
	private float mMarkerRangePaddingTop;
	private float mMarkerRangePaddingBottom;

	private float mIndicatorWidth;
	private float mMinIndicatorHeight;
	private float mIndicatorHeight;
	private float mIndicatorLeft;
	private float mIndicatorRight;

	private float mMarkerRangeHeight;

	private Rect mBarRect;

	private float mMarkerHeight;
	private float mMarkerWidth;
	private float mMarkerLeft;
	private float mMarkerRight;

	private float mRowHeight;
	private float mAdjustedTotalHeight;

	private float mPaddingLeft;
	private float mPaddingTop;
	private float mPaddingRight;
	private float mPaddingBottom;

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
		// Check if we need to show
		if (mTotalItemCount < 1) {
			return;
		}

		if (mListView != null && mListView.getCount() > 0) {
			final View firstChild = mListView.getChildAt(0);
			mRowHeight = firstChild.getHeight() + HEIGHT_ROW_DIVIDER;
			mVisibleItemCount = mListView.getHeight() / mRowHeight;
		}

		if (mTotalItemCount < mVisibleItemCount) {
			return;
		}
		else if (mTotalItemCount == (int) (mVisibleItemCount + 0.5f)) {
			return;
		}

		drawBar(canvas);
		drawIndicator(canvas);
		drawTripAdvisorMarkers(canvas);
	}

	public void rebuildCache() {
		checkCachedMarkers();
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mBarDrawable = getResources().getDrawable(R.drawable.scroll_bar);
		mIndicatorDrawable = getResources().getDrawable(R.drawable.scroll_indicator);
		mTripAdvisorMarker = getResources().getDrawable(R.drawable.scroll_trip_advisor_marker);

		mPaddingLeft = getPaddingLeft();
		mPaddingTop = getPaddingTop();
		mPaddingRight = getPaddingRight();
		mPaddingBottom = getPaddingBottom();

		mBarMinimumWidth = mBarDrawable.getMinimumWidth();
		final int width = (int) (mBarMinimumWidth + mPaddingLeft + mPaddingRight);

		super.onMeasure(width | MeasureSpec.EXACTLY, heightMeasureSpec);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mFirstVisibleItem = (float) firstVisibleItem;
		mTotalItemCount = (float) totalItemCount;

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

		setOnTouchListener(this);

		mIndicatorPaddingTop = PADDING_TOP_INDICATOR * mScaledDensity;
		mIndicatorPaddingBottom = PADDING_BOTTOM_INDICATOR * mScaledDensity;
		mMarkerRangePaddingTop = PADDING_TOP_MARKER_RANGE * mScaledDensity;
		mMarkerRangePaddingBottom = PADDING_BOTTOM_MARKER_RANGE * mScaledDensity;

		mMarkerRangeHeight = mHeight - mPaddingTop - mPaddingBottom - mIndicatorPaddingTop - mIndicatorPaddingBottom
				- mMarkerRangePaddingTop - mMarkerRangePaddingBottom;

		final float barLeft = ((mWidth - mPaddingLeft - mPaddingRight - mBarMinimumWidth) / 2) + mPaddingLeft;
		final float barRight = barLeft + mBarMinimumWidth;
		final float barTop = mPaddingTop;
		final float barBottom = mHeight - mPaddingBottom;

		mBarRect = new Rect((int) barLeft, (int) barTop, (int) barRight, (int) barBottom);

		mMinIndicatorHeight = HEIGHT_INDICATOR_MIN * mScaledDensity;
		mIndicatorWidth = mIndicatorDrawable.getMinimumWidth();
		mIndicatorLeft = ((mWidth - mPaddingLeft - mPaddingRight - mIndicatorWidth) / 2) + mPaddingLeft;
		mIndicatorRight = mIndicatorLeft + mIndicatorWidth;

		mMarkerWidth = mTripAdvisorMarker.getMinimumWidth();
		mMarkerHeight = mTripAdvisorMarker.getMinimumHeight();
		mMarkerLeft = ((mWidth - mPaddingLeft - mPaddingRight - mMarkerWidth) / 2) + mPaddingLeft;
		mMarkerRight = mMarkerLeft + mMarkerWidth;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final float y = event.getY();

		final float indicatorHalfHeight = mIndicatorHeight / 2;
		final float percent = (y + mIndicatorPaddingTop + indicatorHalfHeight)
				/ (mHeight - mIndicatorPaddingBottom - indicatorHalfHeight);

		int position = (int) (((mTotalItemCount - mVisibleItemCount) * percent) - (mVisibleItemCount / 2));
		if (position < 0) {
			position = 0;
		}
		if (position >= mTotalItemCount) {
			position = (int) mTotalItemCount - 1;
		}

		mListView.setSelection(position);
		return true;
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

	public void setSearchResponse(SearchResponse response) {
		mSearchResponse = response;
		rebuildCache();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

	public void checkCachedMarkers() {
		List<Integer> propertyPositions = new ArrayList<Integer>();
		int i = 0;

		for (Property property : mSearchResponse.getFilteredAndSortedProperties(true)) {
			if (property.getTripAdvisorRating() >= Filter.TRIP_ADVISOR_HIGH_RATING) {
				propertyPositions.add(i);
			}
			i++;
		}

		mCachedMarkerPositions = propertyPositions.toArray(new Integer[0]);
	}

	private void drawBar(Canvas canvas) {
		mBarDrawable.setBounds(mBarRect);
		mBarDrawable.draw(canvas);
	}

	private void drawIndicator(Canvas canvas) {
		// SCROLL OFFSET
		final View firstChild = mListView.getChildAt(0);
		final float rowOffset = firstChild.getTop();

		mRowHeight = firstChild.getHeight() + HEIGHT_ROW_DIVIDER;
		mAdjustedTotalHeight = (mTotalItemCount - mVisibleItemCount) * mRowHeight;

		// Calculate this here to get an actual float for smooth scrolling
		mVisibleItemCount = mListView.getHeight() / mRowHeight;

		// INDICATOR HEIGHT
		mIndicatorHeight = (mHeight - mIndicatorPaddingTop - mIndicatorPaddingBottom)
				* (mVisibleItemCount / mTotalItemCount);
		mIndicatorHeight = mIndicatorHeight < mMinIndicatorHeight ? mMinIndicatorHeight : mIndicatorHeight;

		// TOTAL SCROLL HEIGHT
		mScrollHeight = mHeight - mIndicatorHeight - mIndicatorPaddingTop - mIndicatorPaddingBottom - mPaddingTop
				- mPaddingBottom;

		final float adjustedPosition = (mFirstVisibleItem * mRowHeight) - rowOffset;
		final float scrollPercent = adjustedPosition / mAdjustedTotalHeight;

		final float top = (mScrollHeight * scrollPercent) + mIndicatorPaddingTop + mPaddingTop;
		final float bottom = top + mIndicatorHeight;

		mIndicatorDrawable.setBounds((int) mIndicatorLeft, (int) top, (int) mIndicatorRight, (int) bottom);
		mIndicatorDrawable.draw(canvas);
	}

	private void drawTripAdvisorMarkers(Canvas canvas) {
		if (mCachedMarkerPositions != null) {
			final int size = mCachedMarkerPositions.length;
			if (size == mTotalItemCount) {
				return;
			}

			for (int i = 0; i < size; i++) {
				final float markerPercent = (float) mCachedMarkerPositions[i] / (mTotalItemCount - 1);
				final float top = (mMarkerRangeHeight * markerPercent) - (mMarkerHeight / 2) + mIndicatorPaddingTop
						+ mMarkerRangePaddingTop + mPaddingTop;

				mTripAdvisorMarker.setBounds((int) mMarkerLeft, (int) top, (int) mMarkerRight, (int) top
						+ (int) mMarkerHeight);
				mTripAdvisorMarker.draw(canvas);
			}
		}
	}

	private void init() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		mScaledDensity = metrics.scaledDensity;
	}
}
