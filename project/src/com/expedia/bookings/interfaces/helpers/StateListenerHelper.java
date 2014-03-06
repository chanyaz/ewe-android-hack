package com.expedia.bookings.interfaces.helpers;

import android.support.v4.app.Fragment;

import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.mobiata.android.util.Ui;

/**
 * This is an IStateChangeListener that makes implementing IStateChangeListener slightly easier for fragments
 * by providing methods for finding and registering with the parent IStateProvider class.
 *
 * It is expected that registerWithProvider and unregisterWithProvider are called an equal number of times
 * throughout the app's lifetime. It is recommended to put them in lifecycle methods of the fragment. E.g.
 * onResume calls registerWithProvider and onPause calls unregisterWithProvider.
 *
 */
public abstract class StateListenerHelper<T> implements IStateListener<T> {

	/**
	 * This looks for a parent fragment/activity that implements IStateProvider and registers with it.
	 * 
	 * This is well suited for placement in one of the startup fragment lifecycle methods such as onStart,onResume,etc.
	 * 
	 * @param fragment
	 */
	public void registerWithProvider(Fragment fragment) {
		IStateProvider<T> provider = findProvider(fragment);
		provider.registerStateListener(this, true);
	}

	/**
	 * This looks for a parent fragment/activity that implements IStateProvider and unregisters with it.
	 * 
	 * This is well suited for placement in one of the shutdown fragment lifecycle methods such as onStop,onPause,etc.
	 * 
	 * @param fragment
	 */
	public void unregisterWithProvider(Fragment fragment) {
		IStateProvider<T> provider = findProvider(fragment);
		provider.unRegisterStateListener(this);
	}

	@SuppressWarnings("unchecked")
	private IStateProvider<T> findProvider(Fragment fragment) {
		return Ui.findFragmentListener(fragment, IStateProvider.class, false);
	}
}
