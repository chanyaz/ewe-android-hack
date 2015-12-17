package com.expedia.bookings.utils;

import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.expedia.bookings.widget.TouchableFrameLayout;
import com.mobiata.android.Log;

/**
 * This class helps control touch events being passed to a view.
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
 * <p/>
 * setPreventMashing(true, 500)
 * This will consume all new events that happen within 500ms of the previous event
 */
public class TouchControlHelper {

	//logging settings
	private boolean mLoggingEnabled = false;
	private String mLoggingTag = "";

	//For preventing touches from firing
	private boolean mBlockNewEvents = false;

	//For passing touches to a different view
	private boolean mPassThroughTouches = false;
	private View mTouchGetter;

	//For key mashing prevention
	private boolean mPreventMashing = false;
	private long mPreventMashingTouchCooldown = 150;//Arbitrary and small time
	private long mLastTouchTime = 0;

	//If we get the touch event (this does not interfere with touch intercept)
	//we just return true to indicate that we have handled the touch event.
	private boolean mConsumeTouch = false;


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

	public void setPreventMashing(boolean enabled) {
		mPreventMashing = enabled;
	}

	public void setPreventMashing(boolean enabled, long touchCooldownMs) {
		mPreventMashingTouchCooldown = touchCooldownMs;
		mPreventMashing = enabled;
	}

	public void setLoggingEnabled(boolean enabled) {
		mLoggingEnabled = enabled;
	}

	public void setLoggingTag(String tag) {
		mLoggingTag = tag == null ? "" : tag;
	}

	/**
	 * The view hosting this instance should call this in it's own onInterceptTouchEvent method.
	 * <p/>
	 * We return a pair of booleans. The first boolean dictates weather we did something with the event. The second
	 * value indicates what we should return. It is intended to be used in the following way:
	 * <p/>
	 * Pair<Boolean, Boolean> touchControl = mTouchHelper.onInterceptTouchEvent(ev);
	 * if (touchControl.first) {
	 * return touchControl.second;
	 * } else {
	 * return super.onInterceptTouchEvent(ev);
	 * }
	 *
	 * @param ev
	 * @return Pair of Booleans where the first value is true if we used the event, the second is the return value, but
	 * if and only if the first value is set to true.
	 */
	public Pair<Boolean, Boolean> onInterceptTouchEvent(MotionEvent ev) {
		Boolean consumedTouchEvent = false;
		Boolean viewEventReturnValue = false;

		// Don't allow any new actions to be used by children
		if (mBlockNewEvents && (ev.getAction() == MotionEvent.ACTION_DOWN
			|| ev.getAction() == MotionEvent.ACTION_POINTER_DOWN)) {
			log("onInterceptTouchEvent:mBlockNewEvents returning true");

			consumedTouchEvent = true;
			viewEventReturnValue = true;
		}
		//Pass touches
		else if (mPassThroughTouches && mTouchGetter != null) {

			log("onInterceptTouchEvent:mPassThroughTouches returning true");

			consumedTouchEvent = true;
			viewEventReturnValue = true;
		}
		//Cooldown
		else if (mPreventMashing &&
			(ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_POINTER_DOWN)) {
			if (mLastTouchTime > System.currentTimeMillis() - mPreventMashingTouchCooldown) {
				mLastTouchTime = System.currentTimeMillis();
				log("onInterceptTouchEvent:mPreventMashing returning true");
				consumedTouchEvent = true;
				viewEventReturnValue = true;
			}
			mLastTouchTime = System.currentTimeMillis();
		}

		return new Pair<Boolean, Boolean>(consumedTouchEvent, viewEventReturnValue);
	}

	/**
	 * The view hosting this instance should call this in it's own onTouchEvent method.
	 * <p/>
	 * We return a pair of booleans. The first boolean dictates weather we did something with the event. The second
	 * value indicates what we should return. It is intended to be used in the following way:
	 * <p/>
	 * Pair<Boolean, Boolean> touchControl = mTouchHelper.onTouchEvent(ev);
	 * if (touchControl.first) {
	 * return touchControl.second;
	 * } else {
	 * return super.onTouchEvent(ev);
	 * }
	 *
	 * @param ev
	 * @return Pair of Booleans where the first value is true if we used the event, the second is the return value, but
	 * if and only if the first value is set to true.
	 */
	public Pair<Boolean, Boolean> onTouchEvent(MotionEvent ev) {
		Boolean consumedTouchEvent = false;
		Boolean viewEventReturnValue = false;

		// Don't allow any actions to be used by parents
		if (mBlockNewEvents) {
			log("onTouchEvent:mBlockNewEvents returning true");
			consumedTouchEvent = true;
			viewEventReturnValue = true;
		}
		//pass touches
		else if (mPassThroughTouches && mTouchGetter != null) {
			mTouchGetter.dispatchTouchEvent(ev);
			log("onTouchEvent:mPassThroughTouches returning false");
			consumedTouchEvent = true;
			viewEventReturnValue = false;
		}
		else if (mConsumeTouch) {
			log("onTouchEvent:mConsumeTouch returning true");
			consumedTouchEvent = true;
			viewEventReturnValue = true;
		}
		else if (mPreventMashing && (ev.getAction() == MotionEvent.ACTION_DOWN
			|| ev.getAction() == MotionEvent.ACTION_POINTER_DOWN)) {
			log("onTouchEvent:mPreventMashing returning true");
			consumedTouchEvent = true;
			viewEventReturnValue = true;
		}

		return new Pair<>(consumedTouchEvent, viewEventReturnValue);
	}

	private void log(String msg) {
		if (mLoggingEnabled) {
			Log.d(mLoggingTag + " " + TouchableFrameLayout.class.getSimpleName() + " " + msg);
		}
	}
}
