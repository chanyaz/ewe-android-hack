package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A very specialized version of ImageView, purely for
 * the BlurredBackgroundFragment.  It helps avoid overdraw
 * by only drawing down to a certain point.
 */
public class BoundedBottomImageView extends ImageView {

	private int mBottom = 0;

	// Pre-allocate for rendering
	private Rect mClipBounds;

	public BoundedBottomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mClipBounds = new Rect();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBottom != 0) {
			canvas.save();

			canvas.getClipBounds(mClipBounds);
			canvas.clipRect(mClipBounds.left, mClipBounds.top, mClipBounds.right, mBottom);

			super.onDraw(canvas);

			canvas.restore();
		}
		else {
			super.onDraw(canvas);
		}
	}

	public void setBottomBound(int bottom) {
		mBottom = bottom;
	}
}
