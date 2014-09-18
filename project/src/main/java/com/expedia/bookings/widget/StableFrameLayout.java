package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * This is a version of FrameLayout that only lays itself out once.
 *
 * The advantage here is that you can use animations for its children using
 * setLeft(), setRight(), setTop() and setBottom() without worrying about
 * a layout pass midway through (which causes these values to be reset).
 *
 * Obviously, it is a very bad idea to use this in a context where the
 * FrameLayout would need to change its layout.
 */
public class StableFrameLayout extends FrameLayout {

	private boolean mHasDoneLayout = false;

	public StableFrameLayout(Context context) {
		super(context);
	}

	public StableFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public StableFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (!mHasDoneLayout) {
			super.onLayout(changed, left, top, right, bottom);
			mHasDoneLayout = true;
		}
	}

}
