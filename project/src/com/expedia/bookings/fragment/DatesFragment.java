package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.widget.CalendarPicker;
import com.expedia.bookings.widget.CalendarPicker.DateSelectionChangedListener;
import com.mobiata.android.util.Ui;

/**
 * One important detail
 * 
 * 1. This is designed to work in conjunction with TabletSearchFragment.  As such, it does not
 *    keep track of its own internal state; I don't want to risk duplicating any data and
 *    confusing the issue.
 */
public class DatesFragment extends Fragment implements DateSelectionChangedListener {

	private static final int DATE_BOX_FLAGS = DateUtils.FORMAT_SHOW_DATE;

	private DatesFragmentListener mListener;

	private TextView mStatusTextView;
	private TextView mStartTextView;
	private TextView mEndTextView;
	private CalendarPicker mCalendarPicker;

	// These are only used for the initial setting; they do not represent the state most of the time
	private LocalDate mStartDate;
	private LocalDate mEndDate;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, DatesFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dates, container, false);

		mStatusTextView = Ui.findView(view, R.id.status_text_view);
		mStartTextView = Ui.findView(view, R.id.start_text_view);
		mEndTextView = Ui.findView(view, R.id.end_text_view);
		mCalendarPicker = Ui.findView(view, R.id.calendar_picker);

		mCalendarPicker.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(330));
		mCalendarPicker.setSelectedDates(mStartDate, mEndDate);
		mCalendarPicker.setDateChangedListener(this);

		updateStatusText();
		updateDateBoxes();

		return view;
	}

	public void setDatesFromParams(SearchParams searchParams) {
		mStartDate = searchParams.getStartDate();
		mEndDate = searchParams.getEndDate();

		if (mCalendarPicker != null) {
			mCalendarPicker.setSelectedDates(mStartDate, mEndDate);
		}
	}

	private void updateStatusText() {
		updateStatusText(getSelectedDates());
	}

	private void updateStatusText(Pair<LocalDate, LocalDate> dates) {
		if (dates.first != null && dates.second != null) {
			int daysBetween = JodaUtils.daysBetween(dates.first, dates.second);
			mStatusTextView.setText(getString(R.string.dates_status_multi_TEMPLATE, daysBetween));
		}
		else if (dates.first != null) {
			mStatusTextView.setText(R.string.dates_status_one);
		}
		else {
			mStatusTextView.setText(R.string.dates_status_none);
		}
	}

	private void updateDateBoxes() {
		updateDateBoxes(getSelectedDates());
	}

	private void updateDateBoxes(Pair<LocalDate, LocalDate> dates) {
		View selectedView = null;

		if (dates.first != null) {
			String date = JodaUtils.formatLocalDate(getActivity(), dates.first, DATE_BOX_FLAGS);
			mStartTextView.setText(getString(R.string.start_date_TEMPLATE, date));
		}
		else {
			mStartTextView.setText(getString(R.string.start_date_TEMPLATE, ""));
			selectedView = mStartTextView;
		}

		if (dates.second != null) {
			String date = JodaUtils.formatLocalDate(getActivity(), dates.second, DATE_BOX_FLAGS);
			mEndTextView.setText(getString(R.string.end_date_TEMPLATE, date));
		}
		else {
			mEndTextView.setText(R.string.end_date_optional);

			if (selectedView == null) {
				selectedView = mEndTextView;
			}
		}

		// Make sure only one box is selected
		mStartTextView.setBackgroundResource(R.drawable.bg_date_box_normal);
		mEndTextView.setBackgroundResource(R.drawable.bg_date_box_normal);
		if (selectedView != null) {
			selectedView.setBackgroundResource(R.drawable.bg_date_box_selected);
		}
	}

	private Pair<LocalDate, LocalDate> getSelectedDates() {
		LocalDate startDate = mCalendarPicker.getStartDate();
		LocalDate endDate = mCalendarPicker.getEndDate();
		return new Pair<LocalDate, LocalDate>(startDate, endDate);
	}

	public void onDateChanged() {
		Pair<LocalDate, LocalDate> dates = getSelectedDates();

		mListener.onDatesChanged(dates.first, dates.second);

		updateStatusText(dates);
		updateDateBoxes(dates);
	}

	//////////////////////////////////////////////////////////////////////////
	// DateSelectionChangedListener

	@Override
	public void onDateSelectionChanged(LocalDate start, LocalDate end) {
		Pair<LocalDate, LocalDate> dates = getSelectedDates();

		mListener.onDatesChanged(dates.first, dates.second);

		updateStatusText(dates);
		updateDateBoxes(dates);
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface DatesFragmentListener {
		public void onDatesChanged(LocalDate startDate, LocalDate endDate);
	}

}
