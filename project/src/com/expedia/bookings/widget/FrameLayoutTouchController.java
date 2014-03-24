package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.mobiata.android.Log;

/**
 * A FrameLayout that can manipulate touch handling.
 * <p/>
 * Touch handling is handled by the following methods and in the following order
 * <p/>
 * setBlockNewEventsEnabled(true):
 * This will cause the FrameLayout to intercept all new touch events and consume them
 * <p/>
 * setTouchPassThroughEnabled(true);
 * setTouchPassThroughReceiver(View);
 * These work together so that all touch events will be passed to the provided view.
 * <p/>
 * setConsumeTouch(true);
 * This wont always intercept the touch event but if the touch event is ours to deal with
 * we return true indicating that we have handled the touch event so it wont be passed along
 */
public class FrameLayoutTouchController extends FrameLayout {

	//logging settings
	private boolean mLoggingEnabled = false;
	private String mLoggingTag = "";

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


	public void setLoggingEnabled(boolean enabled) {
		mLoggingEnabled = enabled;
	}

	public void setLoggingTag(String tag) {
		mLoggingTag = tag == null ? "" : tag;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// Don't allow any new actions to be used by children
		if (mBlockNewEvents && ev.getAction() == MotionEvent.ACTION_DOWN) {
			log("onInterceptTouchEvent:mBlockNewEvents returning true");
			return true;
		}
		//Pass touches
		else if (mPassThroughTouches && mTouchGetter != null) {

			log("onInterceptTouchEvent:mPassThroughTouches returning true");

			return true;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Don't allow any actions to be used by parents
		if (mBlockNewEvents) {
			log("onTouchEvent:mBlockNewEvents returning true");
			return true;
		}
		//pass touches
		else if (mPassThroughTouches && mTouchGetter != null) {
			mTouchGetter.dispatchTouchEvent(event);
			log("onTouchEvent:mPassThroughTouches returning false");
			return false;
		}
		else if (mConsumeTouch) {
			log("onTouchEvent:mConsumeTouch returning true");
			return true;
		}

		return super.onTouchEvent(event);
	}

	private void log(String msg) {
		if (mLoggingEnabled) {
			Log.d(mLoggingTag + " FrameLayoutTouchController " + msg);
		}
	}

}
