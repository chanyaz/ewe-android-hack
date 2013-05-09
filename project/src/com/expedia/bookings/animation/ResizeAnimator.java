package com.expedia.bookings.animation;

import android.view.View;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * This class contains static helper methods for building ValueAnimator objects that mimic ResizeAnimation.java
 * Note: We see we are using a ValueAnimator and calling getLayoutParams().height = ... this is important
 * 		 Creating an ObjectAnimator on the "height" property does not do the job, we need the requestlayout so that
 * 		 things get measured and can move other things around (in itins case, this happens in the listview, so rows need move)
 *
 */
public class ResizeAnimator {
	public static ValueAnimator buildResizeAnimator(View view, int endHeight) {
		return buildResizeAnimator(view, view.getHeight(), endHeight);
	}

	public static ValueAnimator buildResizeAnimator(final View view, int startHeight, int endHeight) {
		view.setVisibility(View.VISIBLE);

		ValueAnimator anim = ValueAnimator.ofInt(startHeight, endHeight);
		anim.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				view.getLayoutParams().height = (Integer) arg0.getAnimatedValue();
				view.requestLayout();
			}
		});

		anim.setDuration(400);
		return anim;
	}
}
