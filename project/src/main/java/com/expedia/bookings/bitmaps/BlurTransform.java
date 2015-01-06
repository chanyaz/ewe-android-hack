package com.expedia.bookings.bitmaps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import com.expedia.bookings.R;
import com.squareup.picasso.Transformation;

public class BlurTransform implements Transformation {
	private final String KEY = "Blur";

	private Context mContext;

	private static final int BLURRED_IMAGE_SIZE_REDUCTION_FACTOR = 4;
	private float mDarkenMultiplier;
	private int mBlurRadius;

	public BlurTransform(Context context) {
		mContext = context;
		Resources res = context.getResources();

		// Compute the darken multiplier
		mDarkenMultiplier = res.getFraction(R.fraction.stack_blur_darken_multiplier, 1, 1);

		// Compute the blur radius
		mBlurRadius = res.getDimensionPixelSize(R.dimen.stack_blur_radius);
	}
	@Override
	public Bitmap transform(Bitmap source) {
		Bitmap blurred = BitmapUtils.stackBlurAndDarken(source, mContext,
			BLURRED_IMAGE_SIZE_REDUCTION_FACTOR, mBlurRadius, mDarkenMultiplier);
		source.recycle();
		return blurred;
	}

	@Override
	public String key() {
		return KEY;
	}
}
