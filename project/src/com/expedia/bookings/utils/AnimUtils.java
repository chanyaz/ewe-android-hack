package com.expedia.bookings.utils;

import java.util.Collection;
import java.util.Stack;

import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;

public class AnimUtils {

	private static final float[] FADE_IN = new float[] { 0, 1 };
	private static final float[] FADE_OUT = new float[] { 1, 0 };

	private static PropertyValuesHolder sFadeEnterPvh;
	private static PropertyValuesHolder sFadeExitPvh;

	public static Animator createFadeAnimator(View v, boolean enter) {
		return ObjectAnimator.ofFloat(v, "alpha", enter ? FADE_IN : FADE_OUT);
	}

	public static PropertyValuesHolder createFadePropertyValuesHolder(boolean enter) {
		if (enter) {
			if (sFadeEnterPvh == null) {
				sFadeEnterPvh = PropertyValuesHolder.ofFloat("alpha", FADE_IN);
			}
			return sFadeEnterPvh;
		}
		else {
			if (sFadeExitPvh == null) {
				sFadeExitPvh = PropertyValuesHolder.ofFloat("alpha", FADE_OUT);
			}
			return sFadeExitPvh;
		}
	}

	/**
	 * Backwards-compatible method for creating a PropertyValuesHolder ObjectAnimator.
	 * 
	 * Without wrapping the View first, you can't use PropertyValuesHolder with
	 * NineOldAndroid's back-compat methods.
	 */
	public static ObjectAnimator ofPropertyValuesHolder(Object target, PropertyValuesHolder... values) {
		if (AnimatorProxy.NEEDS_PROXY && target instanceof View && !(target instanceof AnimatorProxy)) {
			target = AnimatorProxy.wrap((View) target);
		}

		return ObjectAnimator.ofPropertyValuesHolder(target, values);
	}

	public static AnimatorSet playTogether(Collection<Animator> items) {
		AnimatorSet set = new AnimatorSet();
		set.playTogether(items);
		return set;
	}

	public static AnimatorSet playTogether(Animator... items) {
		AnimatorSet set = new AnimatorSet();
		set.playTogether(items);
		return set;
	}

	public static void reverseAnimator(Animator animator) {
		Stack<Animator> stack = new Stack<Animator>();
		stack.add(animator);

		while (!stack.isEmpty()) {
			Animator anim = stack.pop();

			if (anim instanceof ValueAnimator) {
				((ValueAnimator) anim).reverse();
			}
			else if (anim instanceof AnimatorSet) {
				stack.addAll(((AnimatorSet) anim).getChildAnimations());
			}
		}
	}

	public static void reverseAnimator(android.animation.Animator animator) {
		Stack<android.animation.Animator> stack = new Stack<android.animation.Animator>();
		stack.add(animator);

		while (!stack.isEmpty()) {
			android.animation.Animator anim = stack.pop();

			if (anim instanceof android.animation.ValueAnimator) {
				((android.animation.ValueAnimator) anim).reverse();
			}
			else if (anim instanceof android.animation.AnimatorSet) {
				stack.addAll(((android.animation.AnimatorSet) anim).getChildAnimations());
			}
		}
	}

	/**
	 * Creates an animation bundle for an Activity scale animation.
	 * @param v - view for animation reference frame
	 * @return
	 */
	public static Bundle createActivityScaleBundle(View v) {
		return ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle();
	}

}
