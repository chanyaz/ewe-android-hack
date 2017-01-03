package com.expedia.bookings.launch.interfaces;

/**
 * If we attach to an activity that implements this we will notify that activity we are attached.
 * This is useful for getting references to fragments that are in viewpagers
 */
public interface IPhoneLaunchFragmentListener {
	//11/24/15 - this is not currently called from anywhere so it shouldn't really be overridden anywhere either, but it is.
	@Deprecated
	void onLaunchFragmentAttached(IPhoneLaunchActivityLaunchFragment frag);
}
