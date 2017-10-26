package com.expedia.account.presenter;

import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.expedia.account.util.PresenterUtils;

public class LoadingSpinningTransition extends LoadingAnimationTriggerTransition {

	private View vLoadingView;
	private boolean handleRollOut;

	private float loadingStartRotationY;
	private float loadingEndRotationY;

	OvershootInterpolator interpolator = new OvershootInterpolator();

	public LoadingSpinningTransition(View loadingView, boolean forwardIsLoading, boolean handleRollOut,
		boolean animateOnStart, LoadingAnimationTriggerTransition.AnimationController controller) {
		super(forwardIsLoading, animateOnStart, controller);
		this.vLoadingView = loadingView;
		this.handleRollOut = handleRollOut;
	}

	@Override
	public void startTransition(boolean forward) {
		super.startTransition(forward);
		forward = isReallyForward(forward);
		if (!forward && handleRollOut) {
			loadingStartRotationY = vLoadingView.getRotationY();
			loadingEndRotationY = 0;
			vLoadingView.setRotationY(loadingStartRotationY);
		}
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		super.updateTransition(f, forward);
		forward = isReallyForward(forward);
		if (!forward && handleRollOut) {
			vLoadingView.setRotationY(
				PresenterUtils
					.calculateStep(loadingStartRotationY, loadingEndRotationY, interpolator.getInterpolation(f)));
		}
	}

	@Override
	public void finalizeTransition(boolean forward) {
		super.finalizeTransition(forward);
		forward = isReallyForward(forward);
		if (!forward && handleRollOut) {
			vLoadingView.setRotationY(loadingEndRotationY);
		}
	}
}
