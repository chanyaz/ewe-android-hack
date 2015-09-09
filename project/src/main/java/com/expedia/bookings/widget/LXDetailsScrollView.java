package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.OverScroller;

import com.expedia.bookings.R;

public class LXDetailsScrollView extends GalleryScrollView {

	private int mInitialScrollTop;

	public LXDetailsScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected OverScroller initScroller() {
		return new FullscreenGalleryOverScroller(this);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		int h = b - t;
		mInitialScrollTop = h - (getResources().getDimensionPixelSize(R.dimen.gallery_height));
	}

	public int getInitialScrollTop() {
		return mInitialScrollTop;
	}
}
