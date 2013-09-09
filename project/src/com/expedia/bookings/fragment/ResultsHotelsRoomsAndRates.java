package com.expedia.bookings.fragment;

import com.expedia.bookings.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
public class ResultsHotelsRoomsAndRates extends Fragment {

	public static ResultsHotelsRoomsAndRates newInstance() {
		ResultsHotelsRoomsAndRates frag = new ResultsHotelsRoomsAndRates();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_tablet_hotels_rooms_and_rates, null);

		return view;
	}

}
