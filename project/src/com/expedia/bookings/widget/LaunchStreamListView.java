package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.mobiata.android.Log;

public class LaunchStreamListView extends MeasureListView implements OnScrollListener {

	private LaunchStreamListView mSlaveView;

	public LaunchStreamListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setSlaveView(LaunchStreamListView slave) {
		mSlaveView = slave;
		mSlaveView.setOnScrollListener(this);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		if (mSlaveView != null) {
			int top = (mSlaveView.getChildAt(0) == null ? 0 : mSlaveView.getChildAt(0).getTop()) + t - oldt;
			mSlaveView.setSelectionFromTop(mSlaveView.getFirstVisiblePosition() + 1, top);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}
}
