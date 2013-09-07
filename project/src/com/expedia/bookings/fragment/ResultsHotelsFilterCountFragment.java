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
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
public class ResultsHotelsFilterCountFragment extends Fragment {

	public static ResultsHotelsFilterCountFragment newInstance() {
		ResultsHotelsFilterCountFragment frag = new ResultsHotelsFilterCountFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		TextView view = new TextView(getActivity());
		view.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		view.setGravity(Gravity.CENTER);
		view.setBackgroundColor(Color.argb(200, 0, 0, 0));
		view.setTextColor(Color.WHITE);
		view.setText("Hotel Filtered Count");

		return view;
	}

}
