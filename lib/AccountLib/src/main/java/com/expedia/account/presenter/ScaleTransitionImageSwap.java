package com.expedia.account.presenter;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.expedia.account.util.PresenterUtils;

public class ScaleTransitionImageSwap extends Presenter.Transition {

	private ImageView vTargetView;

	private Drawable mStartDrawable;
	private Drawable mEndDrawable;

	private float mStartScale;
	private float mEndScale;

	public ScaleTransitionImageSwap(ImageView target, Drawable startDrawable, Drawable endDrawable, float startScale, float endScale) {
		vTargetView = target;
		mStartDrawable = startDrawable;
		mEndDrawable = endDrawable;
		mStartScale = startScale;
		mEndScale = endScale;
	}

	@Override
	public void startTransition(boolean forward) {
		vTargetView.setImageDrawable(forward ? mStartDrawable : mEndDrawable);
		vTargetView.setScaleX(forward ? mStartScale : mEndScale);
		vTargetView.setScaleY(forward ? mStartScale : mEndScale);
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		float step = PresenterUtils.calculateStep(forward ? mStartScale : mEndScale, forward ? mEndScale : mStartScale , f);
		vTargetView.setScaleY(step);
		vTargetView.setScaleX(step);
	}

	@Override
	public void finalizeTransition(boolean forward) {
		vTargetView.setImageDrawable(forward ? mEndDrawable : mStartDrawable);
		vTargetView.setScaleX(forward ? mEndScale : mStartScale);
		vTargetView.setScaleY(forward ? mEndScale : mStartScale);
	}
}
