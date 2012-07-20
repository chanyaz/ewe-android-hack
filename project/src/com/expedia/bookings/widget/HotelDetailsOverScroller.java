package com.expedia.bookings.widget;

import java.lang.ref.WeakReference;

import android.widget.OverScroller;

public class HotelDetailsOverScroller extends OverScroller {
	private WeakReference<HotelDetailsScrollView> mScrollViewRef;

	public HotelDetailsOverScroller(HotelDetailsScrollView view) {
		super(view.getContext());
		mScrollViewRef = new WeakReference<HotelDetailsScrollView>(view);
	}

	@Override
	public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY,
			int overX, int overY) {

		HotelDetailsScrollView view = mScrollViewRef.get();
		if (view == null) {
			return;
		}

		int initialScrollTop = view.getInitialScrollTop();
		int modifiedMinY = minY;
		int modifiedMaxY = maxY;

		// If flinging up from far down the page, stop if we get to mInitialScrollTop
		if (startY > initialScrollTop && velocityY < 0) {
			modifiedMinY = Math.max(initialScrollTop, minY);
		}

		else if (startY < initialScrollTop) {
			if (velocityY < 0) {
				modifiedMinY = 0;
				modifiedMaxY = 0;
			}
			else {
				modifiedMinY = initialScrollTop;
				modifiedMaxY = initialScrollTop;
			}
		}

		super.fling(startX, startY, velocityX, velocityY, minX, maxX, modifiedMinY, modifiedMaxY, 0, 20);
	}
}