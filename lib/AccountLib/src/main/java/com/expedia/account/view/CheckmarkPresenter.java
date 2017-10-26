package com.expedia.account.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.expedia.account.R;
import com.expedia.account.presenter.BufferedPresenter;
import com.expedia.account.util.PresenterUtils;


public class CheckmarkPresenter extends BufferedPresenter {

	private ImageView vGoodMark;
	private ImageView vBadMark;

	public boolean isSinglePageStyle = false;
	// States
	public static final String STATE_HIDDEN = "STATE_HIDDEN";
	public static final String STATE_GOOD = "STATE_GOOD";
	public static final String STATE_BAD = "STATE_BAD";

	public CheckmarkPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.acct__widget_checkmark, this);
		vGoodMark = (ImageView) findViewById(R.id.checkmark_good);
		vBadMark = (ImageView) findViewById(R.id.checkmark_bad);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		addDefaultTransition(new DefaultTransition(STATE_HIDDEN) {
			//All set up in xml, don't need to do anything, really.
		});
		addTransition(new RotateInTransition(STATE_HIDDEN, STATE_GOOD, vGoodMark));
		addTransition(new RotateInTransition(STATE_HIDDEN, STATE_BAD, vBadMark));
		addTransition(new RotateSwapTransition(STATE_GOOD, STATE_BAD, vGoodMark, vBadMark));
	}

	public void styleizeFromAccountView(TypedArray a) {
		if (!isSinglePageStyle && a.hasValue(R.styleable.acct__AccountView_acct__checkmark_good_drawable)) {
			vGoodMark.setImageDrawable(a.getDrawable(R.styleable.acct__AccountView_acct__checkmark_good_drawable));
		}
		else {
			vGoodMark.setImageResource(R.drawable.acct__default_checkmark_good_drawable);
		}
		if (!isSinglePageStyle && a.hasValue(R.styleable.acct__AccountView_acct__checkmark_warning_drawable)) {
			vBadMark.setImageDrawable(a.getDrawable(R.styleable.acct__AccountView_acct__checkmark_warning_drawable));
		}
		else {
			vBadMark.setImageResource(R.drawable.acct__default_checkmark_warning_drawable);
		}
	}

	private class RotateSwapTransition extends Transition {

		private View vFirstView;
		private View vSecondView;

		public RotateSwapTransition(String start, String finish, View firstView, View secondView) {
			super(start, finish, new OvershootInterpolator());
			vFirstView = firstView;
			vSecondView = secondView;
		}

		private float outStartAlpha = 1;
		private float outEndAlpha = 0;
		private float outStartRotation = 0;
		private float outEndRotation = -180;
		private float outStartScale = 1.0f;
		private float outEndScale = 0.5f;


		@Override
		public void startTransition(boolean forward) {
			View in;
			View out;
			if (forward) {
				in = vSecondView;
				out = vFirstView;
			}
			else {
				in = vFirstView;
				out = vSecondView;
			}

			out.setAlpha(outStartAlpha);
			out.setRotation(outStartRotation);
			out.setScaleX(outStartScale);
			out.setScaleY(outStartScale);

			in.setAlpha(outEndAlpha);
			in.setRotation(outEndRotation);
			in.setScaleX(outEndScale);
			in.setScaleX(outEndScale);

		}

		@Override
		public void updateTransition(float f, boolean forward) {
			View in;
			View out;
			if (forward) {
				in = vSecondView;
				out = vFirstView;
			}
			else {
				in = vFirstView;
				out = vSecondView;
			}

			out.setAlpha(PresenterUtils.calculateStep(outStartAlpha, outEndAlpha, f));
			out.setRotation(PresenterUtils.calculateStep(outStartRotation, outEndRotation, f));
			out.setScaleX(PresenterUtils.calculateStep(outStartScale, outEndScale, f));
			out.setScaleY(PresenterUtils.calculateStep(outStartScale, outEndScale, f));

			in.setAlpha(PresenterUtils.calculateStep(outEndAlpha, outStartAlpha, f));
			in.setRotation(PresenterUtils.calculateStep(outEndRotation, outStartRotation, f));
			in.setScaleX(PresenterUtils.calculateStep(outEndScale, outStartScale, f));
			in.setScaleY(PresenterUtils.calculateStep(outEndScale, outStartScale, f));
		}

		@Override
		public void finalizeTransition(boolean forward) {
			View in;
			View out;
			if (forward) {
				in = vSecondView;
				out = vFirstView;
			}
			else {
				in = vFirstView;
				out = vSecondView;
			}
			out.setAlpha(outEndAlpha);
			out.setRotation(outEndRotation);
			out.setScaleX(outEndScale);
			out.setScaleY(outEndScale);

			in.setAlpha(outStartAlpha);
			in.setRotation(outStartRotation);
			in.setScaleX(outStartScale);
			in.setScaleY(outStartScale);
		}
	}

	private class RotateInTransition extends Transition {

		private View mTargetView;

		public RotateInTransition(String start, String finish, View targetView) {
			super(start, finish, new OvershootInterpolator());
			mTargetView = targetView;
		}

		private float startAlpha = 0;
		private float endAlpha = 1;
		private float startRotation = -180;
		private float endRotation = 0;
		private float startScale = 0.5f;
		private float endScale = 1;

		@Override
		public void startTransition(boolean forward) {
			if (forward) {
				mTargetView.setAlpha(startAlpha);
				mTargetView.setRotation(startRotation);
				mTargetView.setScaleX(startScale);
				mTargetView.setScaleY(startScale);
			}
			else {
				mTargetView.setAlpha(endAlpha);
				mTargetView.setRotation(endRotation);
				mTargetView.setScaleX(endScale);
				mTargetView.setScaleY(endScale);
			}
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			f = forward ? f : 1f - f;
			mTargetView.setAlpha(PresenterUtils.calculateStep(startAlpha, endAlpha, f));
			mTargetView.setRotation(PresenterUtils.calculateStep(startRotation, endRotation, f));
			mTargetView.setScaleX(PresenterUtils.calculateStep(startScale, endScale, f));
			mTargetView.setScaleY(PresenterUtils.calculateStep(startScale, endScale, f));
		}

		@Override
		public void finalizeTransition(boolean forward) {
			if (forward) {
				mTargetView.setAlpha(endAlpha);
				mTargetView.setRotation(endRotation);
				mTargetView.setScaleX(endScale);
				mTargetView.setScaleY(endScale);
			}
			else {
				mTargetView.setAlpha(startAlpha);
				mTargetView.setRotation(startRotation);
				mTargetView.setScaleX(startScale);
				mTargetView.setScaleY(startScale);
			}
		}
	}
}
