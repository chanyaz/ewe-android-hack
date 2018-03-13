package com.expedia.bookings.utils;

import java.util.Collection;
import java.util.Stack;

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
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;

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

	public static void slideOut(final View v) {
		Animation slideUp = AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_out);
		slideUp.setDuration(200);
		v.startAnimation(slideUp);
		v.postDelayed(new Runnable() {
			@Override
			public void run() {
				v.setVisibility(View.GONE);
			}
		}, 200L);
	}

	public static void slideIn(View v) {
		Animation slideDown = AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_in);
		slideDown.setDuration(200);
		slideDown.setFillAfter(true);
		v.startAnimation(slideDown);
	}

	public static Animation slideInAbove(final View view, final View child) {
		final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) child.getLayoutParams();
		final int originalTopMargin = params.topMargin;

		Animation slideInAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in);
		slideInAnimation.setDuration(200);
		slideInAnimation.setFillAfter(true);
		slideInAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				params.topMargin += view.getHeight();
				child.setLayoutParams(params);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		view.startAnimation(slideInAnimation);

		Animation slideOutAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_out);
		slideOutAnimation.setDuration(200);
		slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				params.topMargin = originalTopMargin;
				child.setLayoutParams(params);
				view.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		return slideOutAnimation;
	}

	public static void slideInOut(final View view, final int height, Animator.AnimatorListener listener) {
		final long animDuration = 400L;
		final long startDelay = 5000L;

		view.setTranslationY(height);
		view.setVisibility(View.VISIBLE);

		if (!ExpediaBookingApp.isAutomation()) {
			AnimatorSet animatorSet = new AnimatorSet();

			ObjectAnimator objectAnimatorIn = ObjectAnimator.ofFloat(view, "translationY", height, 0);
			objectAnimatorIn.setDuration(animDuration);

			ObjectAnimator objectAnimatorOut = ObjectAnimator.ofFloat(view, "translationY", 0, height);
			objectAnimatorOut.setDuration(animDuration);
			objectAnimatorOut.setStartDelay(startDelay);

			animatorSet.playSequentially(objectAnimatorIn, objectAnimatorOut);
			animatorSet.start();
			if (listener != null) {
				animatorSet.addListener(listener);
			}
		}
	}

	public static void slideInOut(final View view, final int height) {
		slideInOut(view, height, null);
	}

	public static void fadeIn(View v) {
		Animation fadeIn = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_in);
		v.startAnimation(fadeIn);
	}

	public static void progressForward(View v) {
		Context context = v.getContext();
		final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		final int loadingImgWidth = ContextCompat.getDrawable(context, R.drawable.packages_loading_pattern)
			.getIntrinsicWidth();
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
	public static void animateView(ValueAnimator anim, final View view, int fromHeight, int toHeight, Long animDuration, Long startDelay) {
		if (anim != null) {
			anim.cancel();
		}
		anim = ValueAnimator.ofInt(fromHeight, toHeight);
		anim.setDuration(animDuration);
		anim.setStartDelay(startDelay);
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				view.setScrollY((int) valueAnimator.getAnimatedValue());
			}
		});
		anim.start();
	}
}
