package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.expedia.bookings.R;

/**
 * Use this widget to give a parallax effect to a view. Wrap the desired view with this
 * container (which extends FrameLayout).
 * <p/>
 * <pre>
 * &lt;com.expedia.bookings.widget.ParallaxContainer
 *     android:layout_width="wrap_content"
 *     android:layout_height="200dp"
 *     app:offsetBottom="80dp"
 *     app:offsetTop="80dp"
 *     app:scrollMax="150dp"
 *     app:scrollMin="10dp" &gt;
 *
 *     &lt;ImageView
 *         android:id="@+id/my_image"
 *         android:layout_width="wrap_content"
 *         android:layout_height="500dp"
 *         android:src="@drawable/tall_image" /&gt;
 * &lt;/com.expedia.bookings.widget.ParallaxContainer&gt;
 * </pre>
 * <p/>
 * <p>As this view is moved about the screen (either from layout changes or from a scroll event),
 * its scrollY will be modified based on its on-screen position. <b>NOTE</b>: ParallaxContainer
 * expects to be a descendant of a {@link com.expedia.bookings.widget.ScrollView}, and may not update
 * itself properly otherwise.</p>
 * <p/>
 * <p>As mentioned, this view will be scrolled depending on its location on the physical screen,
 * using {@link View.getLocationOnScreen(int[])}. If this view isn't expected to make it to the
 * very edge of the screen, {@link #setOffsetTop(int)} and {@link #setOffsetBottom(int)} will
 * adjust a distance away from the edges of the screen.</p>
 * <p/>
 * <p>To set the limits for which this container will be scrolled, use {@link #setScrollMin(int)}
 * and {@link #setScrollMax(int)}. You might try something like 0dp and 300dp (in the above example).
 * Making changes to these can cause a different parallax effect.</p>
 * <p/>
 * <p>These values are interpolated, such that when the bottom of this view is [offsetTop] pixels away
 * from the top of the screen, it will be parallaxed by [scrollMax] pixels, and when the top of
 * this view is [offsetBottom] pixels away from the bottom of the screen, it will be parallaxed by
 * [scrollMin] pixels.
 * </p>
 *
 * @author doug@mobiata.com
 */
public class ParallaxContainer extends FrameLayout {

	private final int[] mLocation = new int[2];
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

	public float getOffsetTop() {
		return mOffsetTop;
	}

	public void setOffsetTop(float offsetTop) {
		this.mOffsetTop = offsetTop;
		invalidate();
	}

	public float getOffsetBottom() {
		return mOffsetBottom;
	}

	public void setOffsetBottom(float offsetBottom) {
		this.mOffsetBottom = offsetBottom;
		invalidate();
	}

	/**
	 * Adjusts this container's scrollY to get a nice parallax effect.
	 * Does nothing if this view is not enabled.
	 */
	public void parallax() {
		if (mInterpolator == null || !isEnabled()) {
			return;
		}

		int counterscroll = (int) (mInterpolator.get(mLocation[1]));
		scrollTo(0, counterscroll);
	}

	private final ScrollView.OnScrollListener mOnScrollListener = new ScrollView.OnScrollListener() {
		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			getLocationOnScreen(mLocation);
			parallax();
		}
	};

	// TODO: find a better way to be notified when the position of this view changes.
	private final OnGlobalLayoutListener mLayoutListener = new OnGlobalLayoutListener() {
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
		float maxVisiblePosition = getResources().getDisplayMetrics().heightPixels - mOffsetBottom;

		PointF p1 = new PointF(minVisiblePosition, mScrollMin);
		PointF p2 = new PointF(maxVisiblePosition, mScrollMax);
		if (minVisiblePosition > maxVisiblePosition) {
			mInterpolator = new SegmentedLinearInterpolator(p2, p1);
		}
		else {
			mInterpolator = new SegmentedLinearInterpolator(p1, p2);
		}
	}

	/**
	 * Created with a number of points to be interpreted as a segmented linear function.
	 * Will return the expected y value for any passed x value. If the "x" value passed
	 * in is outside the range of the given x values, then the first and last segments
	 * will be extended to meet that value.
	 * <p/>
	 * i.e., it will produce a function that looks like this:
	 * <p/>
	 *         _______
	 * _______/
	 */
	private static class SegmentedLinearInterpolator {
		final PointF[] mPoints;

		public SegmentedLinearInterpolator(PointF... points) {
			mPoints = points;
		}

		public float get(float x) {
			for (int i = 0; i <= mPoints.length - 2; i++) {
				float x1 = mPoints[i].x;
				float x2 = mPoints[i + 1].x;
				float y1 = mPoints[i].y;
				float y2 = mPoints[i + 1].y;
				if (i == 0 && x < x1) {
					return y1;
				}
				else if (i == mPoints.length - 2 && x > x2) {
					return y2;
				}
				else if (x >= x1 && x <= x2) {
					return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
				}
			}
			return 0f;
		}
	}

}
