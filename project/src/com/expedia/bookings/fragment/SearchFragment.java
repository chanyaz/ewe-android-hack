package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.widget.CalendarDatePicker;

public class SearchFragment extends Fragment implements EventHandler {

	public static SearchFragment newInstance() {
		return new SearchFragment();
	}

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

		mCalendarDatePicker = (CalendarDatePicker) view.findViewById(R.id.dates_date_picker);
		mAdultsNumberPicker = (NumberPicker) view.findViewById(R.id.adults_number_picker);
		mChildrenNumberPicker = (NumberPicker) view.findViewById(R.id.children_number_picker);

		// TODO: Configure number pickers the same as we do with the dialog
		mAdultsNumberPicker.setMinValue(1);
		mAdultsNumberPicker.setMaxValue(4);
		mChildrenNumberPicker.setMinValue(0);
		mChildrenNumberPicker.setMaxValue(4);

		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker);

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
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		// Do nothing (for now)
	}
}
