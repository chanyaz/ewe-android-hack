package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CheckBoxFilterWidget;
import com.expedia.bookings.widget.CheckBoxFilterWidget.OnCheckedChangeListener;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
public class ResultsFlightFiltersFragment extends Fragment {

	private static final String ARG_LEG_NUMBER = "ARG_LEG_NUMBER";

	private int mLegNumber;

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

		RadioGroup sortGroup = Ui.findView(view, R.id.flight_sort_control);
		sortGroup.setOnCheckedChangeListener(mControlKnobListener);
		RadioGroup filterGroup = Ui.findView(view, R.id.flight_filter_control);
		filterGroup.setOnCheckedChangeListener(mControlKnobListener);

		int departureAirportIndex, arrivalAirportIndex;
		if (mLegNumber == 0) {
			departureAirportIndex = 0;
			arrivalAirportIndex = 1;
		}
		else {
			departureAirportIndex = 1;
			arrivalAirportIndex = 0;
		}

		Spinner departureAirportsSpinner = Ui.findView(view, R.id.departure_airports_spinner);
		TextView departureAirportsHeader = Ui.findView(view, R.id.departure_airports_header);
		Spinner arrivalAirportsSpinner = Ui.findView(view, R.id.arrival_airports_spinner);
		TextView arrivalAirportsHeader = Ui.findView(view, R.id.arrival_airports_header);

		configureAirportFilter(departureAirportIndex, departureAirportsSpinner, departureAirportsHeader);
		configureAirportFilter(arrivalAirportIndex, arrivalAirportsSpinner, arrivalAirportsHeader);

		mAirlineContainer = Ui.findView(view, R.id.filter_airline_container);

		buildAirlineList();

		return view;
	}

	private void configureAirportFilter(final int airportIndex, Spinner spinner, TextView header) {
		final List<String> airports = new ArrayList<String>();

		// Add a placeholder for the first position. When accessed via the Spinner, metro codes from
		// the SearchParams will be retrieved.
		airports.add("metro-code-placeholder");

		airports.addAll(Db.getFlightSearch().getDepartureAirportsForLeg(airportIndex));

		if (airports.size() <= 2) {
			// One for the placeholder, one for the minimum number of airports. Any more means a metro
			// code was searched.
			spinner.setVisibility(View.GONE);
			header.setVisibility(View.GONE);
		}
		else {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_spinner_item,
					airports) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					return getAirportView(airports, position, airportIndex, convertView, parent);
				}

				@Override
				public View getDropDownView(int position, View convertView, ViewGroup parent) {
					return getAirportView(airports, position, airportIndex, convertView, parent);
				}
			};

			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					FlightFilter filter = Db.getFlightSearch().getFilter(mLegNumber);
					if (position == 0) {
						if (airportIndex == 0) {
							filter.clearDepartureAirports();
						}
						else {
							filter.clearArrivalAirports();
						}
					}
					else {
						if (airportIndex == 0) {
							filter.addDepartureAirport(airports.get(position));
						}
						else {
							filter.addArrivalAirport(airports.get(position));
						}
					}
					filter.notifyFilterChanged();
					onFilterChanged();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// We don't need to execute any code if the user doesn't select a row.
				}
			});

			spinner.setVisibility(View.VISIBLE);
			header.setVisibility(View.VISIBLE);
		}
	}

	private View getAirportView(List<String> codes, int spinnerPos, int airportIndex, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getActivity()).inflate(R.layout.simple_spinner_item, parent, false);
		}
		String text;

		String code = codes.get(spinnerPos);

		if (spinnerPos == 0) {
			Location loc;
			if (airportIndex == 0) {
				loc = Db.getFlightSearch().getSearchParams().getDepartureLocation();
			}
			else {
				loc = Db.getFlightSearch().getSearchParams().getArrivalLocation();
			}
			text = loc.getDestinationId() + " - " + loc.getDescription();
		}
		else {
			Airport airport = FlightStatsDbUtils.getAirport(code);
			text = airport.mAirportCode + " - " + airport.mName;
		}

		// TODO use string template, use span, match mocks
		((TextView) convertView).setText(text);
		return convertView;
	}

	private static final Map<Integer, FlightFilter.Sort> RES_ID_SORT_MAP = new HashMap<Integer, FlightFilter.Sort>() {
		{
			put(R.id.flight_sort_arrives, FlightFilter.Sort.ARRIVAL);
			put(R.id.flight_sort_departs, FlightFilter.Sort.DEPARTURE);
			put(R.id.flight_sort_duration, FlightFilter.Sort.DURATION);
			put(R.id.flight_sort_price, FlightFilter.Sort.PRICE);
		}
	};

	private static final SparseIntArray RES_ID_STOPS_FILTER_MAP = new SparseIntArray() {
		{
			put(R.id.flight_filter_stop_any, FlightFilter.STOPS_ANY);
			put(R.id.flight_filter_stop_one_or_less, FlightFilter.STOPS_MAX);
			put(R.id.flight_filter_stop_none, FlightFilter.STOPS_NONSTOP);
		}
	};

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

			filter.notifyFilterChanged();
			onFilterChanged();
		}
	};

	private OnCheckedChangeListener mAirlineOnCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CheckBoxFilterWidget view, boolean isChecked) {
			FlightFilter filter = Db.getFlightSearch().getFilter(mLegNumber);
			FlightTrip trip = (FlightTrip) view.getTag();
			filter.setPreferredAirline(trip.getLeg(mLegNumber).getFirstAirlineCode(), isChecked);
			filter.notifyFilterChanged();
		}
	};

	public void onFilterChanged() {
		buildAirlineList();
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

}
