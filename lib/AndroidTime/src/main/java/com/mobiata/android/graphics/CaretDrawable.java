package com.mobiata.android.graphics;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Draws a caret, pointing either left or right.
 *
 * Scales itself automatically to its bounds.
 */
public class CaretDrawable extends Drawable {

	public enum Direction {
		LEFT,
		RIGHT
	}

	private Direction mDirection;
	private int mColor;
	private int mAlpha;
	private float mStrokeWidth;

	private Paint mPaint;

	private Path mPath;

	public CaretDrawable(Direction direction, int color) {
		mDirection = direction;
		mColor = color;
		mAlpha = 255;
		mStrokeWidth = 0;

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.STROKE);

		mPath = new Path();

		updatePaintColor();
	}

	public void setDirection(Direction direction) {
		if (direction != mDirection) {
			mDirection = direction;
			invalidateSelf();
		}
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

	public void setStrokeWidth(float strokeWidth) {
		if (mStrokeWidth != strokeWidth) {
			mStrokeWidth = strokeWidth;
			calculatePath(getBounds());
			invalidateSelf();
		}
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		calculatePath(bounds);
	}

	private void calculatePath(Rect bounds) {
		// We can pre-calculate the entire path/paint here
		if (mStrokeWidth > 0) {
			mPaint.setStrokeWidth(mStrokeWidth);
		}
		else {
			mPaint.setStrokeWidth(bounds.height() / 6.0f);
		}
		float halfStrokeWidth = mPaint.getStrokeWidth() / 2.0f;

		mPath.rewind();
		switch (mDirection) {
		case LEFT:
			mPath.moveTo(bounds.right - halfStrokeWidth, bounds.top + halfStrokeWidth);
			mPath.lineTo(bounds.left + halfStrokeWidth, bounds.top + (bounds.height() / 2.0f));
			mPath.lineTo(bounds.right - halfStrokeWidth, bounds.bottom - halfStrokeWidth);
			break;

		case RIGHT:
			mPath.moveTo(bounds.left + halfStrokeWidth, bounds.top + halfStrokeWidth);
			mPath.lineTo(bounds.right - halfStrokeWidth, bounds.top + (bounds.height() / 2.0f));
			mPath.lineTo(bounds.left + halfStrokeWidth, bounds.bottom - halfStrokeWidth);
			break;
		}
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawPath(mPath, mPaint);
	}
}
