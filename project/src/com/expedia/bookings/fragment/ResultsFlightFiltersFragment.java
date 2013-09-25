package com.expedia.bookings.fragment;

import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.Ui;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
public class ResultsFlightFiltersFragment extends Fragment {

	private static final String ARG_LEG_NUMBER = "ARG_LEG_NUMBER";

	private int mLegNumber;

	private RadioGroup mSortGroup;
	private RadioGroup mFilterGroup;
	private ViewGroup mAirlineContainer;

	public static ResultsFlightFiltersFragment newInstance(int legNumber) {
		ResultsFlightFiltersFragment frag = new ResultsFlightFiltersFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_LEG_NUMBER, legNumber);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLegNumber = getArguments().getInt(ARG_LEG_NUMBER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_flight_tablet_filter, container, false);

		mSortGroup = Ui.findView(view, R.id.flight_sort_control);
		mFilterGroup = Ui.findView(view, R.id.flight_filter_control);

		mAirlineContainer = Ui.findView(view, R.id.filter_airline_container);
		buildAirlineList();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mSortGroup.setOnCheckedChangeListener(mControlKnobListener);
		mFilterGroup.setOnCheckedChangeListener(mControlKnobListener);
	}

	private RadioGroup.OnCheckedChangeListener mControlKnobListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			FlightFilter filter = Db.getFlightSearch().getFilter(mLegNumber);

			switch (checkedId) {
			case R.id.flight_sort_arrives:
			case R.id.flight_sort_departs:
			case R.id.flight_sort_duration:
			case R.id.flight_sort_price:
				switch (checkedId) {
				case R.id.flight_sort_arrives:
					filter.setSort(FlightFilter.Sort.ARRIVAL);
					break;
				case R.id.flight_sort_departs:
					filter.setSort(FlightFilter.Sort.DEPARTURE);
					break;
				case R.id.flight_sort_duration:
					filter.setSort(FlightFilter.Sort.DURATION);
					break;
				case R.id.flight_sort_price:
					filter.setSort(FlightFilter.Sort.PRICE);
					break;
				}
				filter.notifyFilterChanged();
				break;
			case R.id.flight_filter_stop_any:
			case R.id.flight_filter_stop_one_or_less:
			case R.id.flight_filter_stop_none:
				switch (checkedId) {
				case R.id.flight_filter_stop_any:
					filter.setStops(FlightFilter.STOPS_ANY);
					break;
				case R.id.flight_filter_stop_one_or_less:
					filter.setStops(FlightFilter.STOPS_MAX);
					break;
				case R.id.flight_filter_stop_none:
					filter.setStops(FlightFilter.STOPS_NONSTOP);
					break;
				}
				filter.notifyFilterChanged();
				break;
			}
			onFilterChanged();
		}
	};

	private void onFilterChanged() {
		buildAirlineList();
	}

	private void buildAirlineList() {
		Map<String, FlightTrip> cheapestTrips = Db.getFlightSearch().queryTrips(mLegNumber).getCheapestTripsByAirline();
		FlightTrip[] trips = new FlightTrip[cheapestTrips.values().size()];
		cheapestTrips.values().toArray(trips);

		int numTripsToShow = trips.length;
		int numTripsInContainer = mAirlineContainer.getChildCount();

		FlightTrip trip;
		TextView tv;
		for (int i = 0; i < numTripsToShow; i++) {
			trip = trips[i];
			if (i < numTripsInContainer) {
				tv = (TextView) mAirlineContainer.getChildAt(i);
				tv.setVisibility(View.VISIBLE);
			}
			else {
				tv = new TextView(getActivity());
				mAirlineContainer.addView(tv);
			}
			tv.setText(trip.getLeg(0).getAirlinesFormatted() + " - "
					+ trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));
		}

		// Keep around the views not needed, but just set their visibility to GONE.
		for (int i = numTripsToShow; i < numTripsInContainer; i++) {
			mAirlineContainer.getChildAt(i).setVisibility(View.GONE);
		}

	}
}
