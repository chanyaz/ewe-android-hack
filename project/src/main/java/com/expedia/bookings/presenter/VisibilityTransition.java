package com.expedia.bookings.presenter;

import android.view.View;

public class VisibilityTransition extends Presenter.Transition {

	private final Presenter presenter;

	public VisibilityTransition(Presenter presenter, String state1, String state2) {
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
		System.out.println("destinationState:" + destinationState);
		for (int i = 0; i < presenter.getChildCount(); i++) {
			System.out.println("child:" + presenter.getChildAt(i).getClass().getName());
			View child = presenter.getChildAt(i);
			if (child.getClass().getName().equals(destinationState)) {
				System.out.println("Setting " + child.getClass().getName() + " to VISIBLE");
				child.setVisibility(View.VISIBLE);
			}
			else {
				System.out.println("Setting " + child.getClass().getName() + " to GONE");
				child.setVisibility(View.GONE);
			}
		}
	}

}
