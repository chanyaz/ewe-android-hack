package com.expedia.bookings.interfaces.helpers;

import java.util.ArrayList;
import java.util.HashSet;

import com.expedia.bookings.interfaces.IStateListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.TimingLogger;

/**
 * This class is designed to ease the implementation of IStateProvider.
 * <p/>
 * It allows for easy registration/notification/deactivation of IStateListeners
 *
 * @param <T>
 */
public class StateListenerCollection<T> {
	//These are the persistant lists of our class
	private ArrayList<IStateListener<T>> mStateChangeListeners = new ArrayList<IStateListener<T>>();
	private HashSet<IStateListener<T>> mInactiveStateChangeListeners = new HashSet<IStateListener<T>>();

	private boolean mProfilingEnabled = false;

	private T mLastFinalizedState;
	private T mTransStartState;
	private T mTransEndState;

	public StateListenerCollection(T startState) {

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

	private ArrayList<IStateListener<T>> getSafeListeners() {
		return (ArrayList<IStateListener<T>>) mStateChangeListeners.clone();
	}

	private HashSet<IStateListener<T>> getSafeInactiveListeners() {
		return (HashSet<IStateListener<T>>) mInactiveStateChangeListeners.clone();
	}

	public void startStateTransition(T stateOne, T stateTwo) {
		if (mTransStartState != null || mTransEndState != null) {
			Log.e(
				"startStateTransition may not be called until after endStateTransition has been called on the previous transition.");
		}
		else {
			mTransStartState = stateOne;
			mTransEndState = stateTwo;
			TimingLogger logger = null;
			if (mProfilingEnabled) {
				logger = new TimingLogger("StateListenerCollection", "startStateTransition");
			}
			HashSet<IStateListener<T>> inactive = getSafeInactiveListeners();
			for (IStateListener<T> listener : getSafeListeners()) {
				if (!inactive.contains(listener)) {
					listener.onStateTransitionStart(stateOne, stateTwo);
					if (mProfilingEnabled) {
						logger.addSplit(getProfilingString(listener, stateOne, stateTwo));
					}
				}
			}
			if (mProfilingEnabled) {
				logger.dumpToLog();
			}
		}
	}

	public void updateStateTransition(T stateOne, T stateTwo, float percentage) {
		if (mTransStartState == null || mTransEndState == null || stateOne != mTransStartState
			|| stateTwo != mTransEndState) {
			Log.e(
				"updateStateTransition must be called after startStateTransition. The argument states of startStateTransition must match the argument states of updateStateTransition. startStateOne:"
					+ mTransStartState
					+ " startStateTwo:"
					+ mTransEndState
					+ " stateOne:"
					+ stateOne
					+ " stateTwo:"
					+ stateTwo
			);
		}
		else {
			TimingLogger logger = null;
			if (mProfilingEnabled) {
				logger = new TimingLogger("StateListenerCollection", "updateStateTransition");
			}
			HashSet<IStateListener<T>> inactive = getSafeInactiveListeners();
			for (IStateListener<T> listener : getSafeListeners()) {
				if (!inactive.contains(listener)) {
					listener.onStateTransitionUpdate(stateOne, stateTwo, percentage);
					if (mProfilingEnabled) {
						logger.addSplit(getProfilingString(listener, stateOne, stateTwo) + " percentage:" + percentage);
					}
				}
			}
			if (mProfilingEnabled) {
				logger.dumpToLog();
			}
		}
	}

	public void endStateTransition(T stateOne, T stateTwo) {
		if (mTransStartState == null || mTransEndState == null || stateOne != mTransStartState
			|| stateTwo != mTransEndState) {
			Log.e(
				"endStateTransition must be called after startStateTransition. The argument states of startStateTransition must match the argument states of updateStateTransition. startStateOne:"
					+ mTransStartState
					+ " startStateTwo:"
					+ mTransEndState
					+ " stateOne:"
					+ stateOne
					+ " stateTwo:"
					+ stateTwo
			);
		}
		else {
			TimingLogger logger = null;
			if (mProfilingEnabled) {
				logger = new TimingLogger("StateListenerCollection", "endStateTransition");
			}
			HashSet<IStateListener<T>> inactive = getSafeInactiveListeners();
			for (IStateListener<T> listener : getSafeListeners()) {
				if (!inactive.contains(listener)) {
					listener.onStateTransitionEnd(stateOne, stateTwo);
					if (mProfilingEnabled) {
						logger.addSplit(getProfilingString(listener, stateOne, stateTwo));
					}
				}
			}
			if (mProfilingEnabled) {
				logger.dumpToLog();
			}
			mTransStartState = null;
			mTransEndState = null;
		}
	}

	public void finalizeState(T state) {
		if (mTransStartState != null || mTransEndState != null) {
			Log.e(
				"finalizeState may not be called until after endStateTransition has been called on the transition. Transition that needs to end startStateOne:"
					+ mTransStartState
					+ " startStateTwo:"
					+ mTransEndState
			);
		}
		else {
			mLastFinalizedState = state;
			TimingLogger logger = null;
			if (mProfilingEnabled) {
				logger = new TimingLogger("StateListenerCollection", "finalizeState");
			}
			HashSet<IStateListener<T>> inactive = getSafeInactiveListeners();
			for (IStateListener<T> listener : getSafeListeners()) {
				if (!inactive.contains(listener)) {
					listener.onStateFinalized(state);
					if (mProfilingEnabled) {
						logger.addSplit(getProfilingString(listener, state));
					}
				}
			}
			if (mProfilingEnabled) {
				logger.dumpToLog();
			}
		}
	}

	public void registerStateListener(IStateListener<T> listener, boolean fireFinalizeState) {
		if (!mStateChangeListeners.contains(listener)) {
			mStateChangeListeners.add(listener);
		}
		if (mInactiveStateChangeListeners.contains(listener)) {
			mInactiveStateChangeListeners.remove(listener);
		}
		if (fireFinalizeState && mLastFinalizedState != null) {
			listener.onStateFinalized(mLastFinalizedState);
		}
	}

	public void unRegisterStateListener(IStateListener<T> listener) {
		mStateChangeListeners.remove(listener);
		mInactiveStateChangeListeners.remove(listener);
	}

	private String getProfilingString(IStateListener<T> listener, T... states) {
		String retStr = "listener:" + listener.getClass().getName();
		for (int i = 0; i < states.length; i++) {
			retStr += " State #" + i + ":" + states[i];
		}
		return retStr;
	}
}
