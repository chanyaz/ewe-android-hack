package com.expedia.bookings.interfaces.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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

	//These are temporary stores that help us add and remove things while iterating.
	private boolean mIsIterating = false;
	private LinkedHashMap<IStateListener<T>, ListenerAction> mPendingActions = new LinkedHashMap<IStateListener<T>, ListenerAction>();
	private boolean mProfilingEnabled = true;

	private enum ListenerAction {
		ADD,
		ADD_AND_FIRE,
		REMOVE
	}

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
			Log.e(
				"startStateTransition may not be called until after endStateTransition has been called on the previous transition.");
		}
		else {
			mTransStartState = stateOne;
			mTransEndState = stateTwo;
			preIterate();
			TimingLogger logger = null;
			if (mProfilingEnabled) {
				logger = new TimingLogger("StateListenerCollection", "startStateTransition");
			}
			for (IStateListener<T> listener : mStateChangeListeners) {
				if (!mInactiveStateChangeListeners.contains(listener)) {
					listener.onStateTransitionStart(stateOne, stateTwo);
					if (mProfilingEnabled) {
						logger.addSplit(getProfilingString(listener, stateOne, stateTwo));
					}
				}
			}
			if (mProfilingEnabled) {
				logger.dumpToLog();
			}
			postIterate();
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
			preIterate();
			TimingLogger logger = null;
			if (mProfilingEnabled) {
				logger = new TimingLogger("StateListenerCollection", "updateStateTransition");
			}
			for (IStateListener<T> listener : mStateChangeListeners) {
				if (!mInactiveStateChangeListeners.contains(listener)) {
					listener.onStateTransitionUpdate(stateOne, stateTwo, percentage);
					if (mProfilingEnabled) {
						logger.addSplit(getProfilingString(listener, stateOne, stateTwo) + " percentage:" + percentage);
					}
				}
			}
			if (mProfilingEnabled) {
				logger.dumpToLog();
			}
			postIterate();
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
			preIterate();
			TimingLogger logger = null;
			if (mProfilingEnabled) {
				logger = new TimingLogger("StateListenerCollection", "endStateTransition");
			}
			for (IStateListener<T> listener : mStateChangeListeners) {
				if (!mInactiveStateChangeListeners.contains(listener)) {
					listener.onStateTransitionEnd(stateOne, stateTwo);
					if (mProfilingEnabled) {
						logger.addSplit(getProfilingString(listener, stateOne, stateTwo));
					}
				}
			}
			if (mProfilingEnabled) {
				logger.dumpToLog();
			}
			postIterate();
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
			preIterate();
			TimingLogger logger = null;
			if (mProfilingEnabled) {
				logger = new TimingLogger("StateListenerCollection", "finalizeState");
			}
			for (IStateListener<T> listener : mStateChangeListeners) {
				if (!mInactiveStateChangeListeners.contains(listener)) {
					listener.onStateFinalized(state);
					if (mProfilingEnabled) {
						logger.addSplit(getProfilingString(listener, state));
					}
				}
			}
			if (mProfilingEnabled) {
				logger.dumpToLog();
			}
			postIterate();
		}
	}

	public void registerStateListener(IStateListener<T> listener, boolean fireFinalizeState) {
		if (isIterating()) {
			//If we are iterating, lets wait until we are done before we add our new listener
			mPendingActions.put(listener, (fireFinalizeState ? ListenerAction.ADD_AND_FIRE : ListenerAction.ADD));
		}
		else {
			if (!mStateChangeListeners.contains(listener)) {
				mStateChangeListeners.add(listener);
			}
			if (mInactiveStateChangeListeners.contains(listener)) {
				mInactiveStateChangeListeners.remove(listener);
			}
			if (fireFinalizeState) {
				listener.onStateFinalized(mLastFinalizedState);
			}
		}
	}

	public void unRegisterStateListener(IStateListener<T> listener) {
		if (isIterating()) {
			//If we are iterating, we set the listener inactive (so it stops receiving events)
			mInactiveStateChangeListeners.add(listener);
			//And we slate it for removal
			mPendingActions.put(listener, ListenerAction.REMOVE);
		}
		else {
			mStateChangeListeners.remove(listener);
			mInactiveStateChangeListeners.remove(listener);
		}
	}

	private boolean isIterating() {
		return mIsIterating;
	}

	private void preIterate() {
		mIsIterating = true;
	}

	private void postIterate() {
		mIsIterating = false;

		Iterator<Map.Entry<IStateListener<T>, ListenerAction>> iter = mPendingActions.entrySet().iterator();
		while (!mIsIterating && iter.hasNext()) {
			Map.Entry<IStateListener<T>, ListenerAction> entry = iter.next();
			if (entry.getValue().compareTo(ListenerAction.REMOVE) == 0) {
				unRegisterStateListener(entry.getKey());
			}
			else {
				boolean fireFinalize = entry.getValue().compareTo(ListenerAction.ADD_AND_FIRE) == 0;
				registerStateListener(entry.getKey(), fireFinalize);
			}
			iter.remove();
		}
	}

	private String getProfilingString(IStateListener<T> listener, T... states) {
		String retStr = "listener:" + listener.getClass().getName();
		for (int i = 0; i < states.length; i++) {
			retStr += " State #" + i + ":" + states[i];
		}
		return retStr;
	}
}
