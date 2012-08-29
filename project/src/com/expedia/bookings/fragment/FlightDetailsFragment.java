package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.mobiata.android.json.JSONUtils;

public class FlightDetailsFragment extends Fragment {

	public static final String TAG = FlightDetailsFragment.class.getName();

	private static final String ARG_TRIP_LEG = "ARG_TRIP_LEG";

	public static FlightDetailsFragment newInstance(FlightTrip trip, FlightLeg leg) {
		FlightDetailsFragment fragment = new FlightDetailsFragment();
		Bundle args = new Bundle();
		JSONUtils.putJSONable(args, ARG_TRIP_LEG, new FlightTripLeg(trip, leg));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_flight_details, container, false);
	}

	public FlightTripLeg getFlightTripLeg() {
		return JSONUtils.getJSONable(getArguments(), ARG_TRIP_LEG, FlightTripLeg.class);
	}
}
