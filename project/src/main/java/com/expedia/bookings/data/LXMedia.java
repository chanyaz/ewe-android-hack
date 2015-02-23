package com.expedia.bookings.data;

import android.content.Context;
import android.widget.ImageView;

import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;


public class LXMedia implements IMedia {

	String defaultUrl;

	public LXMedia(String defaultUrl) {
		this.defaultUrl = defaultUrl;
	}

	@Override
	public void loadImage(ImageView imageView, PicassoTarget target, int defaultResId) {
		new PicassoHelper.Builder(imageView).setPlaceholder(defaultResId).setTarget(target).build().load(defaultUrl);
	}

	@Override
	public void preloadImage(Context context) {
		new PicassoHelper.Builder(context).build().load(defaultUrl);
	}
}
