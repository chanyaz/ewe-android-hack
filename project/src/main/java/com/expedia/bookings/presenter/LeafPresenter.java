package com.expedia.bookings.presenter;

import java.util.Stack;

/**
 * A simple implementation of IPresenter for "leaf node" presenters,
 * i.e. presenters with no child presenters to animate.
 */

public class LeafPresenter implements IPresenter<IPresenter> {

	@Override
	public void initBackStack() {
		// No backstack associated with a leaf node
	}

	@Override
	public Stack<IPresenter> getBackStack() {
		// No backstack associated with a leaf node
		return null;
	}

	@Override
	public boolean back() {
		// No backstack associated with a leaf node
		return false;
	}

	@Override
	public void clearBackStack() {
		// No backstack associated with a leaf node
	}

	@Override
	public void show(IPresenter presenter) {
		// Do nothing, show expected to be handled by Transition implementation
	}

	@Override
	public void hide(IPresenter presenter) {
		// Do nothing, hide expected to be handled by Transition implementation
	}
}