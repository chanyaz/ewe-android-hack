package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.mobiata.android.R;
import com.mobiata.hotellib.data.Filter;
import com.mobiata.hotellib.data.Filter.OnFilterChangedListener;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.SearchResponse;

public class ListViewScrollBar extends View implements OnScrollListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private SearchResponse mSearchResponse;
	private Filter mFilter;
	private Integer[] mCachedMarkerPositions;

	private boolean mDoRedraw;
	private AbsListView mListView;
	private AbsListView.OnScrollListener mOnScrollListener;

	private Drawable mBarDrawable;
	private Drawable mIndicatorDrawable;
	private Drawable mTripAdvisorMarker;

	private int mFirstVisibleItem;
	private int mVisibleItemCount;
	private int mTotalItemCount;

	private int mWidth;
	private int mHeight;

	private int mRowHeight;

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
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mBarDrawable = getResources().getDrawable(R.drawable.scroll_bar);
		mIndicatorDrawable = getResources().getDrawable(R.drawable.scroll_indicator);

		super.onMeasure(mBarDrawable.getMinimumWidth() | MeasureSpec.EXACTLY, heightMeasureSpec);
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
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Public methods

	public void setListView(AbsListView view) {
		mListView = view;
		mListView.setOnScrollListener(this);

		if (mListView.getChildCount() > 0) {
			mRowHeight = mListView.getChildAt(0).getHeight();
		}
	}

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		mOnScrollListener = onScrollListener;
	}

	public void setResponse(SearchResponse response) {
		mSearchResponse = response;
		mFilter = mSearchResponse.getFilter();
		mFilter.addOnFilterChangedListener(new OnFilterChangedListener() {
			@Override
			public void onFilterChanged() {
				checkCachedMarkers();

			}
		});
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
		mBarDrawable.setBounds(0, 0, mWidth, mHeight);
		mBarDrawable.draw(canvas);
	}

	private void drawIndicator(Canvas canvas) {
		final int width = mIndicatorDrawable.getMinimumWidth();
		int height = mHeight * (mVisibleItemCount / mTotalItemCount);
		height = height >= 32 ? height : 32;

		final int offset = (int) ((float) mListView.getChildAt(0).getTop() / (float) mHeight);

		final int left = (mWidth - width) / 2;
		final int top = (int) ((((float) mHeight - (float) height) * ((float) mFirstVisibleItem / (float) mTotalItemCount)) + ((float) height / 2))
				- offset;
		final int right = left + width;
		final int bottom = top + height;

		mIndicatorDrawable.setBounds(left, top, right, bottom);
		mIndicatorDrawable.draw(canvas);
	}

	private void drawTripAdvisorMarkers(Canvas canvas) {
		final int width = mTripAdvisorMarker.getMinimumWidth();
		int height = mTripAdvisorMarker.getMinimumHeight();

		final int left = (mWidth - width) / 2;
		final int right = left + width;

		checkCachedMarkers();

		if (mCachedMarkerPositions != null) {
			final int size = mCachedMarkerPositions.length;
			for (int i = 0; i < size; i++) {
				final int top = (int) (((float) mCachedMarkerPositions[i] / (float) mTotalItemCount) * (float) mHeight);
				mTripAdvisorMarker.setBounds(left, top, right, top + height);
				mTripAdvisorMarker.draw(canvas);
			}
		}
	}

	private void init() {
		mBarDrawable = getResources().getDrawable(R.drawable.scroll_bar);
		mIndicatorDrawable = getResources().getDrawable(R.drawable.scroll_indicator);
		mTripAdvisorMarker = getResources().getDrawable(R.drawable.scroll_trip_advisor_marker);

		setMeasuredDimension(mBarDrawable.getMinimumWidth(), getMeasuredHeight());
	}
}
