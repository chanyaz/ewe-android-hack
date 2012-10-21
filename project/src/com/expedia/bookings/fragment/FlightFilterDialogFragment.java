package com.expedia.bookings.fragment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightFilter.Sort;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.mobiata.android.util.Ui;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class FlightFilterDialogFragment extends DialogFragment {

	public static final String TAG = FilterDialogFragment.class.getName();

	private static final String ARG_LEG_POSITION = "ARG_LEG_POSITION";

	private SegmentedControlGroup mSortControl;
	private ViewGroup mAirlineContainer;

	private Map<String, ToggleButton> mAirportButtons;

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

		// Configure the correct initial setting for the filter 
		FlightFilter filter = getFlightFilter();
		switch (filter.getSort()) {
		case PRICE:
			mSortControl.check(R.id.sort_price_button);
			break;
		case DEPARTURE:
			mSortControl.check(R.id.sort_departure_button);
			break;
		case ARRIVAL:
			mSortControl.check(R.id.sort_arrival_button);
			break;
		case DURATION:
			mSortControl.check(R.id.sort_duration_button);
			break;
		}

		for (String airlineCode : filter.getPreferredAirlines()) {
			mAirportButtons.get(airlineCode).setChecked(true);
		}

		// Setup listeners 
		mSortControl.setOnCheckedChangeListener(mSortButtonCheckListener);

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

		// #308: Fixed inability to cancel by touching outside of dialog
		getDialog().setCanceledOnTouchOutside(true);
	}

	//////////////////////////////////////////////////////////////////////////
	// Sort

	private OnCheckedChangeListener mSortButtonCheckListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			FlightFilter filter = getFlightFilter();

			Sort newSort;
			switch (checkedId) {
			case R.id.sort_departure_button:
				newSort = Sort.DEPARTURE;
				break;
			case R.id.sort_arrival_button:
				newSort = Sort.ARRIVAL;
				break;
			case R.id.sort_duration_button:
				newSort = Sort.DURATION;
				break;
			case R.id.sort_price_button:
			default:
				newSort = Sort.PRICE;
				break;
			}

			filter.setSort(newSort);
			filter.notifyFilterChanged();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Airline preference

	// Add a button for each airline on the airline preference container
	private void configureAirlines() {
		mAirportButtons = new HashMap<String, ToggleButton>();

		LayoutInflater inflater = getActivity().getLayoutInflater();

		FlightSearch search = Db.getFlightSearch();

		int legPosition = getArguments().getInt(ARG_LEG_POSITION);
		List<FlightTrip> trips = search.getTrips(legPosition);
		Set<String> airlines = getAirlines(trips, legPosition);

		for (String airlineCode : airlines) {
			ToggleButton airlineButton = (ToggleButton) inflater.inflate(R.layout.snippet_airline_button,
					mAirlineContainer, false);

			final Airline airline = Db.getAirline(airlineCode);

			airlineButton.setText(airline.mAirlineName);
			airlineButton.setTextOn(airline.mAirlineName);
			airlineButton.setTextOff(airline.mAirlineName);

			airlineButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					FlightFilter filter = getFlightFilter();
					filter.setPreferredAirline(airline.mAirlineCode, isChecked);
					filter.notifyFilterChanged();
				}
			});

			mAirlineContainer.addView(airlineButton);

			mAirportButtons.put(airlineCode, airlineButton);
		}
	}

	// TODO: Should this be based on operating airline?  Marketing airline?  Not sure,
	// so it only uses operating airline at this point.
	private Set<String> getAirlines(List<FlightTrip> trips, int legPosition) {
		Set<String> airlines = new HashSet<String>();

		for (FlightTrip trip : trips) {
			FlightLeg leg = trip.getLeg(legPosition);
			airlines.addAll(leg.getPrimaryAirlines());
		}

		return airlines;
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience utilities

	private FlightFilter getFlightFilter() {
		return Db.getFlightSearch().getFilter(getArguments().getInt(ARG_LEG_POSITION));
	}
}
