package com.expedia.account.presenter;

import android.view.View;

import com.expedia.account.util.PresenterUtils;

public class ScaleTransition extends Presenter.Transition {

	private View vTargetView;

	private float mStartScale;
	private float mEndScale;

	public ScaleTransition(View target, float startScale, float endScale) {
		vTargetView = target;
		mStartScale = startScale;
		mEndScale = endScale;
	}

	@Override
	public void startTransition(boolean forward) {
		float start = forward ? mStartScale : mEndScale;
		vTargetView.setScaleX(start);
		vTargetView.setScaleY(start);
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		float start = forward ? mStartScale : mEndScale;
		float end = forward ? mEndScale : mStartScale;
		float step = PresenterUtils.calculateStep(start, end, f);
		vTargetView.setScaleY(step);
		vTargetView.setScaleX(step);
	}

	@Override
	public void finalizeTransition(boolean forward) {
		float end = forward ? mEndScale : mStartScale;
		vTargetView.setScaleX(end);
		vTargetView.setScaleY(end);
	}
}
