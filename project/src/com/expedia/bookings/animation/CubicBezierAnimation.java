package com.expedia.bookings.animation;

import com.mobiata.android.Log;

/**
 * Use this class to generate smooth curves in x,y space. This class was inspired by a very informative
 * blog post written by Chet Haase. The basic idea is that smooth curves can easily be computed using
 * Bezier Curves. A Bezier curve consists of N points. A cubic Bezier curve consists of a start and end
 * point along with two control points that define the curve.
 *
 * This class encapsulates the bookkeeping necessary to interpolate x and y, given t. Construct an instance
 * of this class, by feeding it start and end points. Use a static factory method that handles the creation
 * of the control points, and provides an easy to use public interface. Then you can call "getXInterpolator"
 * and "getYInterpolator" to return a class that can interpolate a point given t, for easy use within a
 * ValueAnimator.
 *
 * Informative links:
 * http://graphics-geek.blogspot.com/2012/01/curved-motion-in-android.html
 * http://www.planetclegg.com/projects/WarpingTextToSplines.html
 * http://en.wikipedia.org/wiki/B%C3%A9zier_curve
 */
public class CubicBezierAnimation {

	private int mStartX;
	private int mStartY;

	private int mEndX;
	private int mEndY;

	private CubicBezierInterpolator mXInterpolator;
	private CubicBezierInterpolator mYInterpolator;

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Static factory methods to generate well-known animations

	/**
	 * Constructs a cubic bezier curve with the given View's top/left coordinates as start and end
	 * coordinates. The control points given to the interpolator describe a curve that matches the
	 * "08A - Add Hotel" animation from the mocks.
	 *
	 * Give a start and end point, the computation of control points defines the curve. The first
	 * step to constructing the control points is to identify the curve given by design. I then
	 * construct this curve on http://www.jasondavies.com/animated-bezier/ by fiddling with the
	 * start, end and control points until the desired curve is drawn.
	 *
	 */

	public static CubicBezierAnimation newOutsideInAnimation(int startX, int startY, int endX, int endY) {
		CubicBezierAnimation bez = new CubicBezierAnimation(startX, startY, endX, endY);

		int deltaX = endX - startX;
		int deltaY = endY - startY;
		float c0x = startX - 0.865f * deltaX;
		float c0y = startY + 0.525f * deltaY;
		float c1x = startX + 0.865f * deltaX;
		float c1y = startY + 0.900f * deltaY;

		Log.d("CubicBezier: start=" + startX + "," + startY);
		Log.d("CubicBezier: control0=" + c0x + "," + c0y);
		Log.d("CubicBezier: control1=" + c1x + "," + c1y);
		Log.d("CubicBezier: end=" + endX + "," + endY);

		bez.setControlPoints(c0x, c0y, c1x, c1y);

		return bez;
	}

	public CubicBezierInterpolator getXInterpolator() {
		return mXInterpolator;
	}

	public CubicBezierInterpolator getYInterpolator() {
		return mYInterpolator;
	}

	private CubicBezierAnimation(int startX, int startY, int endX, int endY) {
		mStartX = startX;
		mStartY = startY;
		mEndX = endX;
		mEndY = endY;
	}

	private void setControlPoints(float c0x, float c0y, float c1x, float c1y) {
		mXInterpolator = new CubicBezierInterpolator(mStartX, c0x, c1x, mEndX);
		mYInterpolator = new CubicBezierInterpolator(mStartY, c0y, c1y, mEndY);
	}

	public static class CubicBezierInterpolator {

		private float mStart;
		private float mControl0;
		private float mControl1;
		private float mEnd;

		public CubicBezierInterpolator(float zero, float one, float two, float three) {
			mStart = zero;
			mControl0 = one;
			mControl1 = two;
			mEnd = three;
		}

		public float interpolate(float t) {
			float oneMinusT = 1 - t;
			return oneMinusT * oneMinusT * oneMinusT * mStart +
					3 * oneMinusT * oneMinusT * t * mControl0 +
					3 * oneMinusT * t * t * mControl1 +
					t * t * t * mEnd;
		}

	}

}
