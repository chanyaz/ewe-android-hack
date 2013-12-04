package com.expedia.bookings.interfaces.helpers;

import java.util.ArrayList;

import com.expedia.bookings.interfaces.IStateListener;

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
		for (IStateListener<T> listener : mStateChangeListeners) {
			if (!mInactiveStateChangeListeners.contains(listener)) {
				listener.onStateTransitionStart(stateOne, stateTwo);
			}
		}
	}

	public void updateStateTransition(T stateOne, T stateTwo, float percentage) {
		for (IStateListener<T> listener : mStateChangeListeners) {
			if (!mInactiveStateChangeListeners.contains(listener)) {
				listener.onStateTransitionUpdate(stateOne, stateTwo, percentage);
			}
		}
	}

	public void endStateTransition(T stateOne, T stateTwo) {
		for (IStateListener<T> listener : mStateChangeListeners) {
			if (!mInactiveStateChangeListeners.contains(listener)) {
				listener.onStateTransitionEnd(stateOne, stateTwo);
			}
		}
	}

	public void finalizeState(T state) {
		mLastFinalizedState = state;
		for (IStateListener<T> listener : mStateChangeListeners) {
			if (!mInactiveStateChangeListeners.contains(listener)) {
				listener.onStateFinalized(state);
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
