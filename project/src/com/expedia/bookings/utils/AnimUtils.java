package com.expedia.bookings.utils;

import java.util.Collection;
import java.util.Stack;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

public class AnimUtils {

	private static final float[] FADE_IN = new float[] { 0, 1 };
	private static final float[] FADE_OUT = new float[] { 1, 0 };

	public static Animator createFadeAnimator(View v, boolean enter) {
		return ObjectAnimator.ofFloat(v, "alpha", enter ? FADE_IN : FADE_OUT);
	}

	public static AnimatorSet playTogether(Collection<Animator> items) {
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
}
