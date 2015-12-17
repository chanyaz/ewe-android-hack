package com.expedia.bookings.bitmaps;

import android.content.Context;
import android.widget.ImageView;

public interface IMedia {
	void loadImage(ImageView imageView, PicassoTarget target, int defaultResId);
	void loadErrorImage(ImageView imageView, PicassoTarget target, int fallbackId);
	void preloadImage(Context context);
	boolean isPlaceHolder();
}
