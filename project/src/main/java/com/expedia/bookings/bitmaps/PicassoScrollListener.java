package com.expedia.bookings.bitmaps;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

public class PicassoScrollListener extends RecyclerView.OnScrollListener implements AbsListView.OnScrollListener {
	private final String mTag;
	private final PicassoHelper mHelper;

	public PicassoScrollListener(Context context, String tag) {
		mHelper = new PicassoHelper.Builder(context).build();
		mTag = tag;
	}

	// ListView

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE ||
				scrollState == SCROLL_STATE_TOUCH_SCROLL) {
			mHelper.resume(mTag);
		}
		else {
			mHelper.pause(mTag);
		}
	}

	// RecyclerView

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
		if (newState == SCROLL_STATE_IDLE ||
				newState == SCROLL_STATE_TOUCH_SCROLL) {
			mHelper.resume(mTag);
		}
		else {
			mHelper.pause(mTag);
		}
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		//ignore
	}
}
