package com.expedia.bookings.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ExpandAnimation extends Animation {
	private View mView;

	private int mStartHeight;
	private int mEndHeight;

	public ExpandAnimation(View view) {
		this(view, 0, view.getLayoutParams().height);
	}

	public ExpandAnimation(View view, int endHeight) {
		this(view, view.getLayoutParams().height, endHeight);
	}

	public ExpandAnimation(View view, int startHeight, int endHeight) {
		setDuration(200);

		mView = view;
		mStartHeight = startHeight;
		mEndHeight = endHeight;

		mView.getLayoutParams().height = startHeight;
		mView.setVisibility(View.VISIBLE);
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
	}

	@Override
	public void cancel() {
		super.cancel();
		mView.getLayoutParams().height = mEndHeight;
	}
}