package com.expedia.bookings.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeAnimation extends Animation {
	public interface AnimationStepListener {
		public void onAnimationStep(Animation animation, float interpolatedTime);
	}

	private View mView;

	private int mStartHeight;
	private int mEndHeight;

	private AnimationStepListener mAnimationStepListener;

	public ResizeAnimation(View view, int endHeight) {
		this(view, view.getHeight(), endHeight);
	}

	public ResizeAnimation(View view, int startHeight, int endHeight) {
		setDuration(400);

		mView = view;
		mStartHeight = startHeight;
		mEndHeight = endHeight;

		mView.getLayoutParams().height = startHeight;
		mView.setVisibility(View.VISIBLE);
	}

	public void setAnimationStepListener(AnimationStepListener onAnimationStepListener) {
		mAnimationStepListener = onAnimationStepListener;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);

		if (interpolatedTime < 1) {
			mView.getLayoutParams().height = (int) ((mEndHeight - mStartHeight) * interpolatedTime) + mStartHeight;
			mView.requestLayout();
		}
		else {
			mView.getLayoutParams().height = mEndHeight;
			mView.requestLayout();
		}

		if (mAnimationStepListener != null) {
			mAnimationStepListener.onAnimationStep(this, interpolatedTime);
		}
	}

	@Override
	public void cancel() {
		super.cancel();
		mView.getLayoutParams().height = mEndHeight;
	}
}
