package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.widget.CalendarDatePicker;

public class FlightSearchParamsFragment extends Fragment {

	public static final String TAG = FlightSearchParamsFragment.class.toString();

	private static final String ARG_DIM_BACKGROUND = "ARG_DIM_BACKGROUND";

	private View mDimmerView;
	private EditText mDepartureAirportEditText;
	private EditText mArrivalAirportEditText;
	private Button mDatesButton;
	private View mPassengersButton;
	private CalendarDatePicker mCalendarDatePicker;

	public static FlightSearchParamsFragment newInstance(boolean dimBackground) {
		FlightSearchParamsFragment fragment = new FlightSearchParamsFragment();
		Bundle args = new Bundle();
		args.putBoolean(ARG_DIM_BACKGROUND, dimBackground);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_search_params, container, false);

		// Cache views
		mDimmerView = Ui.findView(v, R.id.dimmer_view);
		mDepartureAirportEditText = Ui.findView(v, R.id.departure_airport_edit_text);
		mArrivalAirportEditText = Ui.findView(v, R.id.arrival_airport_edit_text);
		mDatesButton = Ui.findView(v, R.id.dates_button);
		mPassengersButton = Ui.findView(v, R.id.passengers_button);
		mCalendarDatePicker = Ui.findView(v, R.id.calendar_date_picker);

		// Configure views
		if (getArguments().getBoolean(ARG_DIM_BACKGROUND)) {
			mDimmerView.setVisibility(View.VISIBLE);
		}

		OnClickListener disableCalendarListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleCalendarDatePicker(false);
			}
		};
		mDepartureAirportEditText.setOnClickListener(disableCalendarListener);
		mArrivalAirportEditText.setOnClickListener(disableCalendarListener);
		mPassengersButton.setOnClickListener(disableCalendarListener);

		mDatesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleCalendarDatePicker(true);
			}
		});

		// Initial calendar date picker variables
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker, CalendarDatePicker.SelectionMode.HYBRID);

		return v;
	}

	//////////////////////////////////////////////////////////////////////////
	// View control

	public void toggleCalendarDatePicker(boolean enabled) {
		// TODO: Add animations

		mCalendarDatePicker.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}

}
