package com.expedia.bookings.interfaces.helpers;

import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.IStateListener;

/**
 * A concrete implementation of IStateListener<T> that implements a single state transition
 * from one state to another state (and reversed too, if desired).
 *
 * Created by dmelton on 6/10/14.
 */
public class SingleStateListener<T> implements IStateListener<T> {
	private final T mStateOne;
	private final T mStateTwo;
	private final boolean isBidirectional;
	private final ISingleStateListener mListener;

	public SingleStateListener(T stateOne, T stateTwo, boolean bidirectional, ISingleStateListener listener) {
		mStateOne = stateOne;
		mStateTwo = stateTwo;
		isBidirectional = bidirectional;
		mListener = listener;
	}

	public final T getStateOne() {
		return mStateOne;
	}

	public final T getStateTwo() {
		return mStateTwo;
	}

	@Override
	public final void onStateTransitionStart(T stateOne, T stateTwo) {
		if (stateOne.equals(mStateOne) && stateTwo.equals(mStateTwo)) {
			mListener.onStateTransitionStart(false);
		}
		else if (isBidirectional && stateOne.equals(mStateTwo) && stateTwo.equals(mStateOne)) {
			mListener.onStateTransitionStart(true);
		}
	}

	@Override
	public final void onStateTransitionUpdate(T stateOne, T stateTwo, float percentage) {
		if (stateOne.equals(mStateOne) && stateTwo.equals(mStateTwo)) {
			mListener.onStateTransitionUpdate(false, percentage);
		}
		else if (isBidirectional && stateOne.equals(mStateTwo) && stateTwo.equals(mStateOne)) {
			mListener.onStateTransitionUpdate(true, 1 - percentage);
		}
	}

	@Override
	public final void onStateTransitionEnd(T stateOne, T stateTwo) {
		if (stateOne.equals(mStateOne) && stateTwo.equals(mStateTwo)) {
			mListener.onStateTransitionEnd(false);
		}
		else if (isBidirectional && stateOne.equals(mStateTwo) && stateTwo.equals(mStateOne)) {
			mListener.onStateTransitionEnd(true);
		}
	}

	@Override
	public final void onStateFinalized(T state) {
		if (state.equals(mStateTwo)) {
			mListener.onStateFinalized(false);
		}
		else if (isBidirectional && state.equals(mStateOne)) {
			mListener.onStateFinalized(true);
		}
	}
}
