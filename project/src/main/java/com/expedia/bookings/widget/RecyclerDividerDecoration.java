package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/*
 * A very basic implementation of ItemDecoration that allows you
 * to quickly define the bottom divider height of RecyclerView items
 */

public class RecyclerDividerDecoration extends RecyclerView.ItemDecoration {

	int mPixels;

	public RecyclerDividerDecoration(Context context, int dp) {
		mPixels = (int) context.getResources().getDisplayMetrics().density * dp;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		outRect.bottom = mPixels;
	}
}
