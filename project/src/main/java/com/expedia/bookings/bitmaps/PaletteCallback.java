package com.expedia.bookings.bitmaps;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.ColorBuilder;
import com.squareup.picasso.Callback;

public abstract class PaletteCallback implements Callback {
	private final ImageView mImageView;

	public PaletteCallback(ImageView imageView) {
		mImageView = imageView;
	}

	public abstract void onSuccess(int color);

	public abstract void onFailed();

	@Override
	public void onSuccess() {
		int defaultColor = mImageView.getContext().getResources().getColor(R.color.transparent_dark);

		Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap(); // Ew!
		Palette palette = PaletteTransformation.getPalette(bitmap);

		int darkenedMutedColor = new ColorBuilder(palette.getMutedColor(defaultColor)).darkenBy(0.3f).build();
		onSuccess(darkenedMutedColor);
	}

	@Override
	public void onError() {
		onFailed();
	}
}
