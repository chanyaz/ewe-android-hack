package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.expedia.bookings.R;

public class NewHotelDetailsScrollView extends GalleryScrollView {

	private int mInitialScrollTop;
	private int mLastScrollY;
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
		mLastScrollY = y;
		if (Math.abs(y - oldy) <= 1 || y >= getMeasuredHeight() || y == 0) {
			isFlinging = false;
		}
		super.onScrollChanged(x, y, oldx, oldy);
	}

	@Override
	public void fling(int velocityY) {
		float minFling = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
		float maxFling = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
		float flingFactor = (velocityY < 0 ? 5 : 4);
		float ourFling = ((maxFling / minFling) / flingFactor) * minFling;
		if (Math.abs(velocityY) > ourFling && mLastScrollY < mInitialScrollTop || mLastScrollY > mInitialScrollTop) {
			isFlinging = true;
			super.fling(velocityY);
		}
	}

	@Override
	public int getInitialScrollTop() {
		return mInitialScrollTop;
	}
}
