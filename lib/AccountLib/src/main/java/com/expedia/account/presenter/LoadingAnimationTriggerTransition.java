package com.expedia.account.presenter;

public class LoadingAnimationTriggerTransition extends Presenter.Transition {

	public interface AnimationController {
		void stopAnimation();

		void startAnimation();
	}

	private boolean forwardIsLoading;
	private AnimationController animationController;
	private boolean animateOnStart;

	public LoadingAnimationTriggerTransition(boolean forwardIsLoading,
		boolean animateOnStart, AnimationController controller) {
		this.forwardIsLoading = forwardIsLoading;
		this.animationController = controller;
		this.animateOnStart = animateOnStart;
	}

	@Override
	public void startTransition(boolean forward) {
		forward = isReallyForward(forward);
		if (!forward) {
			animationController.stopAnimation();
		}
		else if (animateOnStart) {
			animationController.startAnimation();
		}
	}

	@Override
	public void finalizeTransition(boolean forward) {
		forward = isReallyForward(forward);
		if (forward) {
			animationController.startAnimation();
		}
	}

	protected boolean isReallyForward(boolean forward) {
		return (!forward ^ forwardIsLoading);
	}
}
