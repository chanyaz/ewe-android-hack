package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	public static final String TAG = PhoneLaunchFragment.class.getName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_phone_launch, container, false);

		ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_action_bar_logo);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setElevation(0);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	////////////////////////////////////////////////////////////
	// IPhoneLaunchActivityLaunchFragment
	//
	// Note: If you intend to add code to these methods, make sure to override
	// onAttach and invoke IPhoneLaunchFragmentListener.onLaunchFragmentAttached,
	// otherwise PhoneLaunchActivity will never grab reference to this Fragment
	// instance and thus will not be able to invoke the following methods.

	@Override
	public void startMarquee() {
		// No work required
	}

	@Override
	public void cleanUp() {
		// No work required
	}

	@Override
	public void reset() {
		// No work required
	}
}
