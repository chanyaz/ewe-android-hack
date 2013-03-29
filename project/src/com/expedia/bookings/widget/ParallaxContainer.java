package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;

import com.expedia.bookings.R;

/**
 * Use this widget to give a parallax effect to a view. Wrap the desired view with this
 * container (which extends FrameLayout).
 *
 * @author doug@mobiata.com
 */
public class ParallaxContainer extends FrameLayout {

	private int[] mLocation = new int[2];
	private SegmentedLinearInterpolator mInterpolator;
	private float mOffsetTop = 0;
	private float mOffsetBottom = 0;
	private float mScrollMin = 0;
	private float mScrollMax = 0;

	public ParallaxContainer(Context context) {
		super(context);
		init(context, null, 0);
	}

	public ParallaxContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public ParallaxContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ParallaxContainer, 0, 0);
			mOffsetTop = ta.getDimension(R.styleable.ParallaxContainer_offsetTop, 0);
			mOffsetBottom = ta.getDimension(R.styleable.ParallaxContainer_offsetBottom, 0);
			mScrollMin = ta.getDimension(R.styleable.ParallaxContainer_scrollMin, 0);
			mScrollMax = ta.getDimension(R.styleable.ParallaxContainer_scrollMax, 0);
			ta.recycle();
		}

		hook();
		getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		hook();
		getLocationOnScreen(mLocation);
		buildInterpolator(b - t);
		parallax();
	}

	/**
	 * Adjusts this container's scrollY to get a nice parallax effect
	 */
	public void parallax() {
		if (mInterpolator == null) {
			return;
		}

		int counterscroll = (int) (mInterpolator.get(mLocation[1]));
		scrollTo(0, counterscroll);
	}

	private ScrollView.OnScrollListener mOnScrollListener = new ScrollView.OnScrollListener() {
		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			getLocationOnScreen(mLocation);
			parallax();
		}
	};

	// TODO: find a better way to be notified when the position of this view changes.
	private OnGlobalLayoutListener mLayoutListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			// TODO: this is called many times per second. why?
			int old = mLocation[1];
			getLocationOnScreen(mLocation);
			if (mLocation[1] != old) {
				parallax();
			}
		}
	};

	/**
	 * This function hooks into all ancestor onScrollListener's to grab a notification for
	 * when the scroll changes, so that we're sure to handle it.
	 */
	private void hook() {
		ViewParent parent = this.getParent();
		while (parent != null) {
			if (parent instanceof ScrollView) {
				((ScrollView) parent).addOnScrollListener(mOnScrollListener);
			}
			parent = parent.getParent();
		}
	}

	private void buildInterpolator(int thisHeight) {
		// If mLocation were between minVisiblePosition and maxVisiblePostion,
		// then this view would still be visible on the screen.
		float minVisiblePosition = mOffsetTop - thisHeight;
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		float maxVisiblePosition = wm.getDefaultDisplay().getHeight() - mOffsetBottom;

		PointF p1 = new PointF(minVisiblePosition, mScrollMin);
		PointF p2 = new PointF(maxVisiblePosition, mScrollMax);
		mInterpolator = new SegmentedLinearInterpolator(p1, p2);
	}

	/**
	 * Created with a number of points to be interpreted as a segmented linear function.
	 * Will return the expected y value for any passed x value. If the "x" value passed
	 * in is outside the range of the given x values, then the first and last segments
	 * will be extended to meet that value.
	 *
	 * i.e., it will produce a function that looks like this:
	 *
	 *         _______
	 * _______/
	 *
	 */
	private static class SegmentedLinearInterpolator {
		PointF[] mPoints;

		public SegmentedLinearInterpolator(PointF... points) {
			mPoints = points;
		}

		public float get(float x) {
			for (int i = 0; i <= mPoints.length - 2; i++) {
				float x1 = mPoints[i].x;
				float x2 = mPoints[i + 1].x;
				float y1 = mPoints[i].y;
				float y2 = mPoints[i + 1].y;
				if (x >= x1 && x <= x2 || (i == 0 && x < x1) || (i == mPoints.length - 2 && x > x2)) {
					return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
				}
			}
			return 0f;
		}
	}

}
