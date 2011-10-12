package com.expedia.bookings.fragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.GuestsPickerUtils;
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

		// Need to set temporary max values for number pickers, or updateViews() won't work (since a picker value
		// must be within its valid range to be set)
		mAdultsNumberPicker.setMaxValue(100);
		mChildrenNumberPicker.setMaxValue(100);

		updateViews();

		// Configure the location EditText
		mLocationEditText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!isHidden()) {
					String location = s.toString().trim();
					if (location.length() == 0) {
						((TabletActivity) getActivity()).setMyLocationSearch();
					}
					else {
						((TabletActivity) getActivity()).setFreeformLocation(mLocationEditText.getText().toString());
					}
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing
			}

			public void afterTextChanged(Editable s) {
				// Do nothing
			}
		});

		// Configure the calendar
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker);
		mCalendarDatePicker.setOnDateChangedListener(new OnDateChangedListener() {
			public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
				if (!isHidden()) {
					Calendar checkIn = new GregorianCalendar(mCalendarDatePicker.getStartYear(), mCalendarDatePicker
							.getStartMonth(), mCalendarDatePicker.getStartDayOfMonth());
					Calendar checkOut = new GregorianCalendar(mCalendarDatePicker.getEndYear(), mCalendarDatePicker
							.getEndMonth(), mCalendarDatePicker.getEndDayOfMonth());

					((TabletActivity) getActivity()).setDates(checkIn, checkOut);
				}
			}
		});

		// Configure the number pickers
		GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
		OnValueChangeListener valueChangeListener = new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				if (!isHidden()) {
					((TabletActivity) getActivity()).setGuests(mAdultsNumberPicker.getValue(),
							mChildrenNumberPicker.getValue());
				}

				GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
			}
		};
		mAdultsNumberPicker.setOnValueChangedListener(valueChangeListener);
		mChildrenNumberPicker.setOnValueChangedListener(valueChangeListener);

		// Configure the search button
		Button button = (Button) view.findViewById(R.id.search_button);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
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
	// Views

	public void updateViews() {
		SearchParams params = ((TabletActivity) getActivity()).getSearchParams();

		mLocationEditText.setText(params.getSearchDisplayText(getActivity()));

		Calendar start = params.getCheckInDate();
		mCalendarDatePicker.updateStartDate(start.get(Calendar.YEAR), start.get(Calendar.MONTH),
				start.get(Calendar.DAY_OF_MONTH));
		Calendar end = params.getCheckOutDate();
		mCalendarDatePicker.updateEndDate(end.get(Calendar.YEAR), end.get(Calendar.MONTH),
				end.get(Calendar.DAY_OF_MONTH));

		mAdultsNumberPicker.setValue(params.getNumAdults());
		mChildrenNumberPicker.setValue(params.getNumChildren());
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_SEARCH_PARAMS_CHANGED:
			if (isHidden()) {
				updateViews();
			}
			break;
		}
	}
}
