package com.expedia.bookings.fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.section.InfantSeatingOptionSpinnerAdapter;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AirportFilterWidget;
import com.expedia.bookings.widget.CheckBoxFilterWidget;
import com.expedia.bookings.widget.SlidingRadioGroup;
import com.squareup.otto.Subscribe;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
public class ResultsFlightFiltersFragment extends Fragment {

	private static final String ARG_LEG_NUMBER = "ARG_LEG_NUMBER";
	private static final int INFANT_CALLBACK_ID = 4000;

	private int mLegNumber;

	private FlightFilter mFilter;

	private Spinner mInfantSeatingSpinner;

	private SlidingRadioGroup mSortGroup;
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

		setUpInfantToggle(view);
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

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		bindAll();

		Events.register(this);
		Sp.getBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		Events.unregister(this);
		Sp.getBus().unregister(this);
	}

	public void onFilterChanged() {
		if (mFilter != null) {
			mFilter.notifyFilterChanged();
		}
		bindAll();
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

	// Infants

	public void setUpInfantToggle(View view) {
		View toggleLayout = Ui.findView(view, R.id.infant_toggle_container);
		View toggleHeader = Ui.findView(view, R.id.infant_toggle_header);
		if (Db.getFlightSearch().getSearchParams().hasInfants()) {
			boolean infantInLap = Db.getFlightSearch().getSearchParams().getInfantSeatingInLap();
			toggleLayout.setVisibility(View.VISIBLE);
			toggleHeader.setVisibility(View.VISIBLE);
			mInfantSeatingSpinner = Ui.findView(view, R.id.edit_infant_seating_spinner);
			mInfantSeatingSpinner.setAdapter(new InfantSeatingOptionSpinnerAdapter(getActivity()));
			mInfantSeatingSpinner.setSelection(infantInLap ? 0 : 1);
			mInfantSeatingSpinner.setOnItemSelectedListener(mInfantSpinnerListener);
		}
		else {
			toggleLayout.setVisibility(View.GONE);
			toggleHeader.setVisibility(View.GONE);
		}
	}

	private final AdapterView.OnItemSelectedListener mInfantSpinnerListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			boolean infantStatusDb = Db.getFlightSearch().getSearchParams().getInfantSeatingInLap();
			boolean infantStatusSelected = (Boolean) mInfantSeatingSpinner.getAdapter().getItem(position);

			if (infantStatusDb != infantStatusSelected) {
				// Pop a simple dialog
				String title = getString(R.string.infant_seating_preference);
				String message = infantStatusSelected ? getString(R.string.infants_in_laps_prompt) : getString(R.string.infants_in_seats_prompt);
				String button = getString(R.string.ok);
				SimpleCallbackDialogFragment df = SimpleCallbackDialogFragment.newInstance(title, message, button, INFANT_CALLBACK_ID);
				df.show(getFragmentManager(), "infantDialog");
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// No-op
		}
	};

	@Subscribe
	public void onSimpleDialogCallbackClick(Events.SimpleCallBackDialogOnClick click) {
		if (click.callBackId == INFANT_CALLBACK_ID) {
			boolean newLapPref = !Db.getFlightSearch().getSearchParams().getInfantSeatingInLap();
			Sp.getParams().setInfantsInLaps(newLapPref);
			Sp.reportSpUpdate();
		}
	}

	@Subscribe
	public void onSimpleDialogCallbackCancel(Events.SimpleCallBackDialogOnCancel cancel) {
		if (cancel.callBackId == INFANT_CALLBACK_ID) {
			// Revert the spinner to match Db
			mInfantSeatingSpinner.setOnItemSelectedListener(null);
			boolean infantInLap = Db.getFlightSearch().getSearchParams().getInfantSeatingInLap();
			mInfantSeatingSpinner.setSelection(infantInLap ? 0 : 1);
			mInfantSeatingSpinner.setOnItemSelectedListener(mInfantSpinnerListener);
		}
	}

	public void refreshDbFilter() {
		mFilter = Db.getFlightSearch().getFilter(mLegNumber);
	}

	private void bindSortFilter() {
		mSortGroup.setOnCheckedChangeListener(null);
		mFilterGroup.setOnCheckedChangeListener(null);

		// Sort
		mSortGroup.check(SORT_RADIO_BUTTON_MAP.get(mFilter.getSort()));

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

		mSortGroup.setOnCheckedChangeListener(mControlKnobListener);
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
			FlightFilter.Sort sort = RES_ID_SORT_MAP.get(Integer.valueOf(checkedId));
			if (sort != null) {
				mFilter.setSort(sort);
			}

			if (group.getId() == R.id.flight_filter_control) {
				mFilter.setStops(FlightFilter.getStopsValueFromStopsViewId(checkedId));
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

	/////////////////////////////////////////////////////////////////////////
	// Static maps for Sort -> resId and resId - Sort

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

}
