package com.expedia.bookings.fragment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.mobiata.android.util.Ui;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class FlightFilterDialogFragment extends DialogFragment {

	public static final String TAG = FilterDialogFragment.class.getName();

	private static final String ARG_LEG_POSITION = "ARG_LEG_POSITION";

	private SegmentedControlGroup mSortControl;
	private ViewGroup mAirlineContainer;

	public static FlightFilterDialogFragment newInstance(int legPosition) {
		FlightFilterDialogFragment fragment = new FlightFilterDialogFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_LEG_POSITION, legPosition);
		fragment.setArguments(args);
		return fragment;
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FlightFilterDialogTheme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_filter, container, false);

		mSortControl = Ui.findView(v, R.id.sort_button_group);
		mAirlineContainer = Ui.findView(v, R.id.airline_filter_container);

		configureAirlines();

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		// Setup the dialog so that it appears at the bottom of the screen,
		// taking up the entire width of the screen.
		//
		// We do this in onStart() because we need to wait until the dialog
		// is shown before we can start modifying its window like this.
		Window window = getDialog().getWindow();
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		window.setGravity(Gravity.BOTTOM);
		window.setBackgroundDrawable(null);
	}

	//////////////////////////////////////////////////////////////////////////
	// Airline preference

	// Add a button for each airline on the airline preference container
	private void configureAirlines() {
		LayoutInflater inflater = getActivity().getLayoutInflater();

		FlightSearch search = Db.getFlightSearch();

		int legPosition = getArguments().getInt(ARG_LEG_POSITION);
		List<FlightTrip> trips = search.getTrips(legPosition, false);
		Set<String> airlines = getAirlines(trips, legPosition);

		for (String airlineCode : airlines) {
			Button airlineButton = (Button) inflater.inflate(R.layout.snippet_airline_button, mAirlineContainer, false);

			Airline airline = FlightStatsDbUtils.getAirline(airlineCode);

			airlineButton.setText(airline.mAirlineName);
			mAirlineContainer.addView(airlineButton);
		}
	}

	// TODO: Should this be based on operating airline?  Marketing airline?  Not sure,
	// so it includes ALL airlines at this point.
	private Set<String> getAirlines(List<FlightTrip> trips, int legPosition) {
		Set<String> airlines = new HashSet<String>();

		for (FlightTrip trip : trips) {
			FlightLeg leg = trip.getLeg(legPosition);
			for (Flight flight : leg.getSegments()) {
				FlightCode code = flight.getPrimaryFlightCode();
				if (code != null) {
					airlines.add(code.mAirlineCode);
				}

				code = flight.getOperatingFlightCode();
				if (code != null) {
					airlines.add(code.mAirlineCode);
				}
			}
		}

		return airlines;
	}
}
