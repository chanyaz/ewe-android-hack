package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * A FrameLayout that can manipulate touch handling.
 * 
 * Touch handling is handled by the following methods and in the following order
 * 
 * setBlockNewEventsEnabled(true):
 * This will cause the FrameLayout to intercept all new touch events and consume them
 * 
 * setTouchPassThroughEnabled(true);
 * setTouchPassThroughReceiver(View);
 * These work together so that all touch events will be passed to the provided view.
 * 
 * setConsumeTouch(true);
 * This wont always intercept the touch event but if the touch event is ours to deal with
 * we return true indicating that we have handled the touch event so it wont be passed along
 * 
 */
public class FrameLayoutTouchController extends FrameLayout {

	//For preventing touches from firing
	private boolean mBlockNewEvents = false;

	//For passing touches to a different view
	private boolean mPassThroughTouches = false;
	private View mTouchGetter;

	//If we get the touch event (this does not interfere with touch intercept)
	//we just return true to indicate that we have handled the touch event.
	private boolean mConsumeTouch = false;

	public FrameLayoutTouchController(Context context) {
		super(context);
	}

	public FrameLayoutTouchController(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FrameLayoutTouchController(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	//Block touches
	public void setBlockNewEventsEnabled(boolean enabled) {
		mBlockNewEvents = enabled;
	}

	//Pass touches to different view
	public void setTouchPassThroughEnabled(boolean enabled) {
		mPassThroughTouches = enabled;
	}

	public void setTouchPassThroughReceiver(View view) {
		mTouchGetter = view;
	}

	//Set consume my touch
	public void setConsumeTouch(boolean enabled) {
		mConsumeTouch = enabled;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// Don't allow any new actions to be used by children
		if (mBlockNewEvents && ev.getAction() == MotionEvent.ACTION_DOWN) {
			return true;
		}
		//Pass touches
		else if (mPassThroughTouches && mTouchGetter != null) {
			return true;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Don't allow any actions to be used by parents
		if (mBlockNewEvents) {
			return true;
		}
		//pass touches
		else if (mPassThroughTouches && mTouchGetter != null) {
			mTouchGetter.dispatchTouchEvent(event);
			return false;
		}
		else if (mConsumeTouch) {
			return true;
		}

		return super.onTouchEvent(event);
	}

}
