package com.expedia.bookings.interfaces.helpers;

import android.support.v4.app.Fragment;

import com.expedia.bookings.interfaces.IMeasurementListener;
import com.expedia.bookings.interfaces.IMeasurementProvider;
import com.mobiata.android.util.Ui;

/**
 * This is an IMeasurementListener that makes implementing IMeasurementListener slightly easier for fragments
 * by providing methods for finding and registering with the parent IMeasurementProvider class.
 *
 * It is expected that registerWithProvider and unregisterWithProvider are called an equal number of times
 * throughout the app's lifetime. It is recommended to put them in lifecycle methods of the fragment. E.g.
 * onResume calls registerWithProvider and onPause calls unregisterWithProvider.
 *
 */
public abstract class MeasurementHelper implements IMeasurementListener {

	/**
	 * This looks for a parent fragment/activity that implements IMeasurementProvider and registers with it.
	 * 
	 * This is well suited for placement in one of the startup fragment lifecycle methods such as onStart,onResume,etc.
	 * 
	 * @param fragment
	 */
	public void registerWithProvider(Fragment fragment) {
		IMeasurementProvider provider = Ui.findFragmentListener(fragment, IMeasurementProvider.class);
		provider.registerMeasurementListener(this, true);
	}

	/**
	 * This looks for a parent fragment/activity that implements IMeasurementProvider and unregisters with it.
	 * 
	 * This is well suited for placement in one of the shutdown fragment lifecycle methods such as onStop,onPause,etc.
	 * 
	 * @param fragment
	 */
	public void unregisterWithProvider(Fragment fragment) {
		IMeasurementProvider provider = Ui.findFragmentListener(fragment, IMeasurementProvider.class);
		provider.unRegisterMeasurementListener(this);
	}
}
