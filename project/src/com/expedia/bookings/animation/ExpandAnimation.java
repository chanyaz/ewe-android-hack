package com.expedia.bookings.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ExpandAnimation extends Animation {
	private View mView;
	private int mViewHeight;

	public ExpandAnimation(View view) {
		setDuration(200);

		mView = view;
		mViewHeight = view.getLayoutParams().height;

		mView.getLayoutParams().height = 0;
		mView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);

		if (interpolatedTime < 1) {
			mView.getLayoutParams().height = (int) (mViewHeight * interpolatedTime);
			mView.requestLayout();
		}
		else {
			mView.getLayoutParams().height = mViewHeight;
			mView.requestLayout();
		}
	}

	@Override
	public void cancel() {
		super.cancel();
		mView.getLayoutParams().height = mViewHeight;
	}
}