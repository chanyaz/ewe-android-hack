package com.expedia.bookings.widget;

import java.lang.ref.WeakReference;

import android.widget.Scroller;

public class HotelDetailsScroller extends Scroller {
	private WeakReference<HotelDetailsScrollView> mScrollViewRef;

	public HotelDetailsScroller(HotelDetailsScrollView view) {
		super(view.getContext());
		mScrollViewRef = new WeakReference<HotelDetailsScrollView>(view);
	}

	@Override
	public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {

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
				view.smoothScrollTo(0, 0);
			}
			else {
				view.smoothScrollTo(0, initialScrollTop);
			}
			return;
		}

		super.fling(startX, startY, velocityX, velocityY, minX, maxX, modifiedMinY, modifiedMaxY);
	}
}