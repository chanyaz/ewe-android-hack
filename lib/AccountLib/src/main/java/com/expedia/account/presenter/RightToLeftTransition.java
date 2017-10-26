package com.expedia.account.presenter;

public class RightToLeftTransition extends LeftToRightTransition {

	public RightToLeftTransition(Presenter presenter, String right, String left) {
		super(presenter, left, right);
	}

	@Override
	public void startTransition(boolean forward) {
		super.startTransition(!forward);
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		super.updateTransition(f, !forward);
	}

	@Override
	public void endTransition(boolean forward) {
		super.endTransition(!forward);
		// ignore
	}

	@Override
	public void finalizeTransition(boolean forward) {
		super.finalizeTransition(!forward);
	}
}
