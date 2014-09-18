package com.expedia.bookings.graphics;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Draws an arrow pointing up.  Could be extended to do more.
 * 
 * TODO: Delete me eventually.  I am just temporary placeholders before
 * we get assets.
 */
public class ArrowDrawable extends Drawable {

	private int mColor;
	private int mAlpha;

	private Paint mPaint;

	private Path mPath;

	public ArrowDrawable(int color) {
		mColor = color;
		mAlpha = 255;

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL);

		mPath = new Path();

		updatePaintColor();
	}

	public void setColor(int color) {
		if (color != mColor) {
			mColor = color;
			updatePaintColor();
			invalidateSelf();
		}
	}

	@Override
	public void setAlpha(int alpha) {
		if (alpha != mAlpha) {
			mAlpha = alpha;
			updatePaintColor();
			invalidateSelf();
		}
	}

	private void updatePaintColor() {
		// Code lovingly ripped off from ColorDrawable
		int alpha = mAlpha + (mAlpha >> 7);
		int baseAlpha = mColor >>> 24;
		int useAlpha = baseAlpha * alpha >> 8;
		int color = (mColor << 8 >>> 8) | (useAlpha << 24);
		mPaint.setColor(color);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// No effect
	}

	@Override
	public int getOpacity() {
		if (mAlpha == 0) {
			return PixelFormat.TRANSPARENT;
		}

		return PixelFormat.TRANSLUCENT;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);

		mPath.rewind();
		mPath.moveTo(bounds.left, bounds.bottom);
		mPath.lineTo((bounds.right - bounds.left) / 2.0f + bounds.left, bounds.top);
		mPath.lineTo(bounds.right, bounds.bottom);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawPath(mPath, mPaint);
	}
}
