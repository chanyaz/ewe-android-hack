package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
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
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class SwipeOutLayout extends FrameLayout {

	public enum Direction {
		NORTH, SOUTH, EAST, WEST
	}

	private ArrayList<ISwipeOutListener> mListeners = new ArrayList<ISwipeOutListener>();

	private View mContentView;
	private View mSwipeOutView;

	private int mContentResId = R.id.swipe_out_content;
	private int mIndicatorResId = R.id.swipe_out_indicator;

	private float mSwipeOutThreshold = 0.5f;//We will report swipeAllTheWay if we let go and are beyond this threshold
	private float mMaxSlideOutDistance;
	private boolean mVertical = false;//is north/south?
	private boolean mPositiveDirection = false;//should x/y be growing when you drag? true for east and south.
	private Direction mSwipeDirection = Direction.NORTH;
	private boolean mSwipeEnabled = false;
	private boolean mAlwaysSnapBack = true;

	private SwipeOutTouchListener mTouchListener;

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
				Direction direction = Direction.values()[ta.getInt(R.styleable.SwipeOutLayout_swipeOutDirection,
						mSwipeDirection.ordinal())];
				setSwipeDirection(direction);
				mSwipeEnabled = true;
			}

			mContentResId = ta.getResourceId(R.styleable.SwipeOutLayout_swipeOutContentId, mContentResId);
			mIndicatorResId = ta.getResourceId(R.styleable.SwipeOutLayout_swipeOutIndicatorId, mIndicatorResId);

			mSwipeEnabled = ta.getBoolean(R.styleable.SwipeOutLayout_swipeOutEnabled, mSwipeEnabled);

			ta.recycle();
		}
		mTouchListener = new SwipeOutTouchListener(context);
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
		setOnTouchListener(mTouchListener);
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
		boolean isVertical = mSwipeDirection == Direction.NORTH || mSwipeDirection == Direction.SOUTH;

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

		mMaxSlideOutDistance = isVertical ? swipeOutHeight : swipeOutWidth;

		//We begin by giving our rects the base top and left padding
		mLayoutRectContent.left = pL;
		mLayoutRectContent.right = pL;
		mLayoutRectContent.top = pT;
		mLayoutRectContent.bottom = pT;

		mLayoutRectIndicator.left = pL;
		mLayoutRectIndicator.right = pL;
		mLayoutRectIndicator.top = pT;
		mLayoutRectIndicator.bottom = pT;

		//The right/top are easy to figure out depending on swipe orientation 
		if (isVertical) {
			mLayoutRectContent.right += widthNoPad;
			mLayoutRectIndicator.right += widthNoPad;
		}
		else {
			mLayoutRectContent.bottom += heightNoPad;
			mLayoutRectIndicator.bottom += heightNoPad;
		}

		//The remaining values to compute are direction specific
		switch (mSwipeDirection) {
		case NORTH: {
			mLayoutRectContent.top += swipeOutHeight;
			mLayoutRectContent.bottom += swipeOutHeight + heightNoPad;
			mLayoutRectIndicator.top += heightNoPad - swipeOutHeight;
			mLayoutRectIndicator.bottom += heightNoPad;
			break;
		}
		case SOUTH: {
			mLayoutRectContent.bottom += heightNoPad;
			mLayoutRectIndicator.bottom += swipeOutHeight;
			break;
		}
		case EAST: {
			mLayoutRectContent.right += widthNoPad;
			mLayoutRectIndicator.right += swipeOutWidth;
			break;
		}
		case WEST: {
			mLayoutRectContent.left += swipeOutWidth;
			mLayoutRectContent.right += swipeOutWidth + widthNoPad;
			mLayoutRectIndicator.left += widthNoPad - swipeOutWidth;
			mLayoutRectIndicator.right += widthNoPad;
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

	public float getSwipeOutThresholdPercentage() {
		return mSwipeOutThreshold;
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

	public void setSwipeOutThresholdPercentage(float percentage) {
		mSwipeOutThreshold = percentage;
	}

	public void setSwipeDirection(Direction direction) {
		mSwipeDirection = direction;
		if (mSwipeDirection == Direction.NORTH || mSwipeDirection == Direction.SOUTH) {
			mVertical = true;
		}
		else {
			mVertical = false;
		}
		if (mSwipeDirection == Direction.EAST || mSwipeDirection == Direction.SOUTH) {
			mPositiveDirection = true;
		}
		else {
			mPositiveDirection = false;
		}
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
		private float mCurrentDistance = 0f;
		private boolean mHasDown = false;
		private GestureDetector mGesDet;

		public SwipeOutTouchListener(Context context) {
			mGesDet = new GestureDetector(context, mListener);
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//If swiping out is disabled, we ignore touch events
			if (!mSwipeEnabled) {
				return false;
			}

			//We only care about touch events that fall on top of our content view
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mHasDown = true;
				float x = event.getX();
				float y = event.getY();
				if (mVertical) {
					if (y < mContentView.getY() || (mPositiveDirection && y > (getHeight() - getSwipeOutDistance()))) {
						mHasDown = false;
					}
				}
				else {
					if (x < mContentView.getX() || (mPositiveDirection && x > (getWidth() - getSwipeOutDistance()))) {
						mHasDown = false;
					}
				}
			}

			//If our down event fell on top of our content view, we do scroll stuff.
			if (mHasDown) {
				//On down we need to init and fire our listeners
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					reportSwipeStateChanged(SWIPE_STATE_DRAGGING);
				}

				//Do scroll related stuff.
				mGesDet.onTouchEvent(event);

				//clean up and fire listeners
				if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
					if (getPercentageFromOffset(mCurrentDistance) >= mSwipeOutThreshold) {
						reportSwipeAllTheWay();
						if (mAlwaysSnapBack) {
							setTranslation(0);
						}
					}
					else {
						setTranslation(0);
					}

					reportSwipeStateChanged(SWIPE_STATE_IDLE);
					resetVars();
				}

				return true;
			}
			return false;
		}

		private SimpleOnGestureListener mListener = new GestureDetector.SimpleOnGestureListener() {
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				if (mVertical) {
					mCurrentDistance = sanitizeOffset(mCurrentDistance - distanceY);
				}
				else {
					mCurrentDistance = sanitizeOffset(mCurrentDistance - distanceX);
				}
				setTranslation(mCurrentDistance);
				reportSwipeUpdate(getPercentageFromOffset(mCurrentDistance));
				return true;
			}
		};

		private void resetVars() {
			mCurrentDistance = 0;
			mHasDown = false;
		}

		private float getPercentageFromOffset(float offset) {
			return Math.abs(offset) / mMaxSlideOutDistance;
		}

		private float sanitizeOffset(float offset) {
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

		private void setTranslation(float translation) {
			if (mVertical) {
				mContentView.setTranslationY(translation);
			}
			else {
				mContentView.setTranslationX(translation);
			}
		}

	}
}
