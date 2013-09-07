package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * A FrameLayout that can pass its touch events to another view.
 * 
 * This was originally developed for Tablet Results 2013 for the following case:
 * 
 * We have a MapView that we want to draw in the background, but if we set the Visibility
 * of said map view to INVISIBLE it will not draw. If we set the alpha of said map view to 0,
 * it will draw. However, the MapView is on top of a bunch of other stuff that needs to allow 
 * user interaction. Setting the alpha = 0 does not prevent user interaction so we're hosed.
 * 
 * Solution: we put the mapview in one of these TouchThroughFrameLayout containers,
 * we set the pass through touch receiver to be behind this view, of the same size as this view,
 * and with visibility set to INVISIBLE. We pass it a touch, the android touch handling
 * system picks up on the fact that it is an invisible view, and passes the touch to whatever
 * is behind it. YAY!
 * 
 */
public class TouchThroughFrameLayout extends FrameLayout {

	private boolean mPassThroughTouches = false;
	private View mTouchGetter;
	

	public TouchThroughFrameLayout(Context context) {
		super(context);
	}

	public TouchThroughFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchThroughFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setTouchPassThroughEnabled(boolean enabled) {
		mPassThroughTouches = enabled;
	}
	
	public void setTouchPassThroughReceiver(View view){
		mTouchGetter = view;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(!mPassThroughTouches){
			return super.onInterceptTouchEvent(ev);
		}else{
			return true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!mPassThroughTouches){
			return super.onTouchEvent(event);
		}else if (mTouchGetter != null){
			mTouchGetter.dispatchTouchEvent(event);
			return false;
		}
		return false;
	}

}
