package com.expedia.account.presenter;

import android.view.View;

public class LeftToRightTransition extends Presenter.Transition {
	private final Presenter presenter;
	private final String left;
	private final String right;

	public LeftToRightTransition(Presenter presenter, String left, String right) {
		super(left, right);
		this.presenter = presenter;
		this.left = left;
		this.right = right;
	}

	@Override
	public void startTransition(boolean forward) {
		for (int i = 0; i < presenter.getChildCount(); i++) {
			View child = presenter.getChildAt(i);

			if (child.getClass().getName().equals(left)) {
				child.setTranslationX(forward ? 0 : -child.getWidth());
				child.setVisibility(View.VISIBLE);
			}

			if (child.getClass().getName().equals(right)) {
				child.setTranslationX(forward ? child.getWidth() : 0);
				child.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		for (int i = 0; i < presenter.getChildCount(); i++) {
			View child = presenter.getChildAt(i);

			if (child.getClass().getName().equals(left)) {
				float translation = forward ? -child.getWidth() * f : child.getWidth() * (f - 1);
				child.setTranslationX(translation);
			}

			if (child.getClass().getName().equals(right)) {
				float translation = forward ? -child.getWidth() * (f - 1) : child.getWidth() * f;
				child.setTranslationX(translation);
			}
		}
	}

	@Override
	public void endTransition(boolean forward) {
		// ignore
	}

	@Override
	public void finalizeTransition(boolean forward) {
		for (int i = 0; i < presenter.getChildCount(); i++) {
			View child = presenter.getChildAt(i);

			if (child.getClass().getName().equals(left)) {
				child.setVisibility(forward ? View.GONE : View.VISIBLE);
				child.setTranslationX(0);
			}

			if (child.getClass().getName().equals(right)) {
				child.setVisibility(forward ? View.VISIBLE : View.GONE);
				child.setTranslationX(0);
			}
		}
	}
}
