package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.R;

/*
 * A very basic implementation of ItemDecoration that allows you
 * to quickly define the bottom divider height of RecyclerView items
 */

public class RecyclerDividerDecoration extends RecyclerView.ItemDecoration {

	int mTop;
	int mBottom;
	int mLeft;
	int mRight;

	// Padding for transparent Toolbar
	int mHeader;
	int mFooter;

	// Divider Separator
	Paint mPaint = new Paint();
	boolean shouldDrawDivider = false;

	public RecyclerDividerDecoration(Context context, int top, int bottom, int header, boolean drawDivider) {

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(1 * context.getResources().getDisplayMetrics().density);
		mPaint.setColor(context.getResources().getColor(R.color.cars_dropdown_disabled_stroke));
		shouldDrawDivider = drawDivider;

		mTop = (int) context.getResources().getDisplayMetrics().density * top / 2;
		mBottom = (int) context.getResources().getDisplayMetrics().density * bottom / 2;
		mLeft = mTop * 2;
		mRight = mTop * 2;
		mHeader = header + mTop * 2;
		mFooter = mBottom * 2;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		outRect.top = mTop;
		outRect.bottom = mBottom;
		outRect.right = mRight;
		outRect.left = mLeft;

		if (parent.getChildPosition(view) == 0) {
			outRect.top = mHeader;
		}
		else if (parent.getChildPosition(view) == parent.getAdapter().getItemCount() - 1) {
			outRect.bottom = mFooter;
		}
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent) {
		if (!shouldDrawDivider) {
			return;
		}
		int left = parent.getPaddingLeft();
		int right = parent.getWidth() - parent.getPaddingRight();

		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = parent.getChildAt(i);
			RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
			int bottom = child.getBottom() + params.bottomMargin + mBottom;
			c.drawLine(left, bottom, right, bottom, mPaint);
		}
	}

}
