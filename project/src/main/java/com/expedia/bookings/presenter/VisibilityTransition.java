package com.expedia.bookings.presenter;

import android.view.View;

public class VisibilityTransition extends Presenter.Transition {
	private final Presenter presenter;

	public VisibilityTransition(Presenter presenter, Class state1, Class state2) {
		super(state1.getName(), state2.getName(), null, 0);
		this.presenter = presenter;
	}

	@Override
	public void endTransition(boolean forward) {
		String destinationState = forward ? state2 : state1;
		String currentState = forward ? state1 : state2;

		for (int i = 0; i < presenter.getChildCount(); i++) {
			View child = presenter.getChildAt(i);
			if (child.getClass().getName().equals(destinationState)) {
				child.setVisibility(View.VISIBLE);
			}
			else if (child.getClass().getName().equals(currentState)) {
				child.setVisibility(View.GONE);
			}
		}
	}
}
