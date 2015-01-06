package com.expedia.bookings.bitmaps;

import android.content.Context;
import android.widget.AbsListView;

public class PicassoScrollListener implements AbsListView.OnScrollListener {
	private String mTag;
	private PicassoHelper mHelper;
	public PicassoScrollListener(Context context, String tag) {
		mHelper = new PicassoHelper.Builder(context).build();
		mTag = tag;
	}

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
}
