package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;

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
	private double mListViewHeight;
	private double mTotalHeight;
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

	private double mRowHeight;
	private double mHeaderHeight;
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
			mRowHeight = getRowHeight();
			mVisibleItemCount = getVisibleItemCount();
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
		
		// NOTE: considering the height of the list view to be the same as that
		// of the scroll bar (ignoring padding) since both attempt to fill
		// up the available space.
		// This assumption is key in calculating (directly or indirectly) 
		// the number of visible items, scroll size and position
		mListViewHeight = mHeight;

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

	/**
	 * Save and restore the total height as well as the height of a header item
	 * so that we can use these in calculating the scroll size/position on resume
	 * even when the header is not showing
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable state = super.onSaveInstanceState();

		SavedState ss = new SavedState(state);
		ss.headerHeight = mHeaderHeight;
		ss.totalHeight = mTotalHeight;
		return ss;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		mHeaderHeight = ss.headerHeight;
		mTotalHeight = ss.totalHeight;
	}

	public static class SavedState extends BaseSavedState {
		double headerHeight;
		double totalHeight;
		
		SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			double values[] = new double[2];
			values[0] = headerHeight;
			values[1] = totalHeight;
			
			out.writeDoubleArray(values);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

			@Override
			public SavedState createFromParcel(Parcel source) {
				return new SavedState(source);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

		private SavedState(Parcel in) {
			super(in);
			
			double values[] = new double[2];
			in.readDoubleArray(values);
			
			headerHeight = values[0];
			totalHeight = values[1];
		}

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

	private void drawBar(Canvas canvas) {
		mBarDrawable.setBounds(mBarRect);
		mBarDrawable.draw(canvas);
	}

	private void drawIndicator(Canvas canvas) {
		// SCROLL OFFSET
		final View firstChild = mListView.getChildAt(0);

		final double rowOffset = firstChild.getTop();

		mRowHeight = getRowHeight();

		// Calculate this here to get an actual double for smooth scrolling
		mVisibleItemCount = getVisibleItemCount();

		
		// calculate the total height which takes into account 
		// the height of the headers and all the row dividers. 
		// Note that its only possible to calculate total height 
		// when the header views are visible. 
		// ASSUMPTIONS:
		// a) The header views don't occupy more space than the total number of visible items 
		// b) The height of each row is the same.
		if (mListView.getFirstVisiblePosition() == 0) {
			mTotalHeight = getTotalHeight();
		}

		// total height is calculated by manually calculating the height of all headers
		// and then adding up the heights occupied by the remaining rows. 
		// Note that the row divider is taken into account when calculating the row height
		// When the total height has not been calculated, we assume that all rows (including 
		// the headers) have the same height as every row and approximate the adjustedTotalHeight.
		mAdjustedTotalHeight = (mTotalHeight != 0) ? (mTotalHeight - mListViewHeight)
				: (mTotalItemCount - mVisibleItemCount) * mRowHeight;

		// INDICATOR HEIGHT
		// NOTE : When calculating the indicator height, we take into account
		// every visible row except for the variable-height header rows.
		// The reason for this is so that the indicator height does not change
		// as the header view is brought into the visible section of the list view and 
		// thus stays constant throughout
		// ASSUMPTIONS: 
		// a) height of each row is the same
		// b) height of each header row is the same
		mIndicatorHeight = (mHeight - mIndicatorPaddingTop - mIndicatorPaddingBottom - mPaddingTop - mPaddingBottom)
				* (getItemCountWithoutHeaderViews() / (mTotalItemCount - mListView.getHeaderViewsCount()));
		mIndicatorHeight = mIndicatorHeight < mMinIndicatorHeight ? mMinIndicatorHeight : mIndicatorHeight;

		// TOTAL SCROLL HEIGHT
		mScrollHeight = mHeight - mIndicatorHeight - mIndicatorPaddingTop - mIndicatorPaddingBottom - mPaddingTop
				- mPaddingBottom;

		// adjustedPosition variable indicates the total height of list view that is above the visible 
		// fold, to  proportionately position the indicator in the scroll view. We add the offset
		// to take into account the portion of the first visible item thats above the visible fold
		final double adjustedPosition = getTotalHeightUptoPosition((int) mFirstVisibleItem) + Math.abs(rowOffset);
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

	/**
	 * This helper method returns the approximate row height
	 * after taking into account all headers and row dividers between 
	 * each row in the list view. 
	 * 
	 * The assumption here is that the row height is the same
	 * for every row except the header rows.
	 */
	private double getRowHeight() {
		int firstVisible = mListView.getFirstVisiblePosition();
		int numHeaders = mListView.getHeaderViewsCount();
		int targetRow = 0;
		
		if (firstVisible < numHeaders) {
			targetRow = numHeaders;
		}
		
		// there are only headers in the list view. Therefore
		// treat the row height to be the same as the header height
		if (mListView.getChildCount() <= targetRow) {
			return getHeaderHeight(firstVisible) / (double) numHeaders;
		}

		// once again, assuming here that the height of every header is the same 
		// when attempting to calculate the height occupied by each header view
		double totalHeightWithoutHeaderViews = (mTotalItemCount - numHeaders) * mListView.getChildAt(targetRow).getHeight()
				+ (HEIGHT_ROW_DIVIDER * (mTotalItemCount - numHeaders - 1));

		return (totalHeightWithoutHeaderViews / (mTotalItemCount - numHeaders));
	}

	/**
	 * This method returns the total height occupied by all headers starting at
	 * the position specified
	 * 
	 * The method returns 0 if the startingPosition is greater than the
	 * number of headers.
	 */
	private double getHeaderHeight(int startingPosition) {
		int numHeaders = mListView.getHeaderViewsCount();
		if (numHeaders == 0) {
			return 0;
		}

		// Calculate the height of each header
		double headerHeight = 0;
		
		// assuming that the total height occupied by headers is only asked for
		// when some part of the header is showing
		for (int a = startingPosition; a < numHeaders; a++) {
			headerHeight += mListView.getChildAt(a).getHeight() + HEIGHT_ROW_DIVIDER;
			mHeaderHeight = mListView.getChildAt(a).getHeight() + HEIGHT_ROW_DIVIDER;
		}

		// If the first header isn't visible, trust we calculated this correctly before.
		return headerHeight;
	}

	/**
	 * This method returns the total height by taking into account row dividers 
	 * as well as header views 
	 */
	private double getTotalHeight() {
		return getHeaderHeight(0) + (mTotalItemCount - mListView.getHeaderViewsCount()) * mRowHeight;
	}

	/**
	 * This method returns the number of visible items taking into 
	 * account the fact that there might be headers of variable height
	 * that are visible.
	 */
	private double getVisibleItemCount() {
		int firstVisibleItemPosition = mListView.getFirstVisiblePosition();

		// get the height occupied by the headers in the list view
		double headerHeight = getHeaderHeight(firstVisibleItemPosition);

		// get the number of header items that are visible based on the first
		// visible item in the listview
		double headerItems = (headerHeight == 0) ? 0 : (headerHeight / mHeaderHeight);

		// the height remaining to be occupied by fixed width rows is then determined
		// based on the space occupied by the header (including the row dividers)
		double remainingHeight = mListViewHeight - headerHeight;

		// if the total header height is greater than the list view height,
		// then the number of 
		if (headerHeight > mListViewHeight) {
			return headerItems;
		}

		return headerItems + (remainingHeight / mRowHeight);
	}

	/**
	 *  This method returns the number of items (and fractions)
	 *  visible other than the header views themselves
	 */
	private double getItemCountWithoutHeaderViews() {
		return (mListViewHeight - (mHeaderHeight * mListView.getHeaderViewsCount())) / mRowHeight;
	}
	
	/**
	 * This method returns the total height of the list 
	 * that has already been scrolled up above the visible fold
	 */
	private double getTotalHeightUptoPosition(int position) {
		if (mFirstVisibleItem <= 0) {
			return 0;
		}

		if (mFirstVisibleItem < mListView.getHeaderViewsCount()) {
			return (mFirstVisibleItem * mListView.getChildAt(0).getHeight());
		}
		else {
			return (mListView.getHeaderViewsCount() * mHeaderHeight + (mFirstVisibleItem - mListView
					.getHeaderViewsCount()) * mRowHeight);
		}
	}
}
