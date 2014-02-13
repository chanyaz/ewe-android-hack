package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;

/**
 * Results loading fragment for Tablet
 */
public class ResultsLoadingFragment extends Fragment {

	public static ResultsLoadingFragment newInstance() {
		ResultsLoadingFragment frag = new ResultsLoadingFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_results_loading, null);

		return view;
	}
}
