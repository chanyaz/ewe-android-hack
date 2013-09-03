package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.graphics.DestinationBitmapDrawable;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * ResultsBackgroundImageFragment: The fragment that acts as a background image for the whole
 * results activity designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class ResultsBackgroundImageFragment extends MeasurableFragment {

	private static final String ARG_DESTINATION = "ARG_DESTINATION";

	public static ResultsBackgroundImageFragment newInstance(String destination) {
		ResultsBackgroundImageFragment fragment = new ResultsBackgroundImageFragment();
		Bundle args = new Bundle();
		args.putString(ARG_DESTINATION, destination);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;

		View view = new View(getActivity());
		DestinationBitmapDrawable drawable = new DestinationBitmapDrawable(getResources(),
				R.drawable.loading_repeating_sky, this.getArguments().getString(ARG_DESTINATION), width,
				height);

		view.setBackgroundDrawable(drawable);

		return view;
	}

}
