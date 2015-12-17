package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
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

	private FlightFilter mFilter;

	private Spinner mSortGroup;
	private SlidingRadioGroup mFilterGroup;

	private TextView mFilterGroupHeader;

	private ViewGroup mAirlineContainer;

	private TextView mDepartureAirportsHeader;
	private AirportFilterWidget mDepartureAirportFilterWidget;

	private TextView mArrivalAirportsHeader;
	private AirportFilterWidget mArrivalAirportFilterWidget;

	private View mSortOverlay;
	private View mDepartureOverlay;
	private View mArrivalOverlay;
	private View mAirlineOverlay;

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

		mFilterGroupHeader = Ui.findView(view, R.id.stops_header);

		mAirlineContainer = Ui.findView(view, R.id.filter_airline_container);

		mDepartureAirportsHeader = Ui.findView(view, R.id.departure_airports_header);
		mDepartureAirportFilterWidget = Ui.findView(view, R.id.departure_airports_widget);

		mArrivalAirportsHeader = Ui.findView(view, R.id.arrival_airports_header);
		mArrivalAirportFilterWidget = Ui.findView(view, R.id.arrival_airports_widget);

		mSortOverlay = Ui.findView(view, R.id.flight_filter_sort_overlay);
		mDepartureOverlay = Ui.findView(view, R.id.flight_filter_departure_airports_overlay);
		mArrivalOverlay = Ui.findView(view, R.id.flight_filter_arrival_airports_overlay);
		mAirlineOverlay = Ui.findView(view, R.id.flight_filter_airline_overlay);


		List<String> sortOptions = new ArrayList<>();
		sortOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.sort_options_flights)));

		ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_sort_item, sortOptions);
		adapter.setDropDownViewResource(R.layout.spinner_sort_dropdown_item);

		mSortGroup.setAdapter(adapter);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		bindAll();
	}

	public void onFilterChanged() {
		if (mFilter != null) {
			mFilter.notifyFilterChanged();
		}
		bindAll();
		AdTracker.trackFilteredFlightSearch(mLegNumber);
	}

	public boolean requiredDataInDb() {
		return Db.getFlightSearch() != null && Db.getFlightSearch().getSearchResponse() != null;
	}

	public void bindAll() {
		if (mSortGroup != null && requiredDataInDb()) {
			refreshDbFilter();
			bindSortFilter();
			bindAirportFilter();
			buildAirlineList();
		}
	}

	public void refreshDbFilter() {
		mFilter = Db.getFlightSearch().getFilter(mLegNumber);
	}

	private void bindSortFilter() {
		mSortGroup.setOnItemSelectedListener(null);
		mFilterGroup.setOnCheckedChangeListener(null);

		mSortGroup.setSelection(mFilter.getSort().ordinal());

		// Stops filter
		FlightSearch.FlightTripQuery query = Db.getFlightSearch().queryTrips(mLegNumber);
		List<Integer> numStopsList = query.getNumberOfStops();
		mFilterGroup.removeAllViews();
		mFilterGroup.clearCheck();
		if (query.getNumberOfStops().size() > 1) {
			for (Integer integer : numStopsList) {
				int stops = integer.intValue();
				RadioButton rad = Ui.inflate(R.layout.snippet_flight_filter_radio_button, mFilterGroup, false);
				String str;
				if (stops == 0) {
					str = getString(R.string.stop_description__nonstop);
				}
				else {
					str = getResources().getQuantityString(R.plurals.x_Stops_TEMPLATE, stops, stops);
				}
				rad.setText(str);
				rad.setId(FlightFilter.getStopsViewIdFromStopsValue(stops));
				mFilterGroup.addView(rad);
			}
			mFilterGroup.setVisibility(View.VISIBLE);
			mFilterGroupHeader.setVisibility(View.VISIBLE);
		}
		else {
			mFilterGroup.setVisibility(View.GONE);
			mFilterGroupHeader.setVisibility(View.GONE);
		}

		mFilterGroup.check(FlightFilter.getStopsViewIdFromStopsValue(mFilter.getStops()));

		mSortGroup.setOnItemSelectedListener(mSortCheckedChangeListener);
		mFilterGroup.setOnCheckedChangeListener(mControlKnobListener);
	}

	private void bindAirportFilter() {
		FlightSearch.FlightTripQuery query = Db.getFlightSearch().queryTrips(mLegNumber);

		Set<String> departureAirports = query.getDepartureAirportCodes();
		mDepartureAirportFilterWidget
			.bind(mLegNumber, true, departureAirports, mFilter, mAirportOnCheckedChangeListener, mAirportPopupListener);
		mDepartureAirportsHeader.setVisibility(departureAirports.size() < 2 ? View.GONE : View.VISIBLE);

		Set<String> arrivalAirports = query.getArrivalAirportCodes();
		mArrivalAirportFilterWidget
			.bind(mLegNumber, false, arrivalAirports, mFilter, mAirportOnCheckedChangeListener, mAirportPopupListener);
		mArrivalAirportsHeader.setVisibility(arrivalAirports.size() < 2 ? View.GONE : View.VISIBLE);
	}

	private void buildAirlineList() {
		FlightSearch.FlightTripQuery query = Db.getFlightSearch().queryTrips(mLegNumber);
		Map<String, FlightTrip> cheapestTripsMap = query.getCheapestTripsByAirline();

		int numTripsToShow = cheapestTripsMap == null ? 0 : cheapestTripsMap.values().size();
		int numTripsInContainer = mAirlineContainer.getChildCount();

		FlightTrip trip;
		CheckBoxFilterWidget airlineFilterWidget;
		boolean enabled;
		int index = 0;
		for (String airlineCode : cheapestTripsMap.keySet()) {
			trip = cheapestTripsMap.get(airlineCode);
			if (index < numTripsInContainer) {
				airlineFilterWidget = (CheckBoxFilterWidget) mAirlineContainer.getChildAt(index);
				airlineFilterWidget.setVisibility(View.VISIBLE);
			}
			else {
				airlineFilterWidget = new CheckBoxFilterWidget(getActivity());
				mAirlineContainer.addView(airlineFilterWidget);
			}

			enabled = query.getAirlinesFilteredByStopsAndAirports().contains(airlineCode);
			airlineFilterWidget.bindFlight(Db.getFlightSearch().getFilter(mLegNumber), airlineCode, trip);
			airlineFilterWidget.setTag(airlineCode);
			airlineFilterWidget.setEnabled(enabled);
			airlineFilterWidget.setOnCheckedChangeListener(mAirlineOnCheckedChangeListener);

			index++;
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
			if (group.getId() == R.id.flight_filter_control) {
				int newStops = FlightFilter.getStopsValueFromStopsViewId(checkedId);
				if (mFilter.getStops() != newStops) {
					OmnitureTracking.trackNumStopsFlightFilter(newStops);
				}
				mFilter.setStops(newStops);
			}

			onFilterChanged();
		}
	};

	private CheckBoxFilterWidget.OnCheckedChangeListener mAirlineOnCheckedChangeListener = new CheckBoxFilterWidget.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CheckBoxFilterWidget view, boolean isChecked) {
			String airlineCode = (String) view.getTag();
			mFilter.setPreferredAirline(airlineCode, isChecked);
			OmnitureTracking.trackLinkFlightFilter("Airline");
			onFilterChanged();
		}
	};

	private CheckBoxFilterWidget.OnCheckedChangeListener mAirportOnCheckedChangeListener = new CheckBoxFilterWidget.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CheckBoxFilterWidget view, boolean isChecked) {
			String[] split = ((String) view.getTag()).split(";");
			boolean departureAirport = Boolean.parseBoolean(split[0]);
			String airportCode = split[1];
			if (isChecked) {
				mFilter.addAirport(departureAirport, airportCode);
			}
			else {
				mFilter.removeAirport(departureAirport, airportCode);
			}
			OmnitureTracking.trackLinkFlightFilter("Airport");
			onFilterChanged();
		}
	};

	private AirportFilterWidget.AirportFilterWidgetListener mAirportPopupListener = new AirportFilterWidget.AirportFilterWidgetListener() {
		@Override
		public void onPopupToggled(boolean isShowing, boolean departureAirport) {
			int visibility = isShowing ? View.VISIBLE : View.GONE;

			if (departureAirport) {
				mArrivalOverlay.setVisibility(visibility);
			}
			else {
				mDepartureOverlay.setVisibility(visibility);
			}

			mSortOverlay.setVisibility(visibility);
			mAirlineOverlay.setVisibility(visibility);
		}
	};

	private final AdapterView.OnItemSelectedListener mSortCheckedChangeListener = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			FlightFilter.Sort sort = FlightFilter.Sort.values()[position];
			if (sort != null) {
				if (mFilter.getSort() != sort) {
					mFilter.setSort(sort);
					OmnitureTracking.trackLinkFlightSort(sort.toString());
				}
			}

			onFilterChanged();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	};

}
