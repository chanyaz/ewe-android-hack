package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.section.SectionFlightTrip;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FlightTripPriceFragment extends Fragment{

	public static FlightTripPriceFragment newInstance() {
		FlightTripPriceFragment fragment = new FlightTripPriceFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		SectionFlightTrip v = (SectionFlightTrip) inflater.inflate(R.layout.section_display_flight_trip_price_bar, container, false);
		
		if(Db.getFlightSearch().getSelectedFlightTrip() != null){
			v.bind(Db.getFlightSearch().getSelectedFlightTrip());
		}
		
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}
