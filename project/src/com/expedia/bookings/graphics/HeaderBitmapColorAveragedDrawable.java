package com.expedia.bookings.graphics;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;

import com.expedia.bookings.bitmaps.ColorAvgUtils;
import com.expedia.bookings.bitmaps.ColorScheme;
import com.expedia.bookings.utils.ColorSchemeCache;

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
			ColorSchemeCache.getScheme(url, bitmap, mCallback);
		}
	}

	private ColorSchemeCache.Callback mCallback = new ColorSchemeCache.Callback() {
		@Override
		public void callback(ColorScheme colorScheme) {
			int colorDarkened = ColorAvgUtils.darken(colorScheme.primaryAccent, 0.4f);
			int overLayWithAlpha = 0xCC000000 | 0x00ffffff & colorDarkened;
			setOverlayDrawable(new ColorDrawable(overLayWithAlpha));
		}
	};
}