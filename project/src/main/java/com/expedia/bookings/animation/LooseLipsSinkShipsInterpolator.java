package com.expedia.bookings.animation;

import android.view.animation.Interpolator;

public class LooseLipsSinkShipsInterpolator implements Interpolator {
	private final Interpolator mInterpolator;
	private float mLastInput = 0.0f;

	public LooseLipsSinkShipsInterpolator(Interpolator interpolator) {
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
