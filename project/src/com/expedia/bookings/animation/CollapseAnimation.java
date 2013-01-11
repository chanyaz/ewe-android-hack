package com.expedia.bookings.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CollapseAnimation extends Animation {
	private View mView;
	private int mViewHeight;

	public CollapseAnimation(View view) {
		setDuration(200);

		mView = view;
		mViewHeight = view.getLayoutParams().height;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);

		if (interpolatedTime < 1) {
			mView.getLayoutParams().height = mViewHeight - (int) (mViewHeight * interpolatedTime);
			mView.requestLayout();
		}
		else {
			mView.getLayoutParams().height = 0;
			mView.setVisibility(View.GONE);
			mView.requestLayout();
			mView.getLayoutParams().height = mViewHeight;
		}
	}

	@Override
	public void cancel() {
		super.cancel();
		mView.getLayoutParams().height = mViewHeight;
	}
}