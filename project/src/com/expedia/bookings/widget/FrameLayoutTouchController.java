package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.expedia.bookings.utils.TouchControlHelper;

/**
 * A FrameLayout that takes advantage of TouchControlHelper.
 */
public class FrameLayoutTouchController extends FrameLayout {

	TouchControlHelper mTouchHelper = new TouchControlHelper();

	public FrameLayoutTouchController(Context context) {
		super(context);
	}

	public FrameLayoutTouchController(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FrameLayoutTouchController(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setBlockNewEventsEnabled(boolean enabled) {
		mTouchHelper.setBlockNewEventsEnabled(enabled);
	}

	public void setTouchPassThroughEnabled(boolean enabled) {
		mTouchHelper.setTouchPassThroughEnabled(enabled);
	}

	public void setTouchPassThroughReceiver(View view) {
		mTouchHelper.setTouchPassThroughReceiver(view);
	}

	public void setConsumeTouch(boolean enabled) {
		mTouchHelper.setConsumeTouch(enabled);
	}

	public void setPreventMashing(boolean enabled) {
		mTouchHelper.setPreventMashing(enabled);
	}

	public void setPreventMashing(boolean enabled, long touchCooldownMs) {
		mTouchHelper.setPreventMashing(enabled, touchCooldownMs);
	}

	public void setLoggingEnabled(boolean enabled) {
		mTouchHelper.setLoggingEnabled(enabled);
	}

	public void setLoggingTag(String tag) {
		mTouchHelper.setLoggingTag(tag);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		Pair<Boolean, Boolean> touchControl = mTouchHelper.onInterceptTouchEvent(ev);
		if (touchControl.first) {
			return touchControl.second;
		}
		else {
			return super.onInterceptTouchEvent(ev);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Pair<Boolean, Boolean> touchControl = mTouchHelper.onTouchEvent(ev);
		if (touchControl.first) {
			return touchControl.second;
		}
		else {
			return super.onTouchEvent(ev);
		}
	}
}
