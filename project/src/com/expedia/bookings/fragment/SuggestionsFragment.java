package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.AirportDropDownAdapter;

public class SuggestionsFragment extends ListFragment {

	private AirportDropDownAdapter mAirportAdapter;

	// Sometimes we want to prep text to filter before start; by default
	// we start with a blank query (to kick off the defaults)
	private CharSequence mTextToFilterOnCreate = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_suggestions, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mAirportAdapter == null) {
			mAirportAdapter = new AirportDropDownAdapter(getActivity());
			mAirportAdapter.setShowNearbyAirports(true);
			mAirportAdapter.setMaxNearbyAirports(10); // Bump this up just so we see stuff to start
		}

		setListAdapter(mAirportAdapter);

		filter(mTextToFilterOnCreate);
	}

	public void filter(CharSequence text) {
		if (getView() != null) {
			mAirportAdapter.getFilter().filter(text);
		}
		else {
			mTextToFilterOnCreate = text;
		}
	}
}
