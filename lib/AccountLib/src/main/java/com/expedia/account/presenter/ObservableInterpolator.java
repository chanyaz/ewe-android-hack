package com.expedia.account.presenter;

import android.view.animation.Interpolator;

public class ObservableInterpolator implements Interpolator {
	private Interpolator mInterpolator;
	private float mLastInput = 0.0f;

	public ObservableInterpolator(Interpolator interpolator) {
		mInterpolator = interpolator;
	}

	@Override
	public float getInterpolation(float input) {
		mLastInput = input;
		return mInterpolator.getInterpolation(input);
	}

	public float getLastInput() {
		return mLastInput;
	}
}
