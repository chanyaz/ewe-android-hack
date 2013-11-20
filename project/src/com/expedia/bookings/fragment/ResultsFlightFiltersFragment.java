package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AirportFilterWidget;
import com.expedia.bookings.widget.CheckBoxFilterWidget;
import com.expedia.bookings.widget.SlidingRadioGroup;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
public class ResultsFlightFiltersFragment extends Fragment {

	private static final String ARG_LEG_NUMBER = "ARG_LEG_NUMBER";

	private int mLegNumber;

	private SlidingRadioGroup mSortGroup;
	private SlidingRadioGroup mFilterGroup;

	private ViewGroup mAirlineContainer;

	private TextView mDepartureAirportsHeader;
	private AirportFilterWidget mDepartureAirportFilterWidget;

	private TextView mArrivalAirportsHeader;
	private AirportFilterWidget mArrivalAirportFilterWidget;

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
		mSortGroup.setOnCheckedChangeListener(mControlKnobListener);
		mFilterGroup.setOnCheckedChangeListener(mControlKnobListener);

		mAirlineContainer = Ui.findView(view, R.id.filter_airline_container);

		mDepartureAirportsHeader = Ui.findView(view, R.id.departure_airports_header);
		mDepartureAirportFilterWidget = Ui.findView(view, R.id.departure_airports_widget);

		mArrivalAirportsHeader = Ui.findView(view, R.id.arrival_airports_header);
		mArrivalAirportFilterWidget = Ui.findView(view, R.id.arrival_airports_widget);

		Set<String> departureAirports = Db.getFlightSearch().getAirports(mLegNumber, true);
		FlightFilter filter = Db.getFlightSearch().getFilter(mLegNumber);
		mDepartureAirportFilterWidget
				.bind(mLegNumber, true, departureAirports, filter, mAirportOnCheckedChangeListener);
		mDepartureAirportsHeader.setVisibility(departureAirports.size() < 2 ? View.GONE : View.VISIBLE);

		Set<String> arrivalAirports = Db.getFlightSearch().getAirports(mLegNumber, false);
		mArrivalAirportFilterWidget.bind(mLegNumber, false, arrivalAirports, filter, mAirportOnCheckedChangeListener);
		mArrivalAirportsHeader.setVisibility(arrivalAirports.size() < 2 ? View.GONE : View.VISIBLE);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		bindAll();
	}

	public void onFilterChanged() {
		onFilterChanged(Db.getFlightSearch().getFilter(mLegNumber));
	}

	public void onFilterChanged(FlightFilter filter) {
		filter.notifyFilterChanged();

		bindAll();
	}

	private void bindAll() {
		bindSortFilter();
		bindAirportFilter();
		buildAirlineList();
	}

	private void bindSortFilter() {
		FlightFilter.Sort sort = Db.getFlightSearch().getFilter(mLegNumber).getSort();
		mSortGroup.check(SORT_RADIO_BUTTON_MAP.get(sort));

		int filter = Db.getFlightSearch().getFilter(mLegNumber).getStops();
		mFilterGroup.check(STOPS_FILTER_RES_ID_MAP.get(filter));
	}

	private void bindAirportFilter() {
		mDepartureAirportFilterWidget.bindLabel();
		mArrivalAirportFilterWidget.bindLabel();
	}

	private void buildAirlineList() {
		FlightFilter filter = Db.getFlightSearch().getFilter(mLegNumber);

		List<FlightTrip> allTrips = Db.getFlightSearch().getTrips(mLegNumber);
		List<FlightTrip> filteredTrips = FlightSearch.getTripsFilteredByStops(mLegNumber, allTrips, filter.getStops());
		filteredTrips = FlightSearch.getTripsFilteredByAirport(mLegNumber, filteredTrips,
				filter.getDepartureAirports(), true);
		filteredTrips = FlightSearch.getTripsFilteredByAirport(mLegNumber, filteredTrips,
				filter.getArrivalAirports(), false);
		Map<String, FlightTrip> cheapestTripsMap = FlightSearch.getCheapestTripEachAirlineMap(mLegNumber, allTrips);

		// Update the cheapest trips based on the trips available after filtering
		Map<String, FlightTrip> cheapestTripsByStopsMap = FlightSearch.getCheapestTripEachAirlineMap(mLegNumber,
				filteredTrips);
		for (String key : cheapestTripsByStopsMap.keySet()) {
			cheapestTripsMap.put(key, cheapestTripsByStopsMap.get(key));
		}

		int numTripsToShow = cheapestTripsMap == null ? 0 : cheapestTripsMap.values().size();
		int numTripsInContainer = mAirlineContainer.getChildCount();

		List<FlightTrip> trips = new ArrayList<FlightTrip>();
		if (numTripsToShow > 0) {
			trips = new ArrayList<FlightTrip>(cheapestTripsMap.values());
			Collections.sort(trips, new FlightTrip.FlightTripComparator(mLegNumber,
					FlightTrip.CompareField.AIRLINE_NAME));
		}

		FlightTrip trip;
		CheckBoxFilterWidget airlineFilterWidget;
		for (int i = 0; i < numTripsToShow; i++) {
			trip = trips.get(i);
			if (i < numTripsInContainer) {
				airlineFilterWidget = (CheckBoxFilterWidget) mAirlineContainer.getChildAt(i);
				airlineFilterWidget.setVisibility(View.VISIBLE);
			}
			else {
				airlineFilterWidget = new CheckBoxFilterWidget(getActivity());
				mAirlineContainer.addView(airlineFilterWidget);
			}

			boolean enabled = filteredTrips.contains(trip);
			airlineFilterWidget.bindFlight(Db.getFlightSearch().getFilter(mLegNumber), trip, mLegNumber);
			airlineFilterWidget.setTag(trip);
			airlineFilterWidget.setEnabled(enabled);
			airlineFilterWidget.setOnCheckedChangeListener(mAirlineOnCheckedChangeListener);

		}

		// Keep around the views not needed, but just set their visibility to GONE.
		for (int i = numTripsToShow; i < numTripsInContainer; i++) {
			mAirlineContainer.getChildAt(i).setVisibility(View.GONE);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// CheckedChange Listeners

	private RadioGroup.OnCheckedChangeListener mControlKnobListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			FlightFilter filter = Db.getFlightSearch().getFilter(mLegNumber);

			switch (checkedId) {
			case R.id.flight_sort_arrives:
			case R.id.flight_sort_departs:
			case R.id.flight_sort_duration:
			case R.id.flight_sort_price:
				filter.setSort(RES_ID_SORT_MAP.get(Integer.valueOf(checkedId)));
				break;
			case R.id.flight_filter_stop_any:
			case R.id.flight_filter_stop_one_or_less:
			case R.id.flight_filter_stop_none:
				filter.setStops(RES_ID_STOPS_FILTER_MAP.get(checkedId));
				break;
			}

			onFilterChanged(filter);
		}
	};

	private CheckBoxFilterWidget.OnCheckedChangeListener mAirlineOnCheckedChangeListener = new CheckBoxFilterWidget.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CheckBoxFilterWidget view, boolean isChecked) {
			FlightFilter filter = Db.getFlightSearch().getFilter(mLegNumber);
			FlightTrip trip = (FlightTrip) view.getTag();
			filter.setPreferredAirline(trip.getLeg(mLegNumber).getFirstAirlineCode(), isChecked);
			filter.notifyFilterChanged();
		}
	};

	private CheckBoxFilterWidget.OnCheckedChangeListener mAirportOnCheckedChangeListener = new CheckBoxFilterWidget.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CheckBoxFilterWidget view, boolean isChecked) {
			FlightFilter filter = Db.getFlightSearch().getFilter(mLegNumber);
			String[] split = ((String) view.getTag()).split(";");
			boolean departureAirport = Boolean.parseBoolean(split[0]);
			String airportCode = split[1];
			if (isChecked) {
				filter.addAirport(departureAirport, airportCode);
			}
			else {
				filter.removeAirport(departureAirport, airportCode);
			}

			onFilterChanged(filter);
		}
	};

	/////////////////////////////////////////////////////////////////////////
	// Static maps for Filter -> resId and resId - Filter

	private static final Map<Integer, FlightFilter.Sort> RES_ID_SORT_MAP = new HashMap<Integer, FlightFilter.Sort>() {
		{
			put(R.id.flight_sort_arrives, FlightFilter.Sort.ARRIVAL);
			put(R.id.flight_sort_departs, FlightFilter.Sort.DEPARTURE);
			put(R.id.flight_sort_duration, FlightFilter.Sort.DURATION);
			put(R.id.flight_sort_price, FlightFilter.Sort.PRICE);
		}
	};

	private static final Map<FlightFilter.Sort, Integer> SORT_RADIO_BUTTON_MAP = new HashMap<FlightFilter.Sort, Integer>() {
		{
			put(FlightFilter.Sort.ARRIVAL, R.id.flight_sort_arrives);
			put(FlightFilter.Sort.DEPARTURE, R.id.flight_sort_departs);
			put(FlightFilter.Sort.DURATION, R.id.flight_sort_duration);
			put(FlightFilter.Sort.PRICE, R.id.flight_sort_price);
		}
	};

	private static final SparseIntArray RES_ID_STOPS_FILTER_MAP = new SparseIntArray() {
		{
			put(R.id.flight_filter_stop_any, FlightFilter.STOPS_ANY);
			put(R.id.flight_filter_stop_one_or_less, FlightFilter.STOPS_MAX);
			put(R.id.flight_filter_stop_none, FlightFilter.STOPS_NONSTOP);
		}
	};

	private static final SparseIntArray STOPS_FILTER_RES_ID_MAP = new SparseIntArray() {
		{
			put(FlightFilter.STOPS_ANY, R.id.flight_filter_stop_any);
			put(FlightFilter.STOPS_MAX, R.id.flight_filter_stop_one_or_less);
			put(FlightFilter.STOPS_NONSTOP, R.id.flight_filter_stop_none);
		}
	};

}
