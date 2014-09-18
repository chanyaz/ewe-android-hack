package com.expedia.bookings.graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * This was developed for use in the actionbar.
 * <p/>
 * We set a background color, we set a foreground color, and we set a percentage.
 * The percentage corresponds to the alpha of the foreground color.
 */
public class PercentageFadeColorDrawable extends Drawable {

	private int mColorOneA;
	private int mColorOneR;
	private int mColorOneG;
	private int mColorOneB;

	private int mInitialColorTwoA;
	private int mColorTwoA;
	private int mColorTwoR;
	private int mColorTwoG;
	private int mColorTwoB;

	private int mAlpha = 255;

	public PercentageFadeColorDrawable(int colorOne, int colorTwo) {
		mColorOneA = Color.alpha(colorOne);
		mColorOneR = Color.red(colorOne);
		mColorOneG = Color.green(colorOne);
		mColorOneB = Color.blue(colorOne);

		mInitialColorTwoA = Color.alpha(colorTwo);
		mColorTwoA = Color.alpha(colorTwo);
		mColorTwoR = Color.red(colorTwo);
		mColorTwoG = Color.green(colorTwo);
		mColorTwoB = Color.blue(colorTwo);
	}

	public void setPercentage(float percentage) {
		percentage = Math.max(Math.min(percentage, 1f), 0f);
		mColorTwoA = (int) (percentage * mInitialColorTwoA);
		invalidateSelf();
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawARGB(Math.min(mAlpha, mColorOneA), mColorOneR, mColorOneG, mColorOneB);
		canvas.drawARGB(Math.min(mAlpha, mColorTwoA), mColorTwoR, mColorTwoG, mColorTwoB);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSPARENT;
	}

	@Override
	public void setAlpha(int alpha) {
		mAlpha = alpha;
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		throw new RuntimeException("ColorFilters currently not supported");
	}

}
