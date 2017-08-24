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
public class TouchableFrameLayout extends FrameLayout {

	public interface TouchListener {
		void onInterceptTouch(MotionEvent ev);

		void onTouch(MotionEvent ev);
	}

	final TouchControlHelper mTouchHelper = new TouchControlHelper();

	TouchListener mTouchListener;

	public TouchableFrameLayout(Context context) {
		super(context);
	}

	public TouchableFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchableFrameLayout(Context context, AttributeSet attrs, int defStyle) {
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

	public void setPreventMashing(boolean enabled, long touchCooldownMs) {
		mTouchHelper.setPreventMashing(enabled, touchCooldownMs);
	}

	public void setTouchListener(TouchListener listener) {
		mTouchListener = listener;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mTouchListener != null) {
			mTouchListener.onInterceptTouch(ev);
		}

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
		if (mTouchListener != null) {
			mTouchListener.onTouch(ev);
		}

		Pair<Boolean, Boolean> touchControl = mTouchHelper.onTouchEvent(ev);
		if (touchControl.first) {
			return touchControl.second;
		}
		else {
			return super.onTouchEvent(ev);
		}
	}
}
