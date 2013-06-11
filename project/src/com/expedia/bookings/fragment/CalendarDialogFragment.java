package com.expedia.bookings.fragment;

import java.util.Calendar;

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

import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SearchParams;
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
		args.putInt(KEY_START_YEAR, startDate.get(Calendar.YEAR));
		args.putInt(KEY_START_MONTH, startDate.get(Calendar.MONTH));
		args.putInt(KEY_START_DAY_OF_MONTH, startDate.get(Calendar.DAY_OF_MONTH));
		args.putInt(KEY_END_YEAR, endDate.get(Calendar.YEAR));
		args.putInt(KEY_END_MONTH, endDate.get(Calendar.MONTH));
		args.putInt(KEY_END_DAY_OF_MONTH, endDate.get(Calendar.DAY_OF_MONTH));
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
		builder.setTitle(getTitleText(Db.getHotelSearch().getSearchParams()));

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
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker, CalendarDatePicker.SelectionMode.RANGE);

		// Set initial dates
		Bundle dateInfo = (savedInstanceState != null && savedInstanceState.containsKey(KEY_START_YEAR)) ? savedInstanceState
				: getArguments();
		mCalendarDatePicker.updateStartDate(dateInfo.getInt(KEY_START_YEAR),
				dateInfo.getInt(KEY_START_MONTH), dateInfo.getInt(KEY_START_DAY_OF_MONTH));
		mCalendarDatePicker.updateEndDate(dateInfo.getInt(KEY_END_YEAR),
				dateInfo.getInt(KEY_END_MONTH), dateInfo.getInt(KEY_END_DAY_OF_MONTH));
		mCalendarDatePicker.updateStateCache();
		mCalendarDatePicker.markAllCellsDirty();

		// The listener changes based on whether this is a dialog or not.  If it's a dialog, we just update
		// the title (and depend on a button press to indicate the dates changing).  For a normal fragment,
		// we send updates whenever the date selection changes.
		if (getShowsDialog()) {
			mCalendarDatePicker.setOnDateChangedListener(new OnDateChangedListener() {
				public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
					SearchParams workingSearchParams = new SearchParams();
					CalendarUtils.syncParamsFromDatePicker(workingSearchParams, mCalendarDatePicker);
					updateTitle(workingSearchParams);
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
			outState.putInt(KEY_START_YEAR, mCalendarDatePicker.getStartYear());
			outState.putInt(KEY_START_MONTH, mCalendarDatePicker.getStartMonth());
			outState.putInt(KEY_START_DAY_OF_MONTH, mCalendarDatePicker.getStartDayOfMonth());
			outState.putInt(KEY_END_YEAR, mCalendarDatePicker.getEndYear());
			outState.putInt(KEY_END_MONTH, mCalendarDatePicker.getEndMonth());
			outState.putInt(KEY_END_DAY_OF_MONTH, mCalendarDatePicker.getEndDayOfMonth());
		}
	}

	private CharSequence getTitleText(SearchParams workingSearchParams) {
		return CalendarUtils.getCalendarDatePickerTitle(getActivity(), workingSearchParams);
	}

	private void updateTitle(SearchParams workingSearchParams) {
		getDialog().setTitle(getTitleText(workingSearchParams));
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	private void notifyDateChangedListener() {
		Date start = new Date(mCalendarDatePicker.getStartYear(), mCalendarDatePicker
				.getStartMonth() + 1, mCalendarDatePicker.getStartDayOfMonth());
		Date end = new Date(mCalendarDatePicker.getEndYear(), mCalendarDatePicker
				.getEndMonth() + 1, mCalendarDatePicker.getEndDayOfMonth());

		mListener.onChangeDates(start, end);
	}

	public interface CalendarDialogFragmentListener {
		public void onChangeDates(Date start, Date end);
	}
}
