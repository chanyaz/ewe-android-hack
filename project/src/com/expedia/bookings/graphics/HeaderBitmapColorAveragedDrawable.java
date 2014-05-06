package com.expedia.bookings.graphics;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;

import com.expedia.bookings.bitmaps.BitmapUtils;
import com.expedia.bookings.utils.ColorBuilder;

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
			ColorBuilder builder = new ColorBuilder(BitmapUtils.getAvgColorOnePixelTrick(bitmap)).darkenBy(0.4f)
				.setAlpha(204);
			setOverlayDrawable(new ColorDrawable(builder.build()));
		}
	}
}