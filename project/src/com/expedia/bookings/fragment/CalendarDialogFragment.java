package com.expedia.bookings.fragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.OnDateChangedListener;

public class CalendarDialogFragment extends DialogFragment {

	private static final String KEY_START_YEAR = "startYear";
	private static final String KEY_START_MONTH = "startMonth";
	private static final String KEY_START_DAY_OF_MONTH = "startDayOfMonth";
	private static final String KEY_END_YEAR = "endYear";
	private static final String KEY_END_MONTH = "endMonth";
	private static final String KEY_END_DAY_OF_MONTH = "endDayOfMonth";

	private CalendarDatePicker mCalendarDatePicker;

	private CalendarDialogFragmentListener mListener;

	private Calendar mInitialStartDate;
	private Calendar mInitialEndDate;

	public static CalendarDialogFragment newInstance(Calendar startDate, Calendar endDate) {
		CalendarDialogFragment dialog = new CalendarDialogFragment();
		dialog.setInitialDates(startDate, endDate);
		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof CalendarDialogFragmentListener)) {
			throw new RuntimeException("CalendarDialogFragment Activity must implement CalendarDialogFragmentListener!");
		}

		mListener = (CalendarDialogFragmentListener) activity;
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
		if (savedInstanceState == null) {
			mCalendarDatePicker.updateStartDate(mInitialStartDate.get(Calendar.YEAR),
					mInitialStartDate.get(Calendar.MONTH), mInitialStartDate.get(Calendar.DAY_OF_MONTH));
			mCalendarDatePicker.updateEndDate(mInitialEndDate.get(Calendar.YEAR), mInitialEndDate.get(Calendar.MONTH),
					mInitialEndDate.get(Calendar.DAY_OF_MONTH));
		}
		else {
			mCalendarDatePicker.updateStartDate(savedInstanceState.getInt(KEY_START_YEAR),
					savedInstanceState.getInt(KEY_START_MONTH), savedInstanceState.getInt(KEY_START_DAY_OF_MONTH));
			mCalendarDatePicker.updateEndDate(savedInstanceState.getInt(KEY_END_YEAR),
					savedInstanceState.getInt(KEY_END_MONTH), savedInstanceState.getInt(KEY_END_DAY_OF_MONTH));
		}

		builder.setTitle(getTitleText());

		mCalendarDatePicker.setOnDateChangedListener(new OnDateChangedListener() {
			public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
				updateTitle();
			}
		});

		// Configure buttons
		builder.setPositiveButton(R.string.search, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Calendar checkIn = new GregorianCalendar(mCalendarDatePicker.getStartYear(), mCalendarDatePicker
						.getStartMonth(), mCalendarDatePicker.getStartDayOfMonth());
				Calendar checkOut = new GregorianCalendar(mCalendarDatePicker.getEndYear(), mCalendarDatePicker
						.getEndMonth(), mCalendarDatePicker.getEndDayOfMonth());

				mListener.onChangeDates(checkIn, checkOut);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(KEY_START_YEAR, mCalendarDatePicker.getStartYear());
		outState.putInt(KEY_START_MONTH, mCalendarDatePicker.getStartMonth());
		outState.putInt(KEY_START_DAY_OF_MONTH, mCalendarDatePicker.getStartDayOfMonth());
		outState.putInt(KEY_END_YEAR, mCalendarDatePicker.getEndYear());
		outState.putInt(KEY_END_MONTH, mCalendarDatePicker.getEndMonth());
		outState.putInt(KEY_END_DAY_OF_MONTH, mCalendarDatePicker.getEndDayOfMonth());
	}

	private CharSequence getTitleText() {
		return CalendarUtils.getCalendarDatePickerTitle(getActivity(), mCalendarDatePicker);
	}

	private void updateTitle() {
		getDialog().setTitle(getTitleText());
	}

	private void setInitialDates(Calendar startDate, Calendar endDate) {
		mInitialStartDate = startDate;
		mInitialEndDate = endDate;
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface CalendarDialogFragmentListener {
		public void onChangeDates(Calendar start, Calendar end);
	}
}
