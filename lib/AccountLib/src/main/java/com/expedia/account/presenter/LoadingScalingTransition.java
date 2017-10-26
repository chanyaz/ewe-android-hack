package com.expedia.account.presenter;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.expedia.account.util.PresenterUtils;

public class LoadingScalingTransition

	extends LoadingAnimationTriggerTransition {

	private View vLoadingView;
	private boolean handleRollOut;

	//ASSUMPTION: scaleX and scaleY are the same for the target view.
	private Float initialScale;

	private float loadingStartScale;
	private float loadingEndScale;

	OvershootInterpolator interpolator = new OvershootInterpolator();

	public LoadingScalingTransition(View loadingView, boolean forwardIsLoading, boolean handleRollOut,
		boolean animateOnStart, LoadingAnimationTriggerTransition.AnimationController controller, @Nullable Float initialScale) {
		super(forwardIsLoading, animateOnStart, controller);
		this.vLoadingView = loadingView;
		this.handleRollOut = handleRollOut;
		this.initialScale = initialScale;
	}

	@Override
	public void startTransition(boolean forward) {
		super.startTransition(forward);
		if (forward && initialScale == null) {
			initialScale = vLoadingView.getScaleX();
		}
		forward = isReallyForward(forward);
		if (!forward && handleRollOut) {
			loadingStartScale = vLoadingView.getScaleX();
			if (initialScale != null) {
				loadingEndScale = initialScale;
			}
			vLoadingView.setScaleY(loadingStartScale);
			vLoadingView.setScaleX(loadingStartScale);
		}
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		super.updateTransition(f, forward);
		forward = isReallyForward(forward);
		if (!forward && handleRollOut) {
			vLoadingView.setScaleX(
				PresenterUtils
					.calculateStep(loadingStartScale, loadingEndScale, interpolator.getInterpolation(f)));
			vLoadingView.setScaleY(
				PresenterUtils
					.calculateStep(loadingStartScale, loadingEndScale, interpolator.getInterpolation(f)));
		}
	}

	@Override
	public void finalizeTransition(boolean forward) {
		super.finalizeTransition(forward);
		forward = isReallyForward(forward);
		if (!forward && handleRollOut) {
			vLoadingView.setScaleX(loadingEndScale);
			vLoadingView.setScaleY(loadingEndScale);
		}
	}
}
