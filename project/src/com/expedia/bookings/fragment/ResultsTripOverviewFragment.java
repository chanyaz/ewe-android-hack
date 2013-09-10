package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

/**
 * ResultsTripOverviewFragment: The trip overview fragment designed for tablet results 2013
 */
public class ResultsTripOverviewFragment extends Fragment {

	public static ResultsTripOverviewFragment newInstance() {
		ResultsTripOverviewFragment frag = new ResultsTripOverviewFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		TextView view = new TextView(getActivity());
		view.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		view.setGravity(Gravity.CENTER);
		view.setText("Trip Overview");

		return view;
	}

}
