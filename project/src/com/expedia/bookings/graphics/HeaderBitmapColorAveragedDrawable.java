package com.expedia.bookings.graphics;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;

import com.expedia.bookings.bitmaps.BitmapUtils;

public class HeaderBitmapColorAveragedDrawable extends HeaderBitmapDrawable {

	private boolean mOverlayEnabled = false;

	public HeaderBitmapColorAveragedDrawable() {
		super();
	}

	public void enableOverlay() {
		mOverlayEnabled = true;
	}

	public void disableOverlay() {
		mOverlayEnabled = false;
	}

	@Override
	public void onBitmapLoaded(String url, Bitmap bitmap) {
		super.onBitmapLoaded(url, bitmap);
		if (mOverlayEnabled && bitmap != null) {
			setOverlayDrawable(new ColorDrawable(BitmapUtils.getAvgColorOnePixelTrick(bitmap)));
		}
	}
}