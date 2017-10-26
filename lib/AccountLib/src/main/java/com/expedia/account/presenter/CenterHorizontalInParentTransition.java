package com.expedia.account.presenter;

import android.view.View;

import com.expedia.account.util.PresenterUtils;

public class CenterHorizontalInParentTransition extends Presenter.Transition {

	private View vTargetView;
	private View vParentView;
	private float mStartTranslationX;
	private float mEndTranslationX;

	public CenterHorizontalInParentTransition(View targetView, View parentView) {
		vTargetView = targetView;
		vParentView = parentView;
	}

	@Override
	public void startTransition(boolean forward) {
		if (forward) {
			mStartTranslationX = 0;
			mEndTranslationX = PresenterUtils
				.getTranslationXForCenter(vTargetView, vParentView);
		}
		else {
			mStartTranslationX = vTargetView.getTranslationX();
			mEndTranslationX = 0;
		}
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		vTargetView.setTranslationX(
			PresenterUtils.calculateStep(mStartTranslationX, mEndTranslationX, f));
	}

	@Override
	public void finalizeTransition(boolean forward) {
		vTargetView.setTranslationX(mEndTranslationX);
	}
}
