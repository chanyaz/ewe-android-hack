package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.expedia.bookings.R;
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

	private double mScaledDensity;

	private SearchResponse mSearchResponse;
	private Property[] mCachedProperties;
	private Integer[] mCachedMarkerPositions;

	private ListView mListView;
	private AbsListView.OnScrollListener mOnScrollListener;

	private double mTouchPercent;
	private boolean mListViewIsScrolling = false;

	private Drawable mBarDrawable;
	private Drawable mIndicatorDrawable;
	private Drawable mTripAdvisorMarker;

	private double mFirstVisibleItem;
	private double mVisibleItemCount;
	private double mTotalItemCount;

	private double mWidth;
	private double mHeight;
	private double mScreenHeight;
	private double mTop;
	private double mListViewHeight;
	private double mScrollHeight;

	private double mBarMinimumWidth;

	private double mIndicatorPaddingTop;
	private double mIndicatorPaddingBottom;
	private double mMarkerRangePaddingTop;
	private double mMarkerRangePaddingBottom;

	private double mIndicatorWidth;
	private double mMinIndicatorHeight;
	private double mIndicatorHeight;
	private double mIndicatorLeft;
	private double mIndicatorRight;

	private double mMarkerRangeHeight;

	private Rect mBarRect;

	private double mMarkerHeight;
	private double mMarkerWidth;
	private double mMarkerLeft;
	private double mMarkerRight;

	private int mHeaderHeight;
	private double mRowHeight;
	private double mAdjustedTotalHeight;

	private double mPaddingLeft;
	private double mPaddingTop;
	private double mPaddingRight;
	private double mPaddingBottom;

	//////////////////////////////////////////////////////////////////////////////////
	// Constructors

	public ListViewScrollBar(Context context) {
		super(context);
		init();

		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		mScreenHeight = display.getHeight();
	}

	public ListViewScrollBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();

		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		mScreenHeight = display.getHeight();
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
			mRowHeight = getRowHeight();
			mVisibleItemCount = mListViewHeight / mRowHeight;
		}

		if (mTotalItemCount < mVisibleItemCount) {
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
		mTripAdvisorMarker = getResources().getDrawable(R.drawable.scroll_highly_rated_marker);

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
		mFirstVisibleItem = (double) firstVisibleItem;
		mTotalItemCount = (double) totalItemCount;

		invalidate();

		// Bubble this event
		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mListViewIsScrolling = !(scrollState == SCROLL_STATE_IDLE);

		// Bubble this event
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		mWidth = width;
		mHeight = height;

		final int[] location = new int[2];
		mListView.getLocationOnScreen(location);
		mTop = location[1];

		mListViewHeight = mScreenHeight - mTop;

		setOnTouchListener(this);

		mIndicatorPaddingTop = PADDING_TOP_INDICATOR * mScaledDensity;
		mIndicatorPaddingBottom = PADDING_BOTTOM_INDICATOR * mScaledDensity;
		mMarkerRangePaddingTop = PADDING_TOP_MARKER_RANGE * mScaledDensity;
		mMarkerRangePaddingBottom = PADDING_BOTTOM_MARKER_RANGE * mScaledDensity;

		mMarkerRangeHeight = mHeight - mPaddingTop - mPaddingBottom - mIndicatorPaddingTop - mIndicatorPaddingBottom
				- mMarkerRangePaddingTop - mMarkerRangePaddingBottom;

		final double barLeft = ((mWidth - mPaddingLeft - mPaddingRight - mBarMinimumWidth) / 2) + mPaddingLeft;
		final double barRight = barLeft + mBarMinimumWidth;
		final double barTop = mPaddingTop;
		final double barBottom = mHeight - mPaddingBottom;

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
		final double y = event.getY();
		mTouchPercent = (y - mPaddingTop - mIndicatorPaddingTop - (mIndicatorHeight / 2f)) / mScrollHeight;
		mGestureDetector.onTouchEvent(event);

		int position = (int) ((mTotalItemCount * mTouchPercent) - (mVisibleItemCount / 2f));
		if (position < 0) {
			position = 0;
		}
		if (position >= mTotalItemCount) {
			position = (int) mTotalItemCount - 1;
		}

		final int offset = (int) ((mTouchPercent * mRowHeight * (mTotalItemCount - mVisibleItemCount)) - (position * mRowHeight));

		if (mListViewIsScrolling) {

		}
		((ListView) mListView).setSelectionFromTop(position, -offset);

		return true;
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Public methods

	public void setListView(ListView view) {
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

		mCachedProperties = mSearchResponse.getFilteredAndSortedProperties();
		for (Property property : mCachedProperties) {
			if (property.getAverageExpediaRating() >= Filter.HIGH_USER_RATING) {
				propertyPositions.add(i);
			}
			i++;
		}

		mCachedMarkerPositions = propertyPositions.toArray(new Integer[0]);
	}

	private double getRowHeight() {
		int firstVisible = mListView.getFirstVisiblePosition();
		int numHeaders = mListView.getHeaderViewsCount();
		int targetRow = 0;
		if (firstVisible < numHeaders) {
			targetRow = numHeaders;
		}
		double totalHeight = (mTotalItemCount - numHeaders) * mListView.getChildAt(targetRow).getHeight()
				+ getHeaderHeight() + (HEIGHT_ROW_DIVIDER * (mTotalItemCount - 1));
		return totalHeight / mTotalItemCount;
	}

	private int getHeaderHeight() {
		int numHeaders = mListView.getHeaderViewsCount();
		if (numHeaders == 0) {
			return 0;
		}

		int firstVisible = mListView.getFirstVisiblePosition();
		if (firstVisible == 0) {
			// Calculate the height of each header
			mHeaderHeight = 0;
			for (int a = 0; a < numHeaders; a++) {
				mHeaderHeight += mListView.getChildAt(a).getHeight() + HEIGHT_ROW_DIVIDER;
			}
		}

		// If the first header isn't visible, trust we calculated this correctly before.
		return mHeaderHeight;
	}

	private void drawBar(Canvas canvas) {
		mBarDrawable.setBounds(mBarRect);
		mBarDrawable.draw(canvas);
	}

	private void drawIndicator(Canvas canvas) {
		// SCROLL OFFSET
		final View firstChild = mListView.getChildAt(0);

		final double rowOffset = firstChild.getTop();

		mRowHeight = getRowHeight();
		mAdjustedTotalHeight = (mTotalItemCount - mVisibleItemCount) * mRowHeight;

		// Calculate this here to get an actual double for smooth scrolling
		mVisibleItemCount = mListViewHeight / mRowHeight;

		// INDICATOR HEIGHT
		mIndicatorHeight = (mHeight - mIndicatorPaddingTop - mIndicatorPaddingBottom)
				* (mVisibleItemCount / mTotalItemCount);
		mIndicatorHeight = mIndicatorHeight < mMinIndicatorHeight ? mMinIndicatorHeight : mIndicatorHeight;

		// TOTAL SCROLL HEIGHT
		mScrollHeight = mHeight - mIndicatorHeight - mIndicatorPaddingTop - mIndicatorPaddingBottom - mPaddingTop
				- mPaddingBottom;

		final double adjustedPosition = (mFirstVisibleItem * mRowHeight) - rowOffset;
		final double scrollPercent = adjustedPosition / mAdjustedTotalHeight;

		final double top = (mScrollHeight * scrollPercent) + mIndicatorPaddingTop + mPaddingTop;
		final double bottom = top + mIndicatorHeight;

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
				final double markerPercent = (double) mCachedMarkerPositions[i] / (mTotalItemCount - 1);
				final double top = (mMarkerRangeHeight * markerPercent) - (mMarkerHeight / 2) + mIndicatorPaddingTop
						+ mMarkerRangePaddingTop + mPaddingTop;

				mTripAdvisorMarker.setBounds((int) mMarkerLeft, (int) top, (int) mMarkerRight, (int) top
						+ (int) mMarkerHeight);
				mTripAdvisorMarker.draw(canvas);
			}
		}
	}

	private double getNearestTATouchPercentByIndicatorPercent(double percent) {
		if (mCachedMarkerPositions == null || mCachedProperties == null) {
			checkCachedMarkers();
		}

		final double translationRatio = (mScrollHeight / mMarkerRangeHeight);

		// Translate to marker range
		percent -= 0.5f;
		percent *= translationRatio;
		percent += 0.5f;

		final int propertiesSize = mCachedProperties.length;
		final int markersSize = mCachedMarkerPositions.length;
		Double minDifference = null;

		for (int i = 0; i < markersSize; i++) {
			final double markerPercent = (double) mCachedMarkerPositions[i] / ((double) propertiesSize - 1);
			final double difference = markerPercent - percent;
			final double absDifference = Math.abs(difference);
			if (absDifference < 0.05f) {
				if (minDifference == null || absDifference < Math.abs(minDifference)) {
					minDifference = difference;
				}
			}
		}

		if (minDifference != null) {
			percent += minDifference;
		}

		// Translate to indicator range
		percent -= 0.5f;
		percent /= translationRatio;
		percent += 0.5f;

		return percent;
	}

	private void init() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		mScaledDensity = metrics.scaledDensity;
	}

	private final GestureDetector mGestureDetector = new GestureDetector(new OnGestureListener() {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			mTouchPercent = getNearestTATouchPercentByIndicatorPercent(mTouchPercent);
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return onSingleTapUp(e);
		}
	});
}
