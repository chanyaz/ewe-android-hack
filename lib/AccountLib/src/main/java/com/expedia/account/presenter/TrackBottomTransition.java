package com.expedia.account.presenter;

import android.view.View;

public class TrackBottomTransition extends Presenter.Transition {

	private View vOverview;
	private View vMovingView;

	public TrackBottomTransition(View topView, View goesUnderTheOtherView) {
		vOverview = topView;
		vMovingView = goesUnderTheOtherView;
	}

	@Override
	public void startTransition(boolean forward) {
		putInPlace();
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		putInPlace();
	}

	@Override
	public void finalizeTransition(boolean forward) {
		putInPlace();
	}

	private void putInPlace() {
		vMovingView.setTranslationY(vOverview.getHeight() * vOverview.getScaleY());
	}
}
