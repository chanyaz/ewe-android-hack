package com.expedia.bookings.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.expedia.bookings.bitmaps.BitmapUtils;
import com.expedia.bookings.utils.ColorBuilder;
import com.mobiata.android.Log;

public class HeaderBitmapColorAveragedDrawable extends HeaderBitmapDrawable {

	private boolean mOverlayEnabled = false;
	private float mOverlayAlpha = 1f;
	private ColorDrawable mOverlay;

	public HeaderBitmapColorAveragedDrawable() {
		super();
	}

	public void enableOverlay() {
		mOverlayEnabled = true;
		setOverlayAlpha(mOverlayAlpha);
		setOverlayDrawable(mOverlay);
	}

	public void disableOverlay() {
		mOverlayEnabled = false;
		setOverlayDrawable(null);
	}

	/**
	 * 1f = fully transparent
	 * 0f = fully opaque
	 * @param alpha
	 */
	public void setOverlayAlpha(float alpha) {
		mOverlayAlpha = alpha;
		if (mOverlay != null) {
			int ialpha = (int)((1f - alpha) * 204);
			int currentColor = mOverlay.getColor();
			int newColor = Color.argb(ialpha, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor));
			mOverlay.setColor(newColor);
			invalidateSelf();
		}
	}

	@Override
	public void onBitmapLoaded(String url, Bitmap bitmap) {
		super.onBitmapLoaded(url, bitmap);
		if (bitmap != null) {
			ColorBuilder builder = new ColorBuilder(BitmapUtils.getAvgColorOnePixelTrick(bitmap)).darkenBy(0.2f);
			mOverlay = new ColorDrawable(builder.build());
			setOverlayAlpha(mOverlayAlpha);
			setOverlayDrawable(mOverlayEnabled ? mOverlay : null);
		}
	}
}
