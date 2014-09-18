package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A ViewPager that allows you disable swiping to change pages.
 */
public class DisableableViewPager extends ViewPager {

	private static final String STATE_DEFAULT_SAVESTATE = "STATE_DEFAULT_SAVESTATE";
	private static final String STATE_PAGER_ENABLED = "STATE_PAGER_ENABLED";

	private boolean mPageSwipingEnabled = true;

	public DisableableViewPager(Context context) {
		super(context);
	}

	public DisableableViewPager(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

	}

	/**
	 * Do we want to allow page swiping? Enabled by default
	 * @param enabled - true=allow swiping false=disallow swiping
	 */
	public void setPageSwipingEnabled(boolean enabled) {
		mPageSwipingEnabled = enabled;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mPageSwipingEnabled) {
			return super.onTouchEvent(ev);
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mPageSwipingEnabled) {
			return super.onInterceptTouchEvent(ev);
		}
		return false;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(STATE_DEFAULT_SAVESTATE, super.onSaveInstanceState());
		bundle.putBoolean(STATE_PAGER_ENABLED, mPageSwipingEnabled);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle && ((Bundle) state).containsKey(STATE_DEFAULT_SAVESTATE)) {
			super.onRestoreInstanceState(((Bundle) state).getParcelable(STATE_DEFAULT_SAVESTATE));
			mPageSwipingEnabled = ((Bundle) state).getBoolean(STATE_PAGER_ENABLED, true);
		}
		else {
			super.onRestoreInstanceState(state);
		}
	}
}
