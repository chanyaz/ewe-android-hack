package com.expedia.bookings.fragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.widget.CalendarDatePicker;

public class CalendarDialogFragment extends DialogFragment {

	private CalendarDatePicker mCalendarDatePicker;

	private Calendar mInitialStartDate;
	private Calendar mInitialEndDate;

	public static CalendarDialogFragment newInstance(Calendar startDate, Calendar endDate) {
		CalendarDialogFragment dialog = new CalendarDialogFragment();
		dialog.setInitialDates(startDate, endDate);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.fragment_dialog_calendar, null);
		mCalendarDatePicker = (CalendarDatePicker) view.findViewById(R.id.dates_date_picker);
		builder.setView(view);

		// Initial calendar date picker variables
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker);

		// Set initial dates
		mCalendarDatePicker.updateStartDate(mInitialStartDate.get(Calendar.YEAR),
				mInitialStartDate.get(Calendar.MONTH), mInitialStartDate.get(Calendar.DAY_OF_MONTH));
		mCalendarDatePicker.updateEndDate(mInitialEndDate.get(Calendar.YEAR),
				mInitialEndDate.get(Calendar.MONTH), mInitialEndDate.get(Calendar.DAY_OF_MONTH));

		// Configure buttons
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Calendar checkIn = new GregorianCalendar(mCalendarDatePicker.getStartYear(), mCalendarDatePicker
						.getStartMonth(), mCalendarDatePicker.getStartDayOfMonth());
				Calendar checkOut = new GregorianCalendar(mCalendarDatePicker.getEndYear(), mCalendarDatePicker
						.getEndMonth(), mCalendarDatePicker.getEndDayOfMonth());
				((TabletActivity) getActivity()).setDates(checkIn, checkOut);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		return builder.create();
	}

	private void setInitialDates(Calendar startDate, Calendar endDate) {
		mInitialStartDate = startDate;
		mInitialEndDate = endDate;
	}
}
