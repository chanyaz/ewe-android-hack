package com.expedia.bookings.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class PaintDrawable extends Drawable {
	private Paint mPaint;

	public PaintDrawable(Paint paint) {
		mPaint = paint;
	}

	@Override
	public void draw(Canvas canvas) {
		Rect bounds = getBounds();
		canvas.drawRect(bounds, mPaint);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSPARENT;
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// ignore
	}

	@Override
	public void setAlpha(int alpha) {
		// ignore
	}
}
