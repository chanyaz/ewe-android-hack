package com.expedia.bookings.fragment.debug;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.fragment.base.MeasurableFragment;

/**
 * Good simple Fragment for testing.
 * 
 * Use with Color.rgb() for maximum effect.
 */
public class ColorFragment extends MeasurableFragment {

	private static String ARG_COLOR = "ARG_COLOR";

	public static ColorFragment newInstance(int color) {
		ColorFragment fragment = new ColorFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_COLOR, color);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = new View(getActivity());
		view.setBackgroundColor(getArguments().getInt(ARG_COLOR));
		return view;
	}

}
