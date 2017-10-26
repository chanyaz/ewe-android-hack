package com.expedia.account.presenter;

import android.view.View;

import com.expedia.account.util.PresenterUtils;

public class FlipTransition extends Presenter.Transition {

	private View vFirstView;
	private View vSecondView;

	private float mDirection;

	private float mFirstStartRotationY;
	private float mFirstEndRotationY;
	private float mSecondStartRotationY;
	private float mSecondEndRotationY;

	public FlipTransition(View startView, View endView, float direction) {
		vFirstView = startView;
		vSecondView = endView;
		mDirection = direction;
	}

	private float calculateOtherSideOfGoal(float goal, float current) {
		return (2 * goal) - current;
	}

	@Override
	public void startTransition(boolean forward) {
		mFirstStartRotationY = forward ? vFirstView.getRotationY() : -180 * mDirection;
		mFirstEndRotationY = forward ? calculateOtherSideOfGoal(-90 * mDirection, mFirstStartRotationY) : 0;
		mSecondStartRotationY = forward ? 180 * mDirection : vSecondView.getRotationY();
		mSecondEndRotationY = forward ?  0 : calculateOtherSideOfGoal(90 * mDirection, mSecondStartRotationY);
		vFirstView.setVisibility(View.VISIBLE);
		vSecondView.setVisibility(View.VISIBLE);

		if (forward) {
			vFirstView.setAlpha(1f);
			vSecondView.setAlpha(0.0f);
		}
		else {
			vFirstView.setAlpha(0f);
			vSecondView.setAlpha(1.0f);
		}
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		if (forward && f < .50 || !forward && f >= .50) {
			vFirstView.setAlpha(1f);
			vSecondView.setAlpha(0f);
			vFirstView.setRotationY(PresenterUtils.calculateStep(mFirstStartRotationY, mFirstEndRotationY, f));
		}
		else {
			vFirstView.setAlpha(0f);
			vSecondView.setAlpha(1f);
			vSecondView.setRotationY(PresenterUtils.calculateStep(mSecondStartRotationY, mSecondEndRotationY, f));
		}
	}

	@Override
	public void finalizeTransition(boolean forward) {
		if (forward) {
			vFirstView.setVisibility(View.INVISIBLE);
			vSecondView.setVisibility(View.VISIBLE);
		}
		else {
			vFirstView.setVisibility(View.VISIBLE);
			vSecondView.setVisibility(View.INVISIBLE);
		}

		vFirstView.setAlpha(1f);
		vFirstView.setRotationY(0f);
		vSecondView.setAlpha(1f);
		vSecondView.setRotationY(0f);
	}
}
