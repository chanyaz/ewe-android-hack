package com.expedia.bookings.interfaces;

/**
 * If we attach to an activity that implements this we will notify that activity we are attached.
 * This is useful for getting references to fragments that are in viewpagers
 */
public interface IPhoneLaunchFragmentListener {
	void onLaunchFragmentAttached(IPhoneLaunchActivityLaunchFragment frag);
}
