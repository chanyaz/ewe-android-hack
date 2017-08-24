package com.expedia.bookings.interfaces.helpers;

import java.util.ArrayList;

import android.support.v4.app.Fragment;

import com.expedia.bookings.interfaces.IBackManageable;
import com.mobiata.android.util.Ui;

/**
 * This is a helper class for handling back presses in a tree type fragment hierarchy.
 * It is dependent on IBackManageable.
 */
public abstract class BackManager {

	private final ArrayList<IBackManageable> mListeners = new ArrayList<IBackManageable>();
	private final IBackManageable mManageable;

	/**
	 * This will typically be the IBackManageable that this BackManager is declared within.
	 * @param managable
	 */
	public BackManager(IBackManageable managable) {
		mManageable = managable;
	}

	/**
	 * See if we can find a parent IBackManageable and if so, register for handling onBackPressedEvents
	 * 
	 * This is best suited for being called in a fragments startup lifecycle methods e.g. onStart, onResume, etc.
	 * 
	 * @param frag
	 */
	public void registerWithParent(Fragment frag) {
		IBackManageable parent = Ui.findFragmentListener(frag, IBackManageable.class, false);
		if (parent != null && parent.getBackManager() != null) {
			parent.getBackManager().registerBackManageable(mManageable);
		}
	}

	/**
	 * See if we can find a parent IBackManageable and if so, unregister for handling onBackPressedEvents
	 * 
	 * This is best suited for being called in a fragments shutdown lifecycle methods e.g. onStop, onPause, etc.
	 * 
	 * @param frag
	 */
	public void unregisterWithParent(Fragment frag) {
		IBackManageable parent = Ui.findFragmentListener(frag, IBackManageable.class, false);
		if (parent != null && parent.getBackManager() != null) {
			parent.getBackManager().unRegisterBackManageable(mManageable);
		}
	}

	/**
	 * Register child IBackManageable that will try to handle onBackPressed() events before our own handleBackPressed() implementation is called.
	 * 
	 * @param listener
	 */
	public void registerBackManageable(IBackManageable listener) {
		mListeners.add(listener);
	}

	/**
	 * Unregister a child IBackManageable
	 * 
	 * @param listener
	 */
	public void unRegisterBackManageable(IBackManageable listener) {
		mListeners.remove(listener);
	}

	/**
	 * This method asks all of our IBackManageable children if they want to handle the back event,
	 * if not we pass it along to our own handleBackPressed method.
	 * 
	 * This should be called from the onBackPressed() method in the root of the tree activity.
	 * 
	 * @return
	 */
	public boolean doOnBackPressed() {
		for (IBackManageable listener : mListeners) {
			if (listener.getBackManager() != null && listener.getBackManager().doOnBackPressed()) {
				return true;
			}
		}
		return handleBackPressed();
	}

	/**
	 * This is where we do our backpressed work. If we consume the event return true, else false.
	 * 
	 * @return - true if we consumed the backPress, false otherwise
	 */
	protected abstract boolean handleBackPressed();

}
