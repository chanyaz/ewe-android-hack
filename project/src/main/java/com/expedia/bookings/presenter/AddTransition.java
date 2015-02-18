package com.expedia.bookings.presenter;

import android.view.View;

public class AddTransition extends Presenter.Transition {

	private final Presenter presenter;

	public AddTransition(Presenter presenter, String state1, String state2) {
		super(state1, state2, null, 0);
		this.presenter = presenter;
	}

	@Override
	public void startTransition(boolean forward) {
	}

	@Override
	public void updateTransition(float f, boolean forward) {
	}

	@Override
	public void endTransition(boolean forward) {
	}

	@Override
	public void finalizeTransition(boolean forward) {
		String destinationState = forward ? state2 : state1;
		for (int i = 0; i < presenter.getChildCount(); i++) {
			View child = presenter.getChildAt(i);
			if (child.getClass().getName().equals(destinationState)) {
				child.setVisibility(View.VISIBLE);
			}
		}
	}

}
