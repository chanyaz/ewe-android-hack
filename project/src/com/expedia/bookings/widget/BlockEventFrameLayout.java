package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * A FrameLayout that can block new actions on command.  Useful for disabling
 * the UI during animations.
 */
public class BlockEventFrameLayout extends FrameLayout {

	private boolean mBlockNewEvents = false;

	public BlockEventFrameLayout(Context context) {
		super(context);
	}

	public BlockEventFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BlockEventFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setBlockNewEventsEnabled(boolean enabled) {
		mBlockNewEvents = enabled;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// Don't allow any new actions to be used by children
		if (mBlockNewEvents && ev.getAction() == MotionEvent.ACTION_DOWN) {
			return true;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean sup = super.onTouchEvent(event);

		// Don't allow any actions to be used by parents
		if (mBlockNewEvents) {
			return true;
		}

		return sup;
	}

}
