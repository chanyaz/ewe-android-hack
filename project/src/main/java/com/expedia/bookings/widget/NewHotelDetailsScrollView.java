package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.OverScroller;

import com.expedia.bookings.R;

public class NewHotelDetailsScrollView extends GalleryScrollView {

	private int mInitialScrollTop;
	public boolean isFlinging;

	public NewHotelDetailsScrollView(Context context, AttributeSet attrs) {
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

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		if (Math.abs(y - oldy) <= 1 || y >= getMeasuredHeight() || y == 0) {
			isFlinging = false;
		}
		super.onScrollChanged(x, y, oldx, oldy);
	}

	@Override
	public void fling(int velocityY) {
		isFlinging = true;
		super.fling(velocityY);
	}

	@Override
	public int getInitialScrollTop() {
		return mInitialScrollTop;
	}
}
