package com.expedia.bookings.interfaces.helpers;

import com.expedia.bookings.interfaces.IStateListener;
import com.mobiata.android.Log;

/**
 * Sometimes you want an easy way to log all of the state change methods. Just add one of these bad boys to the an IStateProvider instance
 * and enjoy the sweet sweet logs.
 *
 * @param <T>
 */
public class StateListenerLogger<T> implements IStateListener<T> {

	@Override
	public void onStateTransitionStart(T stateOne, T stateTwo) {
		Log.d("IStateChangeLogger - " + getStateTypeStr(stateOne) + " - onPrepareStateTransition - stateOne:" + stateOne
			+ " stateTwo:" + stateTwo);
	}

	@Override
	public void onStateTransitionUpdate(T stateOne, T stateTwo, float percentage) {
		Log.d("IStateChangeLogger - " + getStateTypeStr(stateOne) + " - onStateTransitionPercentageChange - stateOne:"
			+ stateOne + " stateTwo:" + stateTwo
			+ " percentage:" + percentage);

	}

	@Override
	public void onStateTransitionEnd(T stateOne, T stateTwo) {
		Log.d("IStateChangeLogger - " + getStateTypeStr(stateOne) + " - onFinishStateTransition - stateOne:" + stateOne
			+ " stateTwo:" + stateTwo);
	}

	@Override
	public void onStateFinalized(T state) {
		Log.d("IStateChangeLogger - " + getStateTypeStr(state) + " - onStateFinalized - state:" + state);
	}

	private String getStateTypeStr(T state) {
		return state.getClass().getSimpleName();
	}

}
