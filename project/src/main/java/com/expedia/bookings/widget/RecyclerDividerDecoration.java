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

	public RecyclerDividerDecoration() {
		// Default constructor
	}

	public RecyclerDividerDecoration(Context context, int left, int top, int right, int bottom, int header, int footer,
		boolean drawDivider) {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(1);
		mPaint.setColor(context.getResources().getColor(R.color.search_dropdown_disabled_stroke));
		shouldDrawDivider = drawDivider;

		int topScaledPadding = (int) context.getResources().getDisplayMetrics().density * top;
		int bottomScaledPadding = (int) context.getResources().getDisplayMetrics().density * bottom;
		int rightScaledPadding = (int) context.getResources().getDisplayMetrics().density * right;
		int leftScaledPadding = (int) context.getResources().getDisplayMetrics().density * left;
		mTop = topScaledPadding / 2;
		mBottom = bottomScaledPadding / 2;
		mLeft = leftScaledPadding;
		mRight = rightScaledPadding;
		mHeader = header + topScaledPadding;
		mFooter = footer + bottomScaledPadding;
	}

	public RecyclerDividerDecoration(Context context, int padding, int header, int footer, boolean drawDivider) {
		this(context, padding, padding, padding, padding, header, footer, drawDivider);
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		outRect.top = mTop;
		outRect.bottom = mBottom;
		outRect.right = mRight;
		outRect.left = mLeft;

		if (parent.getChildAdapterPosition(view) == 0) {
			outRect.top = mHeader;
		}
		else if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
			outRect.bottom = mFooter;
		}
	}

	@Override
	public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
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
