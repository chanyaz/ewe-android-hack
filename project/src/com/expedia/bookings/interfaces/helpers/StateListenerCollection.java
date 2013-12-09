package com.expedia.bookings.interfaces.helpers;

import java.util.ArrayList;

import com.expedia.bookings.interfaces.IStateListener;
import com.mobiata.android.Log;

/**
 * This class is designed to ease the implementation of IStateProvider.
 * 
 * It allows for easy registration/notification/deactivation of IStateListeners
 *
 * @param <T>
 */
public class StateListenerCollection<T> {
	private ArrayList<IStateListener<T>> mStateChangeListeners = new ArrayList<IStateListener<T>>();
	private ArrayList<IStateListener<T>> mInactiveStateChangeListeners = new ArrayList<IStateListener<T>>();
	private T mLastFinalizedState;

	private T mTransStartState;
	private T mTransEndState;

	public StateListenerCollection(T startState) {
		mLastFinalizedState = startState;
	}

	public boolean setListenerInactive(IStateListener<T> listener) {
		if (!mInactiveStateChangeListeners.contains(listener)) {
			mInactiveStateChangeListeners.add(listener);
			return true;
		}
		return false;
	}

	public boolean setListenerActive(IStateListener<T> listener) {
		return mInactiveStateChangeListeners.remove(listener);
	}

	public void startStateTransition(T stateOne, T stateTwo) {
		if (mTransStartState != null || mTransEndState != null) {
			Log.e("startStateTransition may not be called until after endStateTransition has been called on the previous transition.");
		}
		else {
			mTransStartState = stateOne;
			mTransEndState = stateTwo;
			for (IStateListener<T> listener : mStateChangeListeners) {
				if (!mInactiveStateChangeListeners.contains(listener)) {
					listener.onStateTransitionStart(stateOne, stateTwo);
				}
			}
		}
	}

	public void updateStateTransition(T stateOne, T stateTwo, float percentage) {
		if (mTransStartState == null || mTransEndState == null || stateOne != mTransStartState
				|| stateTwo != mTransEndState) {
			Log.e("updateStateTransition must be called after startStateTransition. The argument states of startStateTransition must match the argument states of updateStateTransition. startStateOne:"
					+ mTransStartState
					+ " startStateTwo:"
					+ mTransEndState
					+ " stateOne:"
					+ stateOne
					+ " stateTwo:"
					+ stateTwo);
		}
		else {
			for (IStateListener<T> listener : mStateChangeListeners) {
				if (!mInactiveStateChangeListeners.contains(listener)) {
					listener.onStateTransitionUpdate(stateOne, stateTwo, percentage);
				}
			}
		}
	}

	public void endStateTransition(T stateOne, T stateTwo) {
		if (mTransStartState == null || mTransEndState == null || stateOne != mTransStartState
				|| stateTwo != mTransEndState) {
			Log.e("endStateTransition must be called after startStateTransition. The argument states of startStateTransition must match the argument states of updateStateTransition. startStateOne:"
					+ mTransStartState
					+ " startStateTwo:"
					+ mTransEndState
					+ " stateOne:"
					+ stateOne
					+ " stateTwo:"
					+ stateTwo);
		}
		else {
			for (IStateListener<T> listener : mStateChangeListeners) {
				if (!mInactiveStateChangeListeners.contains(listener)) {
					listener.onStateTransitionEnd(stateOne, stateTwo);
				}
			}
			mTransStartState = null;
			mTransEndState = null;
		}
	}

	public void finalizeState(T state) {
		if (mTransStartState != null || mTransEndState != null) {
			Log.e("finalizeState may not be called until after endStateTransition has been called on the transition. Transition that needs to end startStateOne:"
					+ mTransStartState
					+ " startStateTwo:"
					+ mTransEndState);
		}
		else {
			mLastFinalizedState = state;
			for (IStateListener<T> listener : mStateChangeListeners) {
				if (!mInactiveStateChangeListeners.contains(listener)) {
					listener.onStateFinalized(state);
				}
			}
		}
	}

	public void registerStateListener(IStateListener<T> listener, boolean fireFinalizeState) {
		if (!mStateChangeListeners.contains(listener)) {
			mStateChangeListeners.add(listener);
		}
		if (fireFinalizeState) {
			listener.onStateFinalized(mLastFinalizedState);
		}
	}

	public void unRegisterStateListener(IStateListener<T> listener) {
		mStateChangeListeners.remove(listener);
		mInactiveStateChangeListeners.remove(listener);
	}
}
