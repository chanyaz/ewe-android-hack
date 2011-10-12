package com.expedia.bookings.fragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.OnDateChangedListener;

public class SearchFragment extends Fragment implements EventHandler {

	public static SearchFragment newInstance() {
		return new SearchFragment();
	}

	private EditText mLocationEditText;
	private CalendarDatePicker mCalendarDatePicker;
	private NumberPicker mAdultsNumberPicker;
	private NumberPicker mChildrenNumberPicker;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((TabletActivity) getActivity()).registerEventHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);

		mLocationEditText = (EditText) view.findViewById(R.id.location_edit_text);
		mCalendarDatePicker = (CalendarDatePicker) view.findViewById(R.id.dates_date_picker);
		mAdultsNumberPicker = (NumberPicker) view.findViewById(R.id.adults_number_picker);
		mChildrenNumberPicker = (NumberPicker) view.findViewById(R.id.children_number_picker);

		// Configure the location EditText
		mLocationEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					((TabletActivity) getActivity()).setFreeformLocation(mLocationEditText.getText().toString());
				}
			}
		});

		// Configure the calendar
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker);
		mCalendarDatePicker.setOnDateChangedListener(new OnDateChangedListener() {
			public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
				Calendar checkIn = new GregorianCalendar(mCalendarDatePicker.getStartYear(), mCalendarDatePicker
						.getStartMonth(), mCalendarDatePicker.getStartDayOfMonth());
				Calendar checkOut = new GregorianCalendar(mCalendarDatePicker.getEndYear(), mCalendarDatePicker
						.getEndMonth(), mCalendarDatePicker.getEndDayOfMonth());

				((TabletActivity) getActivity()).setDates(checkIn, checkOut);
			}
		});

		// Configure the number pickers
		OnValueChangeListener valueChangeListener = new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				((TabletActivity) getActivity()).setGuests(mAdultsNumberPicker.getValue(),
						mChildrenNumberPicker.getValue());
			}
		};
		mAdultsNumberPicker.setOnValueChangedListener(valueChangeListener);
		mChildrenNumberPicker.setOnValueChangedListener(valueChangeListener);

		// TODO: Configure number pickers the same as we do with the dialog
		mAdultsNumberPicker.setMinValue(1);
		mAdultsNumberPicker.setMaxValue(4);
		mChildrenNumberPicker.setMinValue(0);
		mChildrenNumberPicker.setMaxValue(4);

		// Configure the search button
		Button button = (Button) view.findViewById(R.id.search_button);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mLocationEditText.clearFocus();

				((TabletActivity) getActivity()).startSearch();
			}
		});

		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		// Do nothing (for now)
	}
}
