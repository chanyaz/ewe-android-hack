package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * This class handles the special case of the Hotel Details gallery (that zooms using NineOldAndroids).
 * When this view is invalidated, we want to also invalidate its parent, so that the quasi-zoomed-in region
 * is also invalidated. This is only necessary when using NOA, since in Honeycomb++ the acutal view should
 * be resized instead of the NOA hack.
 * @author doug
 *
 */
public class HotelDetailsGallery extends Gallery {

	View mInvalidateView;

	public HotelDetailsGallery(Context context) {
		this(context, null);
	}

	public HotelDetailsGallery(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.galleryStyle);
	}

	public HotelDetailsGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setInvalidateView(View view) {
		mInvalidateView = view;
	}

	@Override
	public void invalidate() {
		if (mInvalidateView == null) {
			super.invalidate();
		}
		else {
			mInvalidateView.invalidate();
		}
	}

}
