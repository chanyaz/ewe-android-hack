package com.expedia.bookings.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;

/**
 * A variant of ColorDrawable that has the option of doing rounded corners.
 */
public class RoundedColorDrawable extends ColorDrawable {

	private float mCornerRadius = 0;

	private Paint mPaint;
	private RectF mRectF;

	public RoundedColorDrawable() {
		super();
		init();
	}

	public RoundedColorDrawable(int color) {
		super(color);
		init();
	}

	public RoundedColorDrawable(int color, float cornerRadius) {
		super(color);
		init();
		mCornerRadius = cornerRadius;
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mRectF = new RectF();
	}

	public void setCornerRadius(float cornerRadius) {
		mCornerRadius = cornerRadius;
	}

	@Override
	public void draw(Canvas canvas) {
		if (mCornerRadius != 0) {
			mPaint.setColor(getColor());
			mRectF.set(getBounds());
			canvas.drawRoundRect(mRectF, mCornerRadius, mCornerRadius, mPaint);
		}
		else {
			super.draw(canvas);
		}
	}

}
