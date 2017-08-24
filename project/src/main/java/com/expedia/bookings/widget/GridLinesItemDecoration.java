package com.expedia.bookings.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridLinesItemDecoration extends RecyclerView.ItemDecoration {

	final Paint linePaint;

	public GridLinesItemDecoration(int color, float strokeWidth) {
		linePaint = new Paint();
		linePaint.setColor(color);
		linePaint.setStrokeWidth(strokeWidth);
		linePaint.setAntiAlias(true);
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		int parentRight = parent.getRight();
		int parentTop = parent.getTop();
		int halfStrokeWidth = (int) (linePaint.getStrokeWidth() / 2);

		for (int i = 0; i < parent.getChildCount(); i++) {
			View child = parent.getChildAt(i);
			int childLeft = child.getLeft();
			int childRight = child.getRight();
			int childBottom = child.getBottom();
			int childTop = child.getTop();

			if (childRight < parentRight) {
				drawLine(c, childRight - halfStrokeWidth, childTop, childRight - halfStrokeWidth, childBottom);
			}
			if (childTop > parentTop) {
				drawLine(c, childLeft, childTop + halfStrokeWidth, childRight, childTop + halfStrokeWidth);
			}
		}
	}

	private void drawLine(Canvas c, int left, int top, int right, int bottom) {
		c.drawLine(left, top, right, bottom, linePaint);
	}
}
