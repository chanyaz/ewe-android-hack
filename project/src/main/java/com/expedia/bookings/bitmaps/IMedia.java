package com.expedia.bookings.bitmaps;

import android.content.Context;
import android.widget.ImageView;

public interface IMedia {
	public void loadImage(ImageView imageView, PicassoTarget target, int defaultResId);
	public void preloadImage(Context context);
}
