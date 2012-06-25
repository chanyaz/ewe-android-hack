package com.expedia.bookings.fragment;

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
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.GregorianCalendar;

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

	public static CalendarDialogFragment newInstance(Calendar startDate, Calendar endDate) {
		CalendarDialogFragment dialog = new CalendarDialogFragment();
		Bundle args = new Bundle();
		if (startDate != null) {
			args.putInt(KEY_START_YEAR, startDate.get(Calendar.YEAR));
			args.putInt(KEY_START_MONTH, startDate.get(Calendar.MONTH));
			args.putInt(KEY_START_DAY_OF_MONTH, startDate.get(Calendar.DAY_OF_MONTH));
		}
		if (endDate != null) {
			args.putInt(KEY_END_YEAR, endDate.get(Calendar.YEAR));
			args.putInt(KEY_END_MONTH, endDate.get(Calendar.MONTH));
			args.putInt(KEY_END_DAY_OF_MONTH, endDate.get(Calendar.DAY_OF_MONTH));
		}
		dialog.setArguments(args);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (!getShowsDialog()) {
			return createView(inflater, container, savedInstanceState);
		}
		else {
			return super.onCreateView(inflater, container, savedInstanceState);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = createView(inflater, null, savedInstanceState);
		builder.setView(view);

		// Dialog-specific stuff
		builder.setTitle(getTitleText());

		// Configure buttons
		builder.setPositiveButton(R.string.search, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				notifyDateChangedListener();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	private View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dialog_calendar, container, false);

		mCalendarDatePicker = (CalendarDatePicker) view.findViewById(R.id.dates_date_picker);

		// If we're showing it as a dialog, we want to limit the height (this is done in the layout
		// itself).  Otherwise, we will just fill the parent.
		if (!getShowsDialog()) {
			mCalendarDatePicker.getLayoutParams().height = ViewGroup.LayoutParams.FILL_PARENT;
		}

		// Initial calendar date picker variables
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker, CalendarDatePicker.SelectionMode.HYBRID);

		// Set initial dates
		Bundle dateInfo = (savedInstanceState != null && savedInstanceState.containsKey(KEY_START_YEAR)) ? savedInstanceState
				: getArguments();
		if (dateInfo.containsKey(KEY_START_YEAR)) {
			mCalendarDatePicker.updateStartDate(dateInfo.getInt(KEY_START_YEAR),
					dateInfo.getInt(KEY_START_MONTH), dateInfo.getInt(KEY_START_DAY_OF_MONTH));
		}

		if (dateInfo.containsKey(KEY_END_YEAR)) {
			mCalendarDatePicker.updateEndDate(dateInfo.getInt(KEY_END_YEAR),
					dateInfo.getInt(KEY_END_MONTH), dateInfo.getInt(KEY_END_DAY_OF_MONTH));
		}

		// The listener changes based on whether this is a dialog or not.  If it's a dialog, we just update
		// the title (and depend on a button press to indicate the dates changing).  For a normal fragment,
		// we send updates whenever the date selection changes.
		if (getShowsDialog()) {
			mCalendarDatePicker.setOnDateChangedListener(new OnDateChangedListener() {
				public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
					updateTitle();
				}
			});
		}
		else {
			mCalendarDatePicker.setOnDateChangedListener(new OnDateChangedListener() {
				public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
					notifyDateChangedListener();
				}
			});
		}

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mCalendarDatePicker != null) {
			if (mCalendarDatePicker.getStartTime() != null) {
				outState.putInt(KEY_START_YEAR, mCalendarDatePicker.getStartYear());
				outState.putInt(KEY_START_MONTH, mCalendarDatePicker.getStartMonth());
				outState.putInt(KEY_START_DAY_OF_MONTH, mCalendarDatePicker.getStartDayOfMonth());
			}
			else {
				outState.remove(KEY_START_YEAR);
				outState.remove(KEY_START_MONTH);
				outState.remove(KEY_START_DAY_OF_MONTH);
			}
			if (mCalendarDatePicker.getEndTime() != null) {
				outState.putInt(KEY_END_YEAR, mCalendarDatePicker.getEndYear());
				outState.putInt(KEY_END_MONTH, mCalendarDatePicker.getEndMonth());
				outState.putInt(KEY_END_DAY_OF_MONTH, mCalendarDatePicker.getEndDayOfMonth());
			}
			else {
				outState.remove(KEY_END_YEAR);
				outState.remove(KEY_END_MONTH);
				outState.remove(KEY_END_DAY_OF_MONTH);
			}
		}
	}

	private CharSequence getTitleText() {
		return CalendarUtils.getCalendarDatePickerTitle(getActivity(), mCalendarDatePicker);
	}

	private void updateTitle() {
		getDialog().setTitle(getTitleText());
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	private void notifyDateChangedListener() {
		Calendar start = new GregorianCalendar(mCalendarDatePicker.getStartYear(), mCalendarDatePicker
				.getStartMonth(), mCalendarDatePicker.getStartDayOfMonth());

		Calendar end;

		if (mCalendarDatePicker.getEndTime() != null) {
			end = new GregorianCalendar(mCalendarDatePicker.getEndYear(), mCalendarDatePicker.getEndMonth(),
					mCalendarDatePicker.getEndDayOfMonth());
		}
		else {
			end = null;
		}

		mListener.onChangeDates(start, end);
	}

	public interface CalendarDialogFragmentListener {
		public void onChangeDates(Calendar start, Calendar end);
	}
}
