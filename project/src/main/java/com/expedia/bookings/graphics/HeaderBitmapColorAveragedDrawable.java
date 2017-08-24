package com.expedia.bookings.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.utils.ColorBuilder;
import com.squareup.picasso.Picasso;

public class HeaderBitmapColorAveragedDrawable extends HeaderBitmapDrawable {

	private boolean mOverlayEnabled = false;
	private float mOverlayAlpha = 1f;
	private ColorDrawable mOverlay;
	private int mDefaultOverlayColor;

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

	public void setDefaultOverlayColor(int defaultOverlayColor) {
		mDefaultOverlayColor = defaultOverlayColor;
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

	public PicassoTarget getPicassoTarget() {
		return callback;
	}

	private final PicassoTarget callback = new PicassoTarget() {

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			super.onBitmapLoaded(bitmap, from);

			if (bitmap != null) {
				Palette palette = Palette.generate(bitmap);
				int mutedColor = palette.getMutedColor(mDefaultOverlayColor);
				int color = new ColorBuilder(mutedColor).darkenBy(0.3f).build();

				mOverlay = new ColorDrawable(color);
				setOverlayAlpha(mOverlayAlpha);
				setOverlayDrawable(mOverlayEnabled ? mOverlay : null);
				enableOverlay();
			}

			setBitmap(bitmap);
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			super.onBitmapFailed(errorDrawable);
			invalidateSelf();
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			super.onPrepareLoad(placeHolderDrawable);
			setPlaceholderDrawable(placeHolderDrawable);
			disableOverlay();
		}
	};

}
