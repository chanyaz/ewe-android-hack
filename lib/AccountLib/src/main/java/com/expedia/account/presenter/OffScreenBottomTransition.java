package com.expedia.account.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.WindowManager;

import com.expedia.account.util.PresenterUtils;

public class OffScreenBottomTransition extends Presenter.Transition {

	private View vTargetView;

	private float initialTosTranslationY;

	private float tosStartTranslationY;
	private float tosEndTranslationY;

	public OffScreenBottomTransition(View targetView) {
		vTargetView = targetView;
	}

	@Override
	public void startTransition(boolean forward) {
		vTargetView.setVisibility(View.VISIBLE);
		Point size = new Point();
		WindowManager wm = (WindowManager) vTargetView.getContext().getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getSize(size);
		int screenHeight = size.y;
		if (forward) {
			initialTosTranslationY = vTargetView.getTranslationY();
		}
		tosStartTranslationY = forward ? initialTosTranslationY : screenHeight;
		tosEndTranslationY = forward ? screenHeight : initialTosTranslationY;
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		vTargetView
			.setTranslationY(PresenterUtils.calculateStep(tosStartTranslationY, tosEndTranslationY, f));
	}

	@Override
	public void finalizeTransition(boolean forward) {
		if (forward) {
			vTargetView.setVisibility(View.INVISIBLE);
		}
		else {
			vTargetView.setVisibility(View.VISIBLE);
		}
	}
}
