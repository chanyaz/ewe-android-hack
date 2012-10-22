package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class LaunchStreamListView extends MeasureListView implements OnScrollListener {

	private LaunchStreamListView mSlaveView;

	public LaunchStreamListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnScrollListener(this);
	}

	public void setSlaveView(LaunchStreamListView slave) {
		mSlaveView = slave;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// OnScrollListener
	//////////////////////////////////////////////////////////////////////////////////////////

	private boolean mDoNotPropogateNextScrollEvent = false;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mSlaveView == null) {
			return;
		}

		int deltaY = getDistanceScrolled();

		if (mDoNotPropogateNextScrollEvent) {
			mDoNotPropogateNextScrollEvent = false;
			return;
		}

		mSlaveView.scrollListBy(deltaY);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// getDistanceScrolled
	//////////////////////////////////////////////////////////////////////////////////////////

	private int mDistanceScrolledPosition;
	private int mDistanceScrolledOffset;

	// Returns the distance scrolled since the last call to this function.
	// *** This will only work if all cells in this ListView are the same height.
	private int getDistanceScrolled() {
		if (getChildAt(0) == null || getChildAt(1) == null) {
			return 0;
		}

		int position = getFirstVisiblePosition();
		int offset = getChildAt(0).getTop();
		int height = getChildAt(1).getTop() - offset;

		int deltaY = (mDistanceScrolledPosition - position) * height + (offset - mDistanceScrolledOffset);

		mDistanceScrolledPosition = position;
		mDistanceScrolledOffset = offset;

		return deltaY;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// scrollListBy()
	//////////////////////////////////////////////////////////////////////////////////////////

	private int mScrollByPosition = 0;
	private int mScrollByTop = 0;
	private int mScrollByDelta = 0;

	// If this is called twice quickly before a refresh, we need to just increase the distance
	// instead of replacing it.
	protected void scrollListBy(int deltaY) {
		mDoNotPropogateNextScrollEvent = true;

		int position = getFirstVisiblePosition();
		int top = (getChildAt(0) == null ? 0 : getChildAt(0).getTop());
		if (mScrollByPosition == position && mScrollByTop == top) {
			mScrollByDelta += deltaY;
		}
		else {
			mScrollByDelta = deltaY;
			mScrollByPosition = position;
			mScrollByTop = top;
		}
		if (mScrollByDelta != 0) {
			setSelectionFromTop(position, top + mScrollByDelta);
		}
	}
}
