package com.expedia.account.presenter;

import android.support.annotation.NonNull;

import com.expedia.account.presenter.Presenter.Transition;

/**
 * Created by doug on 5/13/15.
 */
public class CompoundTransition extends Transition {
	private Transition[] mTransitions;

	public CompoundTransition(String state1, String state2, @NonNull Transition... transitions) {
		super(state1, state2);
		mTransitions = transitions;
	}

	public CompoundTransition(String state1, String state2, int duration, @NonNull Transition... transitions) {
		super(state1, state2, null, duration);
		mTransitions = transitions;
	}

	@Override
	public void startTransition(boolean forward) {
		super.startTransition(forward);
		for (Transition t : mTransitions) {
			t.startTransition(forward);
		}
	}

	@Override
	public void updateTransition(float f, boolean forward) {
		super.updateTransition(f, forward);
		for (Transition t : mTransitions) {
			t.updateTransition(f, forward);
		}
	}

	@Override
	public void endTransition(boolean forward) {
		super.endTransition(forward);
		for (Transition t : mTransitions) {
			t.endTransition(forward);
		}
	}

	@Override
	public void finalizeTransition(boolean forward) {
		super.finalizeTransition(forward);
		for (Transition t : mTransitions) {
			t.finalizeTransition(forward);
		}
	}
}
