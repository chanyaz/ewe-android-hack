package com.expedia.bookings.data;

import java.util.List;

import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;

public class DefaultMedia implements IMedia {
	private final List<String> imageURLs;
	private final String imageCaption;
	private int placeholderId = 0;
	private boolean isPlaceholder = false;

	public DefaultMedia(List<String> imageURLs, String imageCaption) {
		this.imageURLs = imageURLs;
		this.imageCaption = imageCaption;
	}

	public DefaultMedia(List<String> imageURLs, String imageCaption, int placeholderId) {
		this.imageURLs = imageURLs;
		this.imageCaption = imageCaption;
		this.placeholderId = placeholderId;
	}

	@Override
	public void loadImage(ImageView imageView, PicassoTarget target, int defaultResId, boolean fullScreen) {
		imageView.setContentDescription(imageCaption);
		new PicassoHelper.Builder(imageView)
			.setPlaceholder(defaultResId)
			.setTarget(target)
			.setError(getPlaceHolderId())
			.build().load(imageURLs);
	}

	@Override
	public void loadErrorImage(final ImageView imageView, final PicassoTarget target, final int fallbackId) {
		imageView.setContentDescription(imageCaption);
		imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				imageView.getViewTreeObserver().removeOnPreDrawListener(this);
				new PicassoHelper.Builder(imageView.getContext())
					.setTarget(target)
					.build()
					.load(fallbackId);
				return true;
			}
		});
	}

	@Override
	public void setIsPlaceholder(boolean isPlaceholder) {
		this.isPlaceholder = isPlaceholder;
	}

	@Override
	public boolean getIsPlaceHolder() {
		return isPlaceholder;
	}


	@Override
	public int getPlaceHolderId() {
		return placeholderId;
	}

	@Override
	public int getFallbackImage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return imageCaption;
	}
}
