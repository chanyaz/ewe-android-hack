package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.mobiata.android.Log;
import com.mobiata.android.R;
import com.mobiata.hotellib.data.Filter;
import com.mobiata.hotellib.data.Filter.OnFilterChangedListener;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchResponse;

public class ListViewScrollBar extends View implements OnScrollListener, OnFilterChangedListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	private static final int HEIGHT_INDICATOR_MIN = 24;

	private static final int PADDING_TOP_INDICATOR = 5;
	private static final int PADDING_BOTTOM_INDICATOR = 5;

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private float mScaledDensity;

	private SearchResponse mSearchResponse;
	private Filter mFilter;
	private Integer[] mCachedMarkerPositions;

	private boolean mDoRedraw;
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

	private float mPaddingTop;
	private float mPaddingBottom;

	private float mMinIndicatorWidth;
	private float mMinIndicatorHeight;
	private float mIndicatorHeight;

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
		if (mDoRedraw) {
			doDraw(canvas);
		}
	}

	@Override
	public void onFilterChanged() {
		checkCachedMarkers();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mBarDrawable = getResources().getDrawable(R.drawable.scroll_bar);
		mIndicatorDrawable = getResources().getDrawable(R.drawable.scroll_indicator);

		super.onMeasure(mBarDrawable.getMinimumWidth() | MeasureSpec.EXACTLY, heightMeasureSpec);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mFirstVisibleItem = firstVisibleItem;
		//mVisibleItemCount = visibleItemCount;
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

	/**
	 * Checks that we have the latest set of sorts/filters on the data.  If not, notify
	 * that the dataset has changed and update the data.  Should be called before
	 * any method that uses mCachedMarkers.
	 */
	public void checkCachedMarkers() {
		List<Integer> propertyPositions = new ArrayList<Integer>();
		int i = 0;

		for (Property property : mSearchResponse.getFilteredAndSortedProperties()) {
			if (property.getTripAdvisorRating() >= 4.5) {
				propertyPositions.add(i);
			}
			i++;
		}

		mCachedMarkerPositions = new Integer[propertyPositions.size()];
		mCachedMarkerPositions = propertyPositions.toArray(mCachedMarkerPositions);
	}

	private void doDraw(Canvas canvas) {
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

	private void drawBar(Canvas canvas) {
		mBarDrawable.setBounds(0, 0, (int) mWidth, (int) mHeight);
		mBarDrawable.draw(canvas);
	}

	private void drawIndicator(Canvas canvas) {
		// SCROLL OFFSET
		final int rowHeight = mListView.getChildAt(0).getHeight();
		final int rowOffset = mListView.getChildAt(0).getTop();

		// Calculate this here to get an actual float for smooth scrolling
		mVisibleItemCount = mHeight / rowHeight;

		// INDICATOR HEIGHT
		mIndicatorHeight = (mHeight - mPaddingTop - mPaddingBottom) * (mVisibleItemCount / mTotalItemCount);
		mIndicatorHeight = mIndicatorHeight >= mMinIndicatorHeight ? mIndicatorHeight : mMinIndicatorHeight;

		// TOTAL SCROLL HEIGHT
		mScrollHeight = mHeight - mIndicatorHeight - mPaddingTop - mPaddingBottom;

		final float adjustedPosition = (mFirstVisibleItem * rowHeight) - rowOffset;
		final float adjustedTotalHeight = (mTotalItemCount - mVisibleItemCount) * rowHeight;
		final float scrollPercent = adjustedPosition / adjustedTotalHeight;

		Log.t("h: %f - p: %f", adjustedTotalHeight, adjustedPosition);

		final float left = (mWidth - mMinIndicatorWidth) / 2;
		final float top = mScrollHeight * scrollPercent + mPaddingTop;
		final float right = left + mMinIndicatorWidth;
		final float bottom = top + mIndicatorHeight;

		mIndicatorDrawable.setBounds((int) left, (int) top, (int) right, (int) bottom);
		mIndicatorDrawable.draw(canvas);
	}

	private void drawTripAdvisorMarkers(Canvas canvas) {
		final float width = mTripAdvisorMarker.getMinimumWidth();
		final float height = mTripAdvisorMarker.getMinimumHeight();
		final float left = (mWidth - width) / 2;
		final float right = left + width;

		if (mCachedMarkerPositions != null) {
			final int size = mCachedMarkerPositions.length;
			for (int i = 0; i < size; i++) {
				final float top = (int) ((float) mCachedMarkerPositions[i] / mTotalItemCount * mScrollHeight)
						+ (mIndicatorHeight / 2) + mPaddingTop;

				mTripAdvisorMarker.setBounds((int) left, (int) top, (int) right, (int) top + (int) height);
				mTripAdvisorMarker.draw(canvas);
			}
		}
	}

	private void init(Context context) {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		mScaledDensity = metrics.scaledDensity;

		mPaddingTop = PADDING_TOP_INDICATOR * mScaledDensity;
		mPaddingBottom = PADDING_BOTTOM_INDICATOR * mScaledDensity;

		mBarDrawable = getResources().getDrawable(R.drawable.scroll_bar);
		mIndicatorDrawable = getResources().getDrawable(R.drawable.scroll_indicator);
		mTripAdvisorMarker = getResources().getDrawable(R.drawable.scroll_trip_advisor_marker);

		mMinIndicatorHeight = HEIGHT_INDICATOR_MIN * mScaledDensity;
		mMinIndicatorWidth = mIndicatorDrawable.getMinimumWidth();

		setMeasuredDimension(mBarDrawable.getMinimumWidth(), getMeasuredHeight());
	}
}
