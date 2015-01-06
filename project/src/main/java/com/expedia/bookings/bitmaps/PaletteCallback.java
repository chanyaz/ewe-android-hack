package com.expedia.bookings.bitmaps;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.squareup.picasso.Callback;

public abstract class PaletteCallback implements Callback {
	private ImageView mImageView;

	public PaletteCallback(ImageView imageView) {
		mImageView = imageView;
	}

	public abstract void onSuccess(int vibrantColor);

	public abstract void onFailed();

	@Override
	public void onSuccess() {
		Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap(); // Ew!
		Palette palette = PaletteTransformation.getPalette(bitmap);
		onSuccess(palette.getVibrantColor(R.color.transparent_dark));
	}

	@Override
	public void onError() {
		onFailed();
	}
}
