package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.OnDateChangedListener;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class FlightSearchParamsFragment extends Fragment implements OnDateChangedListener {

	public static final String TAG = FlightSearchParamsFragment.class.toString();

	private static final String ARG_INITIAL_PARAMS = "ARG_INITIAL_PARAMS";
	private static final String ARG_DIM_BACKGROUND = "ARG_DIM_BACKGROUND";

	private static final String INSTANCE_SHOW_CALENDAR = "INSTANCE_SHOW_CALENDAR";

	// TODO: Localize this date format
	private static final String DATE_FORMAT = "MMM d";

	private View mFocusStealer;
	private View mDimmerView;
	private EditText mDepartureAirportEditText;
	private EditText mArrivalAirportEditText;
	private TextView mDatesTextView;
	private View mPassengersButton;
	private CalendarDatePicker mCalendarDatePicker;

	private FlightSearchParams mSearchParams;

	public static FlightSearchParamsFragment newInstance(FlightSearchParams initialParams, boolean dimBackground) {
		FlightSearchParamsFragment fragment = new FlightSearchParamsFragment();
		Bundle args = new Bundle();
		JSONUtils.putJSONable(args, ARG_INITIAL_PARAMS, initialParams);
		args.putBoolean(ARG_DIM_BACKGROUND, dimBackground);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSearchParams = JSONUtils.getJSONable(getArguments(), ARG_INITIAL_PARAMS, FlightSearchParams.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_search_params, container, false);

		// Cache views
		mFocusStealer = Ui.findView(v, R.id.focus_stealer);
		mDimmerView = Ui.findView(v, R.id.dimmer_view);
		mDepartureAirportEditText = Ui.findView(v, R.id.departure_airport_edit_text);
		mArrivalAirportEditText = Ui.findView(v, R.id.arrival_airport_edit_text);
		mDatesTextView = Ui.findView(v, R.id.dates_button);
		mPassengersButton = Ui.findView(v, R.id.passengers_button);
		mCalendarDatePicker = Ui.findView(v, R.id.calendar_date_picker);

		// Configure views
		if (getArguments().getBoolean(ARG_DIM_BACKGROUND)) {
			mDimmerView.setVisibility(View.VISIBLE);
		}

		OnFocusChangeListener airportFocusChangeListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					toggleCalendarDatePicker(false);

					// Clear out previous data
					TextView tv = (TextView) v;
					tv.setText(null);
				}
				else {
					if (v == mDepartureAirportEditText) {
						String airportCode = mDepartureAirportEditText.getText().toString().toUpperCase();
						if (!TextUtils.isEmpty(airportCode)) {
							mSearchParams.setDepartureAirportCode(airportCode);
						}
						updateAirportText(mDepartureAirportEditText, mSearchParams.getDepartureAirportCode());
					}
					else {
						String airportCode = mArrivalAirportEditText.getText().toString().toUpperCase();
						if (!TextUtils.isEmpty(airportCode)) {
							mSearchParams.setArrivalAirportCode(airportCode);
						}
						updateAirportText(mArrivalAirportEditText, mSearchParams.getArrivalAirportCode());
					}
				}
			}
		};
		mDepartureAirportEditText.setOnFocusChangeListener(airportFocusChangeListener);
		mArrivalAirportEditText.setOnFocusChangeListener(airportFocusChangeListener);

		if (savedInstanceState == null) {
			// Fill in the initial departure/arrival airports if we are just launching
			updateAirportText(mDepartureAirportEditText, mSearchParams.getDepartureAirportCode());
			updateAirportText(mArrivalAirportEditText, mSearchParams.getArrivalAirportCode());
		}

		mDatesTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearEditTextFocus();

				toggleCalendarDatePicker(true);
			}
		});

		mPassengersButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearEditTextFocus();

				toggleCalendarDatePicker(false);

				Toast.makeText(getActivity(), "TODO: Design & implement passenger picker", Toast.LENGTH_SHORT).show();
			}
		});

		// Initial calendar date picker variables
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker, CalendarDatePicker.SelectionMode.HYBRID);
		mCalendarDatePicker.setOnDateChangedListener(this);

		if (savedInstanceState != null) {
			toggleCalendarDatePicker(savedInstanceState.getBoolean(INSTANCE_SHOW_CALENDAR));
		}

		updateCalendarText();

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_SHOW_CALENDAR, mCalendarDatePicker.getVisibility() == View.VISIBLE);
	}

	//////////////////////////////////////////////////////////////////////////
	// View control

	private void updateAirportText(TextView textView, String airportCode) {
		Airport airport = FlightStatsDbUtils.getAirport(airportCode);
		if (airport == null) {
			textView.setText(null);
		}
		else {
			String str = getString(R.string.search_airport_TEMPLATE, airport.mAirportCode, airport.mCity);
			textView.setText(Html.fromHtml(str));
		}
	}

	private void clearEditTextFocus() {
		EditText textWithFocus = null;

		if (mDepartureAirportEditText.hasFocus()) {
			textWithFocus = mDepartureAirportEditText;
		}
		else if (mArrivalAirportEditText.hasFocus()) {
			textWithFocus = mArrivalAirportEditText;
		}

		if (textWithFocus != null) {
			mFocusStealer.requestFocus();

			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(textWithFocus.getWindowToken(), 0);
		}
	}

	private void toggleCalendarDatePicker(boolean enabled) {
		if (enabled == (mCalendarDatePicker.getVisibility() == View.VISIBLE)) {
			return;
		}

		if (enabled) {
			Date departureDate = mSearchParams.getDepartureDate();
			Date returnDate = mSearchParams.getReturnDate();

			if (departureDate != null) {
				mCalendarDatePicker.updateStartDate(departureDate.getYear(), departureDate.getMonth(),
						departureDate.getDayOfMonth());
			}
			if (returnDate != null) {
				mCalendarDatePicker.updateEndDate(returnDate.getYear(), returnDate.getMonth(),
						returnDate.getDayOfMonth());
			}

			if (returnDate == null) {
				mCalendarDatePicker.setOneWayResearchMode(true);
			}
		}

		mCalendarDatePicker.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}

	private void updateCalendarText() {
		Calendar dateStart = mSearchParams.getDepartureDate() == null ? null : mSearchParams.getDepartureDate()
				.getCalendar();
		Calendar dateEnd = mSearchParams.getReturnDate() == null ? null : mSearchParams.getReturnDate().getCalendar();

		if (dateStart == null && dateEnd == null) {
			mDatesTextView.setText(null);
		}
		else if (dateStart != null && dateEnd == null) {
			CharSequence start = DateFormat.format(DATE_FORMAT, dateStart);
			mDatesTextView.setText(Html.fromHtml(getString(R.string.one_way_TEMPLATE, start)));
		}
		else if (dateStart != null && dateEnd != null) {
			CharSequence start = DateFormat.format(DATE_FORMAT, dateStart);
			CharSequence end = DateFormat.format(DATE_FORMAT, dateEnd);
			mDatesTextView.setText(Html.fromHtml(getString(R.string.round_trip_TEMPLATE, start, end)));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Access

	public FlightSearchParams getSearchParams() {
		return mSearchParams;
	}

	//////////////////////////////////////////////////////////////////////////
	// OnDateChangedListener

	@Override
	public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
		mSearchParams.setDepartureDate(new Date(mCalendarDatePicker.getStartYear(),
				mCalendarDatePicker.getStartMonth(), mCalendarDatePicker.getStartDayOfMonth()));

		if (mCalendarDatePicker.getEndTime() != null) {
			mSearchParams.setReturnDate(new Date(mCalendarDatePicker.getEndYear(),
					mCalendarDatePicker.getEndMonth(), mCalendarDatePicker.getEndDayOfMonth()));
		}
		else {
			mSearchParams.setReturnDate(null);
		}

		updateCalendarText();
	}
}
