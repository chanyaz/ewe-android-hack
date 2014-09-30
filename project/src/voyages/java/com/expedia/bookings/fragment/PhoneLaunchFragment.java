package com.expedia.bookings.fragment;

import android.support.v4.app.Fragment;

import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;

/**
 * This class exists to support the build system and the way in which we provide different
 * LaunchFragment implementations for a given product flavor. The voyages product flavor
 * does not show PhoneLaunchActivity and thus does not use LaunchFragment - so this placeholder
 * acts only to appease the build system.
 */
public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	////////////////////////////////////////////////////////////
	// IPhoneLaunchActivityLaunchFragment
	//
	// Note: If you intend to add code to these methods, make sure to override
	// onAttach and invoke IPhoneLaunchFragmentListener.onLaunchFragmentAttached,
	// otherwise PhoneLaunchActivity will never grab reference to this Fragment
	// instance and thus will not be able to invoke the following methods.

	@Override
	public void startMarquee() {

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void reset() {

	}
}
