package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

/**
 *	SwipeOutLayout - a class for allowing us to drag a view a short distance
 *	revealing a view of equal or smaller size behind it, reporting its state to
 *	n listeners.
 *
 *   SwipeOutLayout must be defined with exactly two children having ids: R.id.swipe_out_content and R.id.swipe_out_indicator
 * 
 *   SwipeOutLayout is expected to be defined in xml, and to have the following property app:swipeOutDirection="east" where east
 *   can be any of north,south,east,west.
 * 
 *   This was developed as a way to remove an item from a collection.
 *   E.g. We drag our view to the left, revealing a red x symbol, and when the user lets go a trip is removed.
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SwipeOutLayout extends FrameLayout {

	public enum Direction {
		NORTH, SOUTH, EAST, WEST
	}

	private ArrayList<ISwipeOutListener> mListeners = new ArrayList<ISwipeOutListener>();

	private int mContentResId = R.id.swipe_out_content;
	private int mIndicatorResId = R.id.swipe_out_indicator;

	private float mMaxSlideOutDistance;
	private View mContentView;
	private View mSwipeOutView;
	private Direction mSwipeDirection = Direction.NORTH;
	private boolean mSwipeEnabled = false;
	private boolean mAlwaysSnapBack = true;

	Rect mLayoutRectContent = new Rect();
	Rect mLayoutRectIndicator = new Rect();

	public SwipeOutLayout(Context context) {
		super(context);
		init(context, null);
	}

	public SwipeOutLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public SwipeOutLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		//Read in attrs
		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SwipeOutLayout, 0, 0);

			if (ta.hasValue(R.styleable.SwipeOutLayout_swipeOutDirection)) {
				mSwipeDirection = Direction.values()[ta.getInt(R.styleable.SwipeOutLayout_swipeOutDirection,
						mSwipeDirection.ordinal())];
				mSwipeEnabled = true;
			}

			mContentResId = ta.getResourceId(R.styleable.SwipeOutLayout_swipeOutContentId, mContentResId);
			mIndicatorResId = ta.getResourceId(R.styleable.SwipeOutLayout_swipeOutIndicatorId, mIndicatorResId);

			mSwipeEnabled = ta.getBoolean(R.styleable.SwipeOutLayout_swipeOutEnabled, mSwipeEnabled);

			ta.recycle();
		}

	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mContentView = Ui.findView(this, mContentResId);
		mSwipeOutView = Ui.findView(this, mIndicatorResId);

		if (mContentView == null || mSwipeOutView == null || getChildCount() != 2) {
			throw new RuntimeException(
					"SwipeOutLayout must be defined with exactly two children having ids: R.id.swipe_out_content and R.id.swipe_out_indicator (custom ids are possible if set via attributes)");
		}

		mContentView.bringToFront();
		mContentView.setOnTouchListener(new SwipeOutTouchListener());
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		//We measure the children, taking into account padding
		int childMeasureSpecW = widthMeasureSpec;
		int childMeasureSpecH = heightMeasureSpec;
		if (MeasureSpec.getMode(childMeasureSpecW) != MeasureSpec.UNSPECIFIED) {
			childMeasureSpecW = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(childMeasureSpecW) - getPaddingLeft()
					- getPaddingRight(), MeasureSpec.getMode(childMeasureSpecW));
		}
		if (MeasureSpec.getMode(childMeasureSpecH) != MeasureSpec.UNSPECIFIED) {
			childMeasureSpecH = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(childMeasureSpecH) - getPaddingTop()
					- getPaddingBottom(), MeasureSpec.getMode(childMeasureSpecH));
		}
		measureChildren(childMeasureSpecW, childMeasureSpecH);

		//We set our dimensions based on the size of the children
		int width, height;
		if (mSwipeDirection == Direction.NORTH || mSwipeDirection == Direction.SOUTH) {
			//Vertical
			height = mContentView.getMeasuredHeight() + mSwipeOutView.getMeasuredHeight();
			width = mContentView.getMeasuredWidth();
		}
		else {
			//horizontal
			height = mContentView.getMeasuredHeight();
			width = mContentView.getMeasuredWidth() + mSwipeOutView.getMeasuredWidth();
		}

		width = width + getPaddingRight() + getPaddingLeft();
		height = height + getPaddingTop() + getPaddingBottom();

		setMeasuredDimension(width, height);
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int pL = getPaddingLeft();
		int pR = getPaddingRight();
		int pT = getPaddingTop();
		int pB = getPaddingBottom();

		int width = right - left;
		int height = bottom - top;
		int widthNoPad = width - pL - pR;
		int heightNoPad = height - pT - pB;

		int swipeOutHeight = mSwipeOutView.getMeasuredHeight();
		int swipeOutWidth = mSwipeOutView.getMeasuredWidth();

		switch (mSwipeDirection) {
		case NORTH: {
			mLayoutRectContent.left = pL;
			mLayoutRectContent.right = pL + widthNoPad;
			mLayoutRectContent.top = pT + swipeOutHeight;
			mLayoutRectContent.bottom = pT + swipeOutHeight + heightNoPad;

			mLayoutRectIndicator.left = pL;
			mLayoutRectIndicator.right = pL + widthNoPad;
			mLayoutRectIndicator.top = pT + heightNoPad - swipeOutHeight;
			mLayoutRectIndicator.bottom = pT + heightNoPad;

			mMaxSlideOutDistance = swipeOutHeight;
			break;
		}
		case SOUTH: {
			mLayoutRectContent.left = pL;
			mLayoutRectContent.right = pL + widthNoPad;
			mLayoutRectContent.top = pT;
			mLayoutRectContent.bottom = pT + heightNoPad;

			mLayoutRectIndicator.left = pL;
			mLayoutRectIndicator.right = pL + widthNoPad;
			mLayoutRectIndicator.top = pT;
			mLayoutRectIndicator.bottom = pT + swipeOutHeight;

			mMaxSlideOutDistance = swipeOutHeight;
			break;
		}
		case EAST: {
			mLayoutRectContent.left = pL;
			mLayoutRectContent.right = pL + widthNoPad;
			mLayoutRectContent.top = pT;
			mLayoutRectContent.bottom = pT + heightNoPad;

			mLayoutRectIndicator.left = pL;
			mLayoutRectIndicator.right = pL + swipeOutWidth;
			mLayoutRectIndicator.top = pT;
			mLayoutRectIndicator.bottom = pT + heightNoPad;

			mMaxSlideOutDistance = swipeOutWidth;
			break;
		}
		case WEST: {
			mLayoutRectContent.left = pL + swipeOutWidth;
			mLayoutRectContent.right = pL + swipeOutWidth + widthNoPad;
			mLayoutRectContent.top = pT;
			mLayoutRectContent.bottom = pT + heightNoPad;

			mLayoutRectIndicator.left = pL + widthNoPad - swipeOutWidth;
			mLayoutRectIndicator.right = pL + widthNoPad;
			mLayoutRectIndicator.top = pT;
			mLayoutRectIndicator.bottom = pT + heightNoPad;

			mMaxSlideOutDistance = swipeOutWidth;

			break;
		}
		}
		mContentView.layout(mLayoutRectContent.left, mLayoutRectContent.top, mLayoutRectContent.right,
				mLayoutRectContent.bottom);
		mSwipeOutView.layout(mLayoutRectIndicator.left, mLayoutRectIndicator.top, mLayoutRectIndicator.right,
				mLayoutRectIndicator.bottom);
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		clearListeners();
	}

	/*
	 * ACCESSORS
	 */

	public float getSwipeOutDistance() {
		return mMaxSlideOutDistance;
	}

	public Direction getSwipeDirection() {
		return mSwipeDirection;
	}

	public boolean getSwipeEnabled() {
		return mSwipeEnabled;
	}

	public boolean getAlwaysSnapBack() {
		return mAlwaysSnapBack;
	}

	public View getContentView() {
		return mContentView;
	}

	public View getSwipeOutView() {
		return mSwipeOutView;
	}

	/*
	 * MUTATORS
	 */
	public void setSwipeEnabled(boolean enabled) {
		mSwipeEnabled = enabled;
	}

	public void setAlwaysSnapBack(boolean alwaysSnapBack) {
		mAlwaysSnapBack = alwaysSnapBack;
	}

	/*
	 * LISTENER STUFF
	 */

	public static final int SWIPE_STATE_IDLE = 0;
	public static final int SWIPE_STATE_DRAGGING = 1;

	private int mLastReportedSwipeState = SWIPE_STATE_IDLE;
	private float mLastReportedMovePercentage = -1;

	public interface ISwipeOutListener {

		public void onSwipeStateChange(int oldState, int newState);

		public void onSwipeUpdate(float percentage);

		public void onSwipeAllTheWay();
	}

	public void addListener(ISwipeOutListener listener) {
		mListeners.add(listener);
	}

	public void removeListener(ISwipeOutListener listener) {
		mListeners.remove(listener);
	}

	public void clearListeners() {
		mListeners.clear();
	}

	private void reportSwipeStateChanged(int newState) {
		if (newState != mLastReportedSwipeState) {
			Log.d("SwipeOut - reportSwipeStateChanged(" + newState + ")");
			for (ISwipeOutListener listener : mListeners) {
				listener.onSwipeStateChange(mLastReportedSwipeState, newState);
			}
			mLastReportedSwipeState = newState;
		}
	}

	private void reportSwipeUpdate(float percentage) {
		if (percentage != mLastReportedMovePercentage) {
			Log.d("SwipeOut - reportSwipeUpdate(" + percentage + ")");
			for (ISwipeOutListener listener : mListeners) {
				listener.onSwipeUpdate(percentage);
			}
			mLastReportedMovePercentage = percentage;
		}
	}

	private void reportSwipeAllTheWay() {
		Log.d("SwipeOut - reportSwipeAllTheWay");
		for (ISwipeOutListener listener : mListeners) {
			listener.onSwipeAllTheWay();
		}
	}

	/*
	 * TOUCH HANDLING
	 */

	private class SwipeOutTouchListener implements OnTouchListener {

		private float mTouchDownPos = 0;//where we touched down (x for east,west - y for north,south)
		private boolean mVertical = false;//is north/south?
		private boolean mPositiveDirection = false;//should x/y be growing when you drag? true for east and south.

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (!mSwipeEnabled) {
				return false;
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				switch (mSwipeDirection) {
				case NORTH: {
					mVertical = true;
					mPositiveDirection = false;
					break;
				}
				case SOUTH: {
					mVertical = true;
					mPositiveDirection = true;
					break;
				}
				case EAST: {
					mVertical = false;
					mPositiveDirection = true;
					break;
				}
				case WEST: {
					mVertical = false;
					mPositiveDirection = false;
					break;
				}
				}

				mTouchDownPos = getTouchDownPos(event);
				reportSwipeStateChanged(SWIPE_STATE_DRAGGING);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				float offset = getTouchOffset(event);
				float sanatized = sanatizeOffset(offset);
				float percentage = getPercentageFromOffset(sanatized);

				setTranslation(sanatized);
				reportSwipeUpdate(percentage);

				break;
			}
			case MotionEvent.ACTION_CANCEL: {
				setTranslation(0);
				reportSwipeStateChanged(SWIPE_STATE_IDLE);
				resetVars();
				break;
			}
			case MotionEvent.ACTION_UP: {
				float offset = sanatizeOffset(getTouchOffset(event));
				float percentage = getPercentageFromOffset(offset);
				if (percentage == 1) {
					reportSwipeUpdate(1);
					if (mAlwaysSnapBack) {
						setTranslation(0);
					}
					else {
						setTranslation(offset);
					}
					reportSwipeAllTheWay();
				}
				else {
					reportSwipeUpdate(0);
					setTranslation(0);
				}
				reportSwipeStateChanged(SWIPE_STATE_IDLE);

				resetVars();
				break;
			}
			}
			return true;
		}

		private void resetVars() {
			mTouchDownPos = 0;
			mVertical = false;
			mPositiveDirection = false;
		}

		private float getPercentageFromOffset(float offset) {
			return Math.abs(offset) / mMaxSlideOutDistance;
		}

		private float sanatizeOffset(float offset) {
			if (mPositiveDirection && offset < 0) {
				return 0;
			}
			else if (!mPositiveDirection && offset > 0) {
				return 0;
			}

			float absOffset = Math.abs(offset);
			if (absOffset > mMaxSlideOutDistance) {
				if (offset < 0) {
					return -mMaxSlideOutDistance;
				}
				else {
					return mMaxSlideOutDistance;
				}
			}

			return offset;
		}

		private void setTranslation(float tanslation) {
			if (mVertical) {
				mContentView.setTranslationY(tanslation);
			}
			else {
				mContentView.setTranslationX(tanslation);
			}
		}

		private float getTouchDownPos(MotionEvent event) {
			if (mVertical) {
				return event.getY();
			}
			else {
				return event.getX();
			}
		}

		private float getTouchOffset(MotionEvent event) {
			if (mVertical) {
				return event.getY() - mTouchDownPos;
			}
			else {
				return event.getX() - mTouchDownPos;
			}
		}

	}

}
