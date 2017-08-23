package com.expedia.bookings.widget;

import java.lang.ref.WeakReference;

import android.widget.OverScroller;

public class FullscreenGalleryOverScroller<T extends GalleryScrollView> extends OverScroller {
	private WeakReference<T> mScrollViewRef;

	public FullscreenGalleryOverScroller(T view) {
		super(view.getContext());
		mScrollViewRef = new WeakReference<T>(view);
	}

	@Override
	public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY,
			int overX, int overY) {

		GalleryScrollView view = mScrollViewRef.get();
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
