package com.expedia.bookings.bitmaps;

import android.widget.ImageView;

public interface IMedia {
	void loadImage(ImageView imageView, PicassoTarget target, int defaultResId, boolean fullScreen);
	void loadErrorImage(ImageView imageView, PicassoTarget target, int fallbackId);
	void setIsPlaceholder(boolean isPlaceholder);
	boolean getIsPlaceHolder();
	int getPlaceHolderId();
	int getFallbackImage();

	String getDescription();
}
