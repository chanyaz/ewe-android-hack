package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.mobiata.android.R;
import com.mobiata.hotellib.data.Filter;
import com.mobiata.hotellib.data.Filter.OnFilterChangedListener;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchResponse;

public class ListViewScrollBar extends View implements OnScrollListener, OnFilterChangedListener {
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
	private Filter mFilter;
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

	private float mIndicatorPaddingTop;
	private float mIndicatorPaddingBottom;
	private float mMarkerRangePaddingTop;
	private float mMarkerRangePaddingBottom;

	private float mIndicatorWidth;
	private float mMinIndicatorHeight;
	private float mIndicatorHeight;

	private float mMarkerRangeHeight;

	private float mMarkerHeight;
	private float mMarkerWidth;
	private float mMarkerLeft;
	private float mMarkerRight;

	private float mRowHeight;
	private float mAdjustedTotalHeight;

	//////////////////////////////////////////////////////////////////////////////////
	// Constructors

	public ListViewScrollBar(Context context) {
		super(context);
		init(context);
	}

	public ListViewScrollBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onDraw(Canvas canvas) {
		// Check if we need to show
		if (mTotalItemCount < 1) {
			return;
		}
		if (mTotalItemCount < mVisibleItemCount) {
			return;
		}

		drawBar(canvas);
		drawIndicator(canvas);
		drawTripAdvisorMarkers(canvas);
	}

	@Override
	public void onFilterChanged() {
		checkCachedMarkers();
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mBarDrawable = getResources().getDrawable(R.drawable.scroll_bar);
		mIndicatorDrawable = getResources().getDrawable(R.drawable.scroll_indicator);

		super.onMeasure(mBarDrawable.getMinimumWidth() | MeasureSpec.EXACTLY, heightMeasureSpec);
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
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		mScaledDensity = metrics.scaledDensity;

		mWidth = width;
		mHeight = height;

		mIndicatorPaddingTop = PADDING_TOP_INDICATOR * mScaledDensity;
		mIndicatorPaddingBottom = PADDING_BOTTOM_INDICATOR * mScaledDensity;
		mMarkerRangePaddingTop = PADDING_TOP_MARKER_RANGE * mScaledDensity;
		mMarkerRangePaddingBottom = PADDING_BOTTOM_MARKER_RANGE * mScaledDensity;

		mMarkerRangeHeight = mHeight - mIndicatorPaddingTop - mIndicatorPaddingBottom - mMarkerRangePaddingTop
				- mMarkerRangePaddingBottom;

		mMinIndicatorHeight = HEIGHT_INDICATOR_MIN * mScaledDensity;
		mIndicatorWidth = mIndicatorDrawable.getMinimumWidth();

		mMarkerWidth = mTripAdvisorMarker.getMinimumWidth();
		mMarkerHeight = mTripAdvisorMarker.getMinimumHeight();
		mMarkerLeft = (mWidth - mMarkerWidth) / 2;
		mMarkerRight = mMarkerLeft + mMarkerWidth;
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

	public void setResponse(SearchResponse response) {
		mSearchResponse = response;
		mFilter = mSearchResponse.getFilter();
		mFilter.addOnFilterChangedListener(this);

		checkCachedMarkers();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

	public void checkCachedMarkers() {
		List<Integer> propertyPositions = new ArrayList<Integer>();
		int i = 0;

		for (Property property : mSearchResponse.getFilteredAndSortedProperties()) {
			if (property.getTripAdvisorRating() >= Filter.TRIP_ADVISOR_HIGH_RATING) {
				propertyPositions.add(i);
			}
			i++;
		}

		mCachedMarkerPositions = propertyPositions.toArray(new Integer[0]);
	}

	private void drawBar(Canvas canvas) {
		mBarDrawable.setBounds(0, 0, (int) mWidth, (int) mHeight);
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
		mScrollHeight = mHeight - mIndicatorHeight - mIndicatorPaddingTop - mIndicatorPaddingBottom;

		final float adjustedPosition = (mFirstVisibleItem * mRowHeight) - rowOffset;
		final float scrollPercent = adjustedPosition / mAdjustedTotalHeight;

		final float left = (mWidth - mIndicatorWidth) / 2;
		final float top = (mScrollHeight * scrollPercent) + mIndicatorPaddingTop;
		final float right = left + mIndicatorWidth;
		final float bottom = top + mIndicatorHeight;

		mIndicatorDrawable.setBounds((int) left, (int) top, (int) right, (int) bottom);
		mIndicatorDrawable.draw(canvas);
	}

	private void drawTripAdvisorMarkers(Canvas canvas) {
		if (mCachedMarkerPositions != null) {
			final int size = mCachedMarkerPositions.length;
			for (int i = 0; i < size; i++) {
				final float markerPercent = (float) mCachedMarkerPositions[i] / (mTotalItemCount - 1);
				final float top = (mMarkerRangeHeight * markerPercent) - (mMarkerHeight / 2) + mIndicatorPaddingTop
						+ mMarkerRangePaddingTop;

				mTripAdvisorMarker.setBounds((int) mMarkerLeft, (int) top, (int) mMarkerRight, (int) top
						+ (int) mMarkerHeight);
				mTripAdvisorMarker.draw(canvas);
			}
		}
	}

	private void init(Context context) {
		mBarDrawable = getResources().getDrawable(R.drawable.scroll_bar);
		mIndicatorDrawable = getResources().getDrawable(R.drawable.scroll_indicator);
		mTripAdvisorMarker = getResources().getDrawable(R.drawable.scroll_trip_advisor_marker);

		setMeasuredDimension(mBarDrawable.getMinimumWidth(), getMeasuredHeight());
	}
}
