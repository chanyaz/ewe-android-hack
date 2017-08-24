package com.expedia.bookings.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;

public abstract class GalleryScrollView extends CustomScrollerScrollView {

	private ValueAnimator mAnimator;

	public GalleryScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public abstract int getInitialScrollTop();

	protected void animateScrollY(int from, int to) {
		if (mAnimator != null && mAnimator.isRunning()) {
			return;
		}
		if (from == to) {
			return;
		}

		mAnimator = ObjectAnimator.ofInt(this, "scrollY", from, to).setDuration(200);
		mAnimator.start();
	}

	protected void animateScrollY(int from, int to, Long duration) {
		if (mAnimator != null && mAnimator.isRunning()) {
			return;
		}
		if (from == to) {
			return;
		}

		mAnimator = ObjectAnimator.ofInt(this, "scrollY", from, to).setDuration(duration);
		mAnimator.start();
	}

	/**
	 * Created with a number of points to be interpreted as a segmented linear function.
	 * Will return the expected y value for any passed x value. If the "x" value passed
	 * in is outside the range of the given x values, then the first and last segments
	 * will be extended to meet that value.
	 *
	 */
	public static class SegmentedLinearInterpolator {
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
				if (x >= x1 && x <= x2 || (i == 0 && x < x1) || (i == mPoints.length - 2 && x > x2)) {
					return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
				}
			}
			return 0f;
		}
	}
}
