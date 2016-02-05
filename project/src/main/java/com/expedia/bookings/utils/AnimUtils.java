package com.expedia.bookings.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.FrameLayout;

import java.util.Collection;
import java.util.Stack;

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

	/**
	 * Creates an animation bundle for an Activity scale animation.
	 * @param v - view for animation reference frame
	 * @return
	 */
	public static Bundle createActivityScaleBundle(View v) {
		return ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle();
	}

	public static void doTheHarlemShake(View v) {
		Animation shake = AnimationUtils.loadAnimation(v.getContext(), R.anim.wiggle);
		v.startAnimation(shake);
	}

	private static final int LOADING_COLOR_LIGHT = Color.parseColor("#D3D4D4");
	private static final int LOADING_COLOR_DARK = Color.parseColor("#848F94");

	public static ValueAnimator setupLoadingAnimation(View v, boolean forward) {
		if (GlobalSettingsUtils.getAnimatorDurationScale(v.getContext()) == 0.0f) {
			v.setBackgroundColor(LOADING_COLOR_LIGHT);
			return null;
		}

		if (forward) {
			return animateBackground(v, LOADING_COLOR_DARK, LOADING_COLOR_LIGHT);
		}
		else {
			return animateBackground(v, LOADING_COLOR_LIGHT, LOADING_COLOR_DARK);
		}
	}

	// This is stateless so we should cache it
	public final static ArgbEvaluator ARGB_EVALUATOR = new ArgbEvaluator();

	private static ValueAnimator animateBackground(final View view, int startColor, int endColor) {
		ValueAnimator animation = ValueAnimator.ofObject(ARGB_EVALUATOR, startColor, endColor);
		animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				view.setBackgroundColor((Integer) animator.getAnimatedValue());
			}

		});
		animation.setRepeatMode(ValueAnimator.REVERSE);
		animation.setRepeatCount(ValueAnimator.INFINITE);
		animation.setDuration(600);
		animation.start();
		return animation;
	}

	public static void rotate(View v) {
		Animation rotate = AnimationUtils.loadAnimation(v.getContext(), R.anim.rotate);
		v.startAnimation(rotate);
	}

	public static void reverseRotate(View v) {
		Animation rotate = AnimationUtils.loadAnimation(v.getContext(), R.anim.rotate_reverse);
		v.startAnimation(rotate);
	}

	public static void slideUp(View v) {
		Animation slideUp = AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_up);
		slideUp.setDuration(100);
		v.startAnimation(slideUp);
	}

	public static void slideDown(View v) {
		Animation slideDown = AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_down);
		slideDown.setDuration(400);
		slideDown.setFillAfter(true);
		v.startAnimation(slideDown);
	}

	public static void fadeIn(View v) {
		Animation fadeIn = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_in);
		v.startAnimation(fadeIn);
	}

	public static void progressForward(View v) {
		Context context = v.getContext();
		final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		final int loadingImgWidth = context.getDrawable(R.drawable.packages_loading_pattern).getIntrinsicWidth();
		int animatedViewWidth = 0;
		while (animatedViewWidth < screenWidth) {
			animatedViewWidth += loadingImgWidth;
		}

		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();
		layoutParams.width = animatedViewWidth + (loadingImgWidth * 3);
		v.setLayoutParams(layoutParams);

		Animation progressAnimation = new TranslateAnimation(-(loadingImgWidth * 2), 0, 0, 0);
		progressAnimation.setInterpolator(new LinearInterpolator());
		progressAnimation.setRepeatCount(Animation.INFINITE);
		progressAnimation.setDuration(1000);
		v.startAnimation(progressAnimation);
	}

	public static AnimatorSet getFadeInRotateAnim(View v) {
		AnimatorSet set = new AnimatorSet();
		set.playTogether(
				ObjectAnimator.ofFloat(v, "rotation", -180f, 0f),
				ObjectAnimator.ofFloat(v, "alpha", 0f, 1f)
		);
		set.setDuration(300);
		set.setInterpolator(new DecelerateInterpolator());
		return set;
	}
}
