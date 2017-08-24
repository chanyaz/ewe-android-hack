package com.expedia.bookings.utils;

import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;

/**
 * Runnable that can be posted to a View that then focuses it.
 *
 * Only works with Fragments at the moment, but could be extended further.
 */
public class FocusViewRunnable implements Runnable {

	private static final long DELAY_MILLIS = 200;

	private final Fragment mFragment;
	private final View mView;

	public FocusViewRunnable(Fragment fragment, View view) {
		mFragment = fragment;
		mView = view;
	}

	@Override
	public void run() {
		// #283 - Don't run if we are no longer attached to an Activity
		if (mFragment.getActivity() == null) {
			return;
		}

		// Dumb but effective - show the keyboard by emulating a click on the view
		mView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
				SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
		mView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
				SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
	}

	/**
	 * Convenience method which does the standard action
	 */
	public static void focusView(Fragment fragment, View view) {
		view.postDelayed(new FocusViewRunnable(fragment, view), DELAY_MILLIS);
	}
}
