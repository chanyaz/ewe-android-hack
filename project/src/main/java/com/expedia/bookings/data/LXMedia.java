package com.expedia.bookings.data;

import java.util.List;

import android.content.Context;
import android.widget.ImageView;

import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;


public class LXMedia implements IMedia {
	private final List<String> imageURLs;
	private final String imageCaption;

	public LXMedia(List<String> imageURLs, String imageCaption) {
		this.imageURLs = imageURLs;
		this.imageCaption = imageCaption;
	}

	@Override
	public void loadImage(ImageView imageView, PicassoTarget target, int defaultResId) {
		imageView.setContentDescription(imageCaption);
		new PicassoHelper.Builder(imageView).setPlaceholder(defaultResId).setTarget(target).build().load(imageURLs);
	}

	@Override
	public void preloadImage(Context context) {
		new PicassoHelper.Builder(context).build().load(imageURLs);
	}

	@Override
	public void loadErrorImage(ImageView imageView, PicassoTarget target, int fallbackId) {
		imageView.setContentDescription(imageCaption);
	}

	@Override
	public boolean isPlaceHolder() {
		return false;
	}
}
