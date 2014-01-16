package com.expedia.bookings.fragment;

import java.util.HashMap;
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

	private FlightFilter mFilter;

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
		mFilter = Db.getFlightSearch().getFilter(mLegNumber);
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

		FlightSearch.FlightTripQuery query = Db.getFlightSearch().queryTrips(mLegNumber);
		Set<String> departureAirports = query.getDepartureAirportCodes();
		mDepartureAirportFilterWidget
				.bind(mLegNumber, true, departureAirports, mFilter, mAirportOnCheckedChangeListener);
		mDepartureAirportsHeader.setVisibility(departureAirports.size() < 2 ? View.GONE : View.VISIBLE);

		Set<String> arrivalAirports = query.getArrivalAirportCodes();
		mArrivalAirportFilterWidget.bind(mLegNumber, false, arrivalAirports, mFilter, mAirportOnCheckedChangeListener);
		mArrivalAirportsHeader.setVisibility(arrivalAirports.size() < 2 ? View.GONE : View.VISIBLE);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		bindAll();
	}

	public void onFilterChanged() {
		mFilter.notifyFilterChanged();

		bindAll();
	}

	private void bindAll() {
		bindSortFilter();
		bindAirportFilter();
		buildAirlineList();
	}

	private void bindSortFilter() {
		mSortGroup.check(SORT_RADIO_BUTTON_MAP.get(mFilter.getSort()));
		mFilterGroup.check(STOPS_FILTER_RES_ID_MAP.get(mFilter.getStops()));
	}

	private void bindAirportFilter() {
		mDepartureAirportFilterWidget.bindLabel();
		mArrivalAirportFilterWidget.bindLabel();
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
			FlightFilter.Sort sort = RES_ID_SORT_MAP.get(Integer.valueOf(checkedId));
			if (sort != null) {
				mFilter.setSort(sort);
			}

			int stops = RES_ID_STOPS_FILTER_MAP.get(checkedId, FlightFilter.STOPS_UNSPECIFIED);
			if (stops != FlightFilter.STOPS_UNSPECIFIED) {
				mFilter.setStops(stops);
			}

			onFilterChanged();
		}
	};

	private CheckBoxFilterWidget.OnCheckedChangeListener mAirlineOnCheckedChangeListener = new CheckBoxFilterWidget.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CheckBoxFilterWidget view, boolean isChecked) {
			String airlineCode = (String) view.getTag();
			mFilter.setPreferredAirline(airlineCode, isChecked);

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

			onFilterChanged();
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
