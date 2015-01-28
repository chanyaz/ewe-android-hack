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

	int mTop;
	int mBottom;

	public RecyclerDividerDecoration(Context context, int top, int bottom) {
		mTop = (int) context.getResources().getDisplayMetrics().density * top;
		mBottom = (int) context.getResources().getDisplayMetrics().density * bottom;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		outRect.top = mTop;
		outRect.bottom = mBottom;
	}
}
