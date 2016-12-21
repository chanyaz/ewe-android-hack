package com.expedia.bookings.launch.interfaces;

/**
 * PhoneLaunchActivity LaunchFragments must implement this interface. This interface
 * skews towards the expedia flavor, and as such, most implementers will not need to
 * do any work for these methods. Each product flavor must construct a LaunchFragment
 * instance that implements this interface.
 */
public interface IPhoneLaunchActivityLaunchFragment {

	/**
	 * Called when back is pressed on activity
	 * @return
	 * true: if event is handled
	 */
	boolean onBackPressed();
}
