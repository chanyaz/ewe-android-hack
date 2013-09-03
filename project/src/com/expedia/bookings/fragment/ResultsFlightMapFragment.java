package com.expedia.bookings.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

/**
 * ResultsFlightMapFragment: The hotel map fragment designed for tablet results 2013
 */
public class ResultsFlightMapFragment extends Fragment {

	public static ResultsFlightMapFragment newInstance() {
		ResultsFlightMapFragment frag = new ResultsFlightMapFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		TextView view = new TextView(getActivity());
		view.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		view.setGravity(Gravity.CENTER);
		view.setBackgroundColor(Color.GREEN);
		view.setText("Flight Map");

		return view;
	}

}
